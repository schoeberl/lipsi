/*
 * Copyright: 2017, Technical University of Denmark, DTU Compute
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * License: Simplified BSD License
 * 
 * Test Lipsi.
 */

package lipsi

import lipsi.sim._
import chisel3._
import chisel3.iotesters.PeekPokeTester

class LipsiCoSim(dut: Lipsi, arg0: String) extends PeekPokeTester(dut) {

    if (dut.debug == false) {
    println("Lipsi needs to be built in debug mode")
    fail
  }
  else {
    val lsim = new LipsiSim(arg0)

    var run = true
    var maxInstructions = 30
    while(run) {

      expect(dut.io.dbg.get.pc, lsim.pc, "PC shall be equal.\n")
      expect(dut.io.dbg.get.accu, lsim.accu, "Accu shall be equal.\n")
      
      step(1)
      lsim.step()
      maxInstructions -= 1
      run = peek(dut.io.dbg.get.exit) == 0 && maxInstructions > 0
    }
    expect(dut.io.dbg.get.accu, 0, "Accu shall be zero at the end of a test case.\n")
  }
}

object LipsiCoSim {
  def main(args: Array[String]): Unit = {
    println("Co-simulation of Lipsi")
    iotesters.Driver.execute(Array[String](), () => new Lipsi(args(0), debug = false)) {
      c => new LipsiCoSim(c, args(0))
    }

    /* Chisel 2
    chiselMainTest(Array("--genHarness", "--test", "--backend", "c",
      "--compile", "--vcd", "--targetDir", "generated"),
      () => Module(new Lipsi(args(0)))) {
        f => new LipsiCoSim(f, args(0))
      }
      */
  }
}
