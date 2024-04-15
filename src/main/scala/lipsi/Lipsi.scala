/*
 * Copyright: 2017, Technical University of Denmark, DTU Compute
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * License: Simplified BSD License
 * 
 * Lipsi, a very tiny processor.
 */

package lipsi

import chisel3._
import chisel3.stage.{ChiselStage, ChiselGeneratorAnnotation}
import chisel3.util._

/*

Instruction encoding (plus timing):

0fff rrrr ALU register (2 cc)
1000 rrrr st rx (1 cc)
1001 rrrr brl rx
1010 rrrr ldind (rx) (3 cc)
1011 rrrr stind (rx) (2 cc)
1100 -fff + nnnn nnnn ALU imm (2 cc)
1101 --00 + aaaa aaaa br (2 cc)
1101 --10 + aaaa aaaa brz
1101 --11 + aaaa aaaa brnz
1110 --ff ALU shift
1111 aaaa IO (1 cc)
1111 1111 exit for the tester

ALU function:

add, sub, adc, sbb, and, or, xor, ld

*/

class DebugData extends Bundle {
  val pc = UInt(8.W)
  val accu = UInt(8.W)
  val exit = Bool()
}

class Lipsi(prog: String, val memSize: Int = 256, val debug: Boolean = false ) extends Module {
  val io = IO(new Bundle {
    val dout = Output(UInt(8.W))
    val din = Input(UInt(8.W))
    val dbg = if (debug) Some(Output(new DebugData)) else None
  })

  

  val pcReg = RegInit(0.U(8.W))
  val accuReg = RegInit(0.U(8.W))
  val enaAccuReg = RegInit(false.B)

  val enaPcReg = RegInit(false.B)

  val funcReg = RegInit(0.U(3.W))
  // debug(funcReg) Chisel 2

  // IO register
  val outReg = RegInit(0.U(8.W))
  val enaIoReg = RegInit(false.B)

  val mem = Module(new Memory(prog, memSize))

  //  val selPC = Bool(true)
  //  val selData = Bool(false)

  val rdData = mem.io.rdData

  // the following is used?
  val regInstr = RegNext(rdData)

  //  val rdAddr = Mux(selPC, Cat(UInt(0, 1), regPC + UInt(1)),
  //    Cat(UInt(1, 1), Mux(selData, rdData, regA)))

  // Do we need a support of storing the PC?
  // Probably, but it should be simple into a fixed register (15))
  val isCall = false.B

  val wrEna = Wire(Bool())
  val wrAddr = Wire(UInt())
  val rdAddr = Wire(UInt())
  val updPC = Wire(Bool())

  mem.io.rdAddr := rdAddr
  mem.io.wrAddr := Cat(1.U(1.W), wrAddr(7, 0))
  mem.io.wrData := Mux(isCall, pcReg, accuReg)
  mem.io.wrEna := wrEna

  val nextPC = Wire(UInt())
  // defaults
  wrEna := false.B
  wrAddr := rdData
  rdAddr := Cat(0.U(1.W), nextPC)
  updPC := true.B
  nextPC := pcReg + 1.U

  when(enaPcReg) {
    nextPC := rdData
  }
  when(updPC) {
    pcReg := nextPC
  }

  val fetch :: execute :: stind :: ldind1 :: ldind2 :: exit :: Nil = Enum(6)
  val stateReg = RegInit(fetch)
  // debug(stateReg)

  val exitReg = RegInit(false.B)
  // debug(exitReg) Chisel 2

  val accuZero = accuReg === 0.U

  val doBranch = (rdData(1, 0) === 0.U) ||
    ((rdData(1, 0) === 2.U) && accuZero) ||
    ((rdData(1, 0) === 3.U) && !accuZero)

  enaAccuReg := false.B
  enaPcReg := false.B
  enaIoReg := false.B

