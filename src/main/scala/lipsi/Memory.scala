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
  val io = IO(new Bundle {
    val rdAddr = Input(UInt(width = 9))
    val rdData = Output(UInt(width = 8))
    val wrEna = Input(Bool())
    val wrData = Input(UInt(width = 8))
    val wrAddr = Input(UInt(width = 9))
  })

  val regPC = RegInit(UInt(0, 8))
  val rdAddrReg = RegInit(UInt(0, 9))
  rdAddrReg := io.rdAddr

  val program = Vec(util.Assembler.getProgram(prog).map(Bits(_)))
  val instr = program(rdAddrReg(7, 0))

  /* Chisel 2 val mem = Mem(UInt(width = 8), 256, seqRead = true) */
  val mem = Mem(256, UInt(width = 8))
  val data = mem(rdAddrReg(7, 0))
  when(io.wrEna) {
    mem(io.wrAddr) := io.wrData
  }
  
  // Output MUX for now
  io.rdData := Mux(rdAddrReg(8), data, instr)
}
