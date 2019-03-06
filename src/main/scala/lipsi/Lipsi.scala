/*
 * Copyright: 2017, Technical University of Denmark, DTU Compute
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * License: Simplified BSD License
 * 
 * Lipsi, a very tiny processor.
 */

package lipsi

import Chisel._

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

class Lipsi(prog: String) extends Module {
  val io = IO(new Bundle {
    val dout = Output(UInt(width = 8))
    val din = Input(UInt(width = 8))
  })

  val pcReg = RegInit(UInt(0, 8))
  val accuReg = RegInit(UInt(0, 8))
  val enaAccuReg = RegInit(Bool(false))

  val enaPcReg = RegInit(Bool(false))

  val funcReg = RegInit(UInt(0, 3))
  debug(funcReg)

  // IO register
  val outReg = RegInit(UInt(0, 8))
  val enaIoReg = RegInit(Bool(false))

  val mem = Module(new Memory(prog))

  //  val selPC = Bool(true)
  //  val selData = Bool(false)

  val rdData = mem.io.rdData

  // the following is used?
  val regInstr = Reg(next = rdData)

  //  val rdAddr = Mux(selPC, Cat(UInt(0, 1), regPC + UInt(1)),
  //    Cat(UInt(1, 1), Mux(selData, rdData, regA)))

  // Do we need a support of storing the PC?
  // Probably, but it should be simple into a fixed register (15))
  val isCall = Bool(false)

  val wrEna = Wire(Bool())
  val wrAddr = Wire(UInt())
  val rdAddr = Wire(UInt())
  val updPC = Wire(Bool())

  mem.io.rdAddr := rdAddr
  mem.io.wrAddr := Cat(UInt(1, 1), wrAddr(7, 0))
  mem.io.wrData := Mux(isCall, pcReg, accuReg)
  mem.io.wrEna := wrEna

  val nextPC = Wire(UInt())
  // defaults
  wrEna := Bool(false)
  wrAddr := rdData
  rdAddr := Cat(UInt(0, 1), nextPC)
  updPC := Bool(true)
  nextPC := pcReg + UInt(1)

  when(enaPcReg) {
    nextPC := rdData
  }
  when(updPC) {
    pcReg := nextPC
  }

  val fetch :: execute :: stind :: ldind1 :: ldind2 :: exit :: Nil = Enum(UInt(), 6)
  val stateReg = RegInit(fetch)
  debug(stateReg)
  val exitReg = RegInit(Bool(false))
  debug(exitReg)

  val accuZero = Wire(Bool())
  accuZero := Bool(false)
  when(accuReg === Bits(0)) {
    accuZero := Bool(true)
  }

  val doBranch = (rdData(1, 0) === Bits(0)) ||
    ((rdData(1, 0) === Bits(2)) && accuZero) ||
    ((rdData(1, 0) === Bits(3)) && !accuZero)

  enaAccuReg := Bool(false)
  enaPcReg := Bool(false)
  enaIoReg := Bool(false)

  debug(enaAccuReg)
  switch(stateReg) {
    is(fetch) {
      stateReg := execute
      funcReg := rdData(6, 4)
      // ALU register
      when(rdData(7) === Bits(0)) {
        updPC := Bool(false)
        funcReg := rdData(6, 4)
        enaAccuReg := Bool(true)
        rdAddr(8, 4) := UInt(0x10)
        rdAddr(3, 0) := rdData
      }
      // st rx, is just a single cycle
      when(rdData(7, 4) === Bits(0x8)) {
        wrAddr(7, 4) := UInt(0)
        wrEna := Bool(true)
        stateReg := fetch
      }
      // ldind
      when(rdData(7, 4) === Bits(0xa)) {
        updPC := Bool(false)
        rdAddr(8, 4) := UInt(0x10)
        rdAddr(3, 0) := rdData
        stateReg := ldind1
      }
      // stind
      when(rdData(7, 4) === Bits(0xb)) {
        updPC := Bool(false)
        rdAddr(8, 4) := UInt(0x10)
        rdAddr(3, 0) := rdData
        stateReg := stind
      }
      // ALU imm
      when(rdData(7, 4) === Bits(0xc)) {
        funcReg := rdData(2, 0)
        enaAccuReg := Bool(true)
      }
      // Branch
      when(rdData(7, 4) === Bits(0xd)) {
        when(doBranch) {
          enaPcReg := Bool(true)
        }
      }
      // IO
      when(rdData === Bits(0xf0)) {
        outReg := accuReg
        enaIoReg := Bool(true)
        stateReg := fetch
      }
      // exit (for the tester)
      when(rdData === Bits(0xff)) {
        stateReg := exit
      }
    }
    is(stind) {
      wrEna := Bool(true)
      stateReg := fetch
    }
    is(execute) {
      stateReg := fetch
    }
    is(ldind1) {
      updPC := Bool(false)
      funcReg := Bits(7)
      enaAccuReg := Bool(true)
      rdAddr(8) := UInt(0x1)
      rdAddr(7, 0) := rdData
      stateReg := ldind2
    }
    is(ldind2) {
      stateReg := fetch
    }
    is(exit) {
      exitReg := Bool(true)
    }
  }

  val op = Wire(rdData)
  val res = Wire(UInt())
  res := UInt(0, 8)

  val add :: sub :: adc :: sbb :: and :: or :: xor :: ld :: Nil = Enum(UInt(), 8)
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
}

class LipsiTop(prog: String) extends Module {
  val io = IO(new Bundle {
    val dout = Output(UInt(width = 8))
    val din = Input(UInt(width = 8))
  })

  val resetRegs = RegNext(!RegNext(reset))

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

    lipsi.reset := resetRegs
    io <> lipsi.io
  }
}

object LipsiMain {
  def main(args: Array[String]): Unit = {
    println("Generating the Lipsi hardware")
    chiselMain(Array("--backend", "v", "--targetDir", "generated"),
      () => Module(new LipsiTop(args(0))))
  }
}
