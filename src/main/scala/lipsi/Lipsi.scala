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
1101 --00 + aaaa aaaa br
1101 --10 + aaaa aaaa brz
1101 --11 + aaaa aaaa brnz
1110 --ff ALU shift
1111 aaaa IO
1111 1111 exit for the tester

ALU function:

add, sub, adc, sbb, and, or, xor, ld

*/

class Lipsi(prog: String) extends Module {
  val io = new Bundle {
    val pc = UInt(OUTPUT, 8)
    val acc = UInt(OUTPUT, 8)
    val data = UInt(OUTPUT, 8)
  }

  val pcReg = Reg(init = UInt(0, 8))
  val accuReg = Reg(init = UInt(0, 8))
  val enaAccuReg = Reg(init = Bool(false))

  val enaPcReg = Reg(init = Bool(false))

  val funcReg = Reg(init = UInt(0, 3))
  debug(funcReg)

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

  val wrEna = Bool()
  val wrAddr = UInt()
  val rdAddr = UInt()
  val updPC = Bool()

  mem.io.rdAddr := rdAddr
  mem.io.wrAddr := Cat(UInt(1, 1), wrAddr(7, 0))
  mem.io.wrData := Mux(isCall, pcReg, accuReg)
  mem.io.wrEna := wrEna

  val nextPC = UInt()
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
  val stateReg = Reg(init = fetch)
  debug(stateReg)
  val exitReg = Reg(init = Bool(false))
  debug(exitReg)

  val accuZero = Bool()
  accuZero := Bool(false)
  when(accuReg === Bits(0)) {
    accuZero := Bool(true)
  }

  val doBranch = (rdData(1, 0) === Bits(0)) ||
    ((rdData(1, 0) === Bits(2)) && accuZero) ||
    ((rdData(1, 0) === Bits(3)) && !accuZero)

  enaAccuReg := Bool(false)
  enaPcReg := Bool(false)

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

  val op = rdData
  val res = UInt()
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

  io.pc := pcReg
  io.acc := accuReg
  io.data := rdData
}

object LipsiMain {
  def main(args: Array[String]): Unit = {
    println("Generating the Lipsi hardware")
    chiselMain(Array("--backend", "v", "--targetDir", "generated"),
      () => Module(new Lipsi(args(0))))
  }
}
