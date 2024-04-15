/*
 * Copyright: 2017, Technical University of Denmark, DTU Compute
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * License: Simplified BSD License
 * 
 * Test Lipsi.
 */

package lipsi


import chisel3._
import chisel3.iotesters.PeekPokeTester

class LipsiTester(dut: Lipsi) extends PeekPokeTester(dut) {
  if (dut.debug == false) {
    println(s"Lipsi must be built in debug mode")
    fail
  }
  else {
    var run = true
    var maxInstructions = 30
    while(run) {
      peek(dut.io.dbg.get.pc)
      peek(dut.io.dbg.get.accu)
      // peek(dut.mem.io.rdAddr)
      // peek(dut.stateReg) possible in Chisel 2
      step(1)
      maxInstructions -= 1
      run = peek(dut.io.dbg.get.exit) == 0 && maxInstructions > 0
      // poke(dut.io.din, maxInstructions)
    }
    expect(dut.io.dbg.get.accu, 0, "Accu shall be zero at the end of a test case.\n")
  }
}

object LipsiTester {
  def main(args: Array[String]): Unit = {
    println("Testing Lipsi")
     iotesters.Driver.execute(
       Array("--generate-vcd-output", "on",
         "--target-dir", "generated",
         "--top-name", "Lipsi"),
       () => new Lipsi(args(0), debug = false)) {
      c => new LipsiTester(c)
    }
  }
}
