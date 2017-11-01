/*
 * Copyright: 2017, Technical University of Denmark, DTU Compute
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * License: Simplified BSD License
 * 
 * Lipsi, a very minimalistic processor.
 */

package lipsi

import Chisel._

class Lipsi(prog: String) extends Module {
  val io = new Bundle {
    val pc = UInt(OUTPUT, 8)
    val acc = UInt(OUTPUT, 8)
    val data = UInt(OUTPUT, 8)
  }

  val regPC = Reg(init = UInt(0, 8))
  val regA = Reg(init = UInt(0, 8))
  val regEnaA = Reg(init = Bool(false))

  val regFunc = Reg(init = UInt(0, 3))
  debug(regFunc)

  val mem = Module(new Memory(prog))

  //  val selPC = Bool(true)
  //  val selData = Bool(false)

  val rdData = mem.io.rdData

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
  mem.io.wrData := Mux(isCall, regPC, regA)
  mem.io.wrEna := wrEna

  val isLoad = Bool(false)

  val nextPC = regPC + UInt(1)
  // defaults
  wrEna := Bool(false)
  wrAddr := rdData
  rdAddr := Cat(UInt(0, 1), nextPC)
  updPC := Bool(true)

  when(updPC) {
    regPC := nextPC
  }

  val fetch :: execute :: load :: exit :: Nil = Enum(UInt(), 4)
  val stateReg = Reg(init = fetch)
  debug(stateReg)
  val regExit = Reg(init = Bool(false))
  debug(regExit)

  regEnaA := Bool(false)
  debug(regEnaA)
  switch(stateReg) {
    is(fetch) {
      stateReg := execute
      regFunc := rdData(6, 4)
      // ALU register
      when(rdData(7) === Bits(0)) {
        updPC := Bool(false)
        regFunc := rdData(6, 4)
        regEnaA := Bool(true)
        rdAddr(8, 4) := UInt(0x10)
        rdAddr(3, 0) := rdData
      }
      // ALU imm
      when(rdData(7, 4) === Bits(0xc)) {
        regFunc := rdData(2, 0)
        regEnaA := Bool(true)
      }
      // st rx, is just a single cycle
      when(rdData(7, 4) === Bits(0x8)) {
        wrAddr(7, 4) := UInt(0)
        wrEna := Bool(true)
        stateReg := fetch
      }
      // exit (for the tester)
      when(rdData === Bits(0xff)) {
        stateReg := exit
      }
    }
    is(execute) {
      when(isLoad) {
        stateReg := load
      }.otherwise {
        stateReg := fetch
      }
    }
    is(load) {
      stateReg := fetch
    }
    is(exit) {
      regExit := Bool(true)
    }
  }

  val op = rdData
  val res = UInt()
  res := UInt(0, 8)

  val add :: sub :: adc :: sbb :: and :: or :: xor :: ld :: Nil = Enum(UInt(), 8)
  switch(regFunc) {
    is(add) { res := regA + op }
    is(sub) { res := regA - op }
    is(adc) { res := regA + op } // TODO: adc
    is(sbb) { res := regA - op } // TODO: sbb
    is(and) { res := regA & op }
    is(or) { res := regA | op }
    is(xor) { res := regA ^ op }
    is(ld) { res := op }
  }
  when(regEnaA) {
    regA := res
  }

  io.pc := regPC
  io.acc := regA
  io.data := rdData
}

object LipsiMain {
  def main(args: Array[String]): Unit = {
    println("Generating the Lipsi hardware")
    chiselMain(Array("--backend", "v", "--targetDir", "generated"),
      () => Module(new Lipsi(args(0))))
  }
}
