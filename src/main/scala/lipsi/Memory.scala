/*
 * Copyright: 2017, Technical University of Denmark, DTU Compute
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * License: Simplified BSD License
 * 
 * Lipsi, a very minimalistic processor.
 */

package lipsi

import chisel3._
import lipsi.util._

/**
 * The memory for Lipsi.
 *
 * 256 byte instructions and 256 bytes data, using exactly one FPGA memory block
 * with preinitialized data.
 * 
 * As we cannot express initialized memory in Chisel (yet) we have a multiplexer between
 * memory and the instruction ROM table. Shall be substituted by a BlackBox and generated VHDL or Verilog.
 */
class Memory(prog: String, size: Int) extends Module {
  val io = IO(new Bundle {
    val rdAddr = Input(UInt(9.W))
    val rdData = Output(UInt(8.W))
    val wrEna = Input(Bool())
    val wrData = Input(UInt(8.W))
    val wrAddr = Input(UInt(9.W))
  })

  val regPC = RegInit(0.U(8.W))
  val rdAddrReg = RegInit(0.U(9.W))
  rdAddrReg := io.rdAddr

  val program = VecInit(Assembler.getProgram(prog).map(_.U))
  val instr = program(rdAddrReg(7, 0))

  /* Chisel 2 val mem = Mem(UInt(width = 8), 256, seqRead = true) */
  val mem = Mem(size, UInt(8.W))
  val data = mem(rdAddrReg(7, 0))
  when(io.wrEna) {
    mem(io.wrAddr) := io.wrData
  }
  
  // Output MUX for now
  io.rdData := Mux(rdAddrReg(8), data, instr)
}
