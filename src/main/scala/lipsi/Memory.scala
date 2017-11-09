/*
 * Copyright: 2017, Technical University of Denmark, DTU Compute
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * License: Simplified BSD License
 * 
 * Lipsi, a very minimalistic processor.
 */

package lipsi

import Chisel._

/**
 * The memory for Lipsi.
 *
 * 256 byte instructions and 256 bytes data, using exactly one FPGA memory block
 * with preinitialized data.
 * 
 * As we cannot express initialized memory in Chisel (yet) we have a multiplexer between
 * memory and the instruction ROM table. Shall be substituted by a BlackBox and generated VHDL or Verilog.
 */
class Memory(prog: String) extends Module {
  val io = new Bundle {
    val rdAddr = UInt(INPUT, 9)
    val rdData = UInt(OUTPUT, 8)
    val wrEna = Bool(INPUT)
    val wrData = UInt(INPUT, 8)
    val wrAddr = UInt(INPUT, 9)
  }

  val regPC = Reg(init = UInt(0, 8))
  val rdAddrReg = Reg(init = UInt(0, 9), next = io.rdAddr)

  val program = Vec(util.Assembler.getProgram(prog).map(Bits(_)))
  val instr = program(rdAddrReg(7, 0))

  val mem = Mem(UInt(width = 8), 256, seqRead = true)
  val data = mem(rdAddrReg(7, 0))
  when(io.wrEna) {
    mem(io.wrAddr) := io.wrData
  }
  
  // Output MUX for now
  io.rdData := Mux(rdAddrReg(8), data, instr)
}
