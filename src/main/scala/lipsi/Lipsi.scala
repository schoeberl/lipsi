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
  }

  val regPC = Reg(init = UInt(0, 8))
  val regA = Reg(init = UInt(0, 8))

  // In the original design the program shall be preloaded in the
  // FPGA on-chip memory. We cannot express this in Chisel 2.
  // Maybe this is possible in Chisel 3.
  // The the memory input register will be fed from the regPC input mux.
  val program = util.Assembler.getProgram()
  val instr = program(regPC)

  val isLoad = Bool(false)

  val fetch :: execute :: load :: Nil = Enum(UInt(), 3)
  val stateReg = Reg(init = fetch)

  switch(stateReg) {
    is(fetch) {
      stateReg := execute
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
  
  val op = UInt(7)
  
  // val add :: sub :: adc :: sbb :: Nil = Enum(UInt(), 4)
  switch(instr(1, 0)) {
    is(Bits(0)) { regA := regA + op }
    is(Bits(1)) { regA := regA - op }
  }

  val rdAddr = UInt(3, 8)
  val wrEna = Bool(true)
  val wrData = UInt(5, 8)
  val wrAddr = UInt(6, 8)

  val mem = Mem(UInt(width = 8), 256, seqRead = true)
  val rdData = mem(Reg(next = rdAddr))
  when(wrEna) {
    mem(wrAddr) := wrData
  }

  regPC := regPC + UInt(1)

  io.pc := regPC
  io.acc := regA
}
