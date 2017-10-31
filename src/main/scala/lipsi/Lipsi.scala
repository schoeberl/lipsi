/*
 * Copyright: 2017, Technical University of Denmark, DTU Compute
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * License: Simplified BSD License
 * 
 * Lipsi, a very minimalistic processor.
 */

package lipsi

import Chisel._

class Lipsi extends Module {
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

  val mem = Module(new Memory())

  val selPC = Bool(true)
  val selData = Bool(false)

  val rdData = mem.io.rdData

  val regInstr = Reg(next = rdData)

  val rdAddr = Mux(selPC, Cat(UInt(0, 1), regPC + UInt(1)),
    Cat(UInt(1, 1), Mux(selData, rdData, regA)))

  val wrEna = Bool(true)
  // Do we need a support of storing the PC?
  // Probably, but it should be simple into a fixed register (15))
  val isCall = Bool(false)

  mem.io.rdAddr := rdAddr
  mem.io.wrAddr := Cat(UInt(1, 1), rdData)
  mem.io.wrData := Mux(isCall, regPC, regA)
  mem.io.wrEna := wrEna

  val updPC = Bool(true)

  when(updPC) {
    regPC := rdAddr
  }

  val isLoad = Bool(false)

  val fetch :: execute :: load :: Nil = Enum(UInt(), 3)
  val stateReg = Reg(init = fetch)
  debug(stateReg)

  regEnaA := Bool(false)
  debug(regEnaA)
  switch(stateReg) {
    is(fetch) {
      stateReg := execute
      regFunc := rdData(6, 4)
      // ALU imm
      when(rdData(7, 4) === Bits(0xc)) {
        regFunc := rdData(2, 0)
        regEnaA := Bool(true)
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

  when
  io.pc := regPC
  io.acc := regA
  io.data := rdData
}

object LipsiMain {
  def main(args: Array[String]): Unit = {
    println("Generating the Lipsi hardware")
    chiselMain(Array("--backend", "v", "--targetDir", "generated"),
      () => Module(new Lipsi()))
  }
}