  // debug(enaAccuReg) Chisel 2
  switch(stateReg) {
    is(fetch) {
      stateReg := execute
      funcReg := rdData(6, 4)
      // ALU register
      when(rdData(7) === 0.U) {
        updPC := false.B
        funcReg := rdData(6, 4)
        enaAccuReg := true.B
        rdAddr := Cat(0x10.U, rdData(3, 0))
      }
      // st rx, is just a single cycle
      when(rdData(7, 4) === 0x8.U) {
        wrAddr := Cat(0.U, rdData(3, 0))
        wrEna := true.B
        stateReg := fetch
      }
      // ldind
      when(rdData(7, 4) === 0xa.U) {
        updPC := false.B
        rdAddr := Cat(0x10.U, rdData(3, 0))
        stateReg := ldind1
      }
      // stind
      when(rdData(7, 4) === 0xb.U) {
        updPC := false.B
        rdAddr := Cat(0x10.U, rdData(3, 0))
        stateReg := stind
      }
      // ALU imm
      when(rdData(7, 4) === 0xc.U) {
        funcReg := rdData(2, 0)
        enaAccuReg := true.B
      }
      // Branch
      when(rdData(7, 4) === 0xd.U) {
        when(doBranch) {
          enaPcReg := true.B
        }
      }
      // IO
      when(rdData === 0xf0.U) {
        outReg := accuReg
        enaIoReg := true.B
        stateReg := fetch
      }
      // exit (for the tester)
      when(rdData === 0xff.U) {
        stateReg := exit
      }
    }
    is(stind) {
      wrEna := true.B
      stateReg := fetch
    }
    is(execute) {
      stateReg := fetch
    }
    is(ldind1) {
      updPC := false.B
      funcReg := 7.U
      enaAccuReg := true.B
      rdAddr := Cat(0x1.U, rdData)
      stateReg := ldind2
    }
    is(ldind2) {
      stateReg := fetch
    }
    is(exit) {
      exitReg := true.B
    }
  }

  val op = rdData
  val res = Wire(UInt())
  res := 0.U(8.W)

  val add :: sub :: adc :: sbb :: and :: or :: xor :: ld :: Nil = Enum(8)
  switch(funcReg) {
    is(add) { res := accuReg + op }
    is(sub) { res := accuReg - op }
    is(adc) { res := accuReg + op } // TODO: adc
    is(sbb) { res := accuReg - op } // TODO: sbb
    is(and) { res := accuReg & op }
    is(or) { res := accuReg | op }
    is(xor) { res := accuReg ^ op }
    is(ld) { res := op }
  }
  when(enaAccuReg) {
    accuReg := res
  }
  when(enaIoReg) {
    accuReg := io.din
  }

  io.dout := outReg
  if (debug) {
    io.dbg.get.accu := accuReg
    io.dbg.get.pc := pcReg
    io.dbg.get.exit := exitReg
  }
  
}

class LipsiTop(prog: String, val debug : Boolean = false) extends Module {
  val io = IO(new Bundle {
    val dout = Output(UInt(8.W))
    val din = Input(UInt(8.W))
    val dbg = if (debug) Some(Output(new DebugData)) else None
  })

  

  val x = Wire(Bool())
  x := RegNext(reset)
  val y = Wire(Bool())
  y := !x

  val resetRegs = RegNext(y)

  // val resetRegs = RegNext(RegNext(reset))

  // val resetRegs = RegNext(!RegNext(reset))

  // val resetRegs = reset

  val many = false
  val N = 432

  if (many) {
    val lipsis = new Array[Lipsi](N)
    for (i <- 0 until N) {
      lipsis(i) = Module(new Lipsi(prog))
      lipsis(i).reset := resetRegs
    }
    lipsis(0).io.din := io.din
    io.dout := lipsis(N - 1).io.dout
    for (i <- 1 until N) lipsis(i).io.din := lipsis(i - 1).io.dout
    
  } else {
    val lipsi = Module(new Lipsi(prog))

      if (debug) {
        io.dbg.get <> lipsi.io.dbg.get
      }


    lipsi.reset := resetRegs
    io.dout <> lipsi.io.dout
    io.din <> lipsi.io.din
  }
}

object LipsiMain {
  def main(args: Array[String]): Unit = {
    println("Generating the Lipsi hardware")
    (new chisel3.stage.ChiselStage).execute(Array("--target-dir", "generated"),
      Seq(ChiselGeneratorAnnotation(() => new LipsiTop(args(0)))))
    /* Chisel 2
    chiselMain(Array("--backend", "v", "--targetDir", "generated"),
      () => Module(new LipsiTop(args(0))))
      */
  }
}
