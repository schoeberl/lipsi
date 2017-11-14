/*
 * Copyright: 2017, Technical University of Denmark, DTU Compute
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * License: Simplified BSD License
 * 
 * Test Lipsi.
 */

package lipsi

import Chisel._

class LipsiTester(dut: Lipsi) extends Tester(dut) {

  var run = true
  var maxInstructions = 30
  while(run) {
    peek(dut.pcReg)
    peek(dut.accuReg)
    peek(dut.mem.io.rdAddr)
    peek(dut.stateReg)
    step(1)
    maxInstructions -= 1
    run = peek(dut.exitReg) == 0 && maxInstructions > 0
    // poke(dut.io.din, maxInstructions)
  }
  expect(dut.accuReg, 0, "Accu shall be zero at the end of a test case.\n")
}

object LipsiTester {
  def main(args: Array[String]): Unit = {
    println("Testing Lipsi")
    chiselMainTest(Array("--genHarness", "--test", "--backend", "c",
      "--compile", "--vcd", "--targetDir", "generated"),
      () => Module(new Lipsi(args(0)))) {
        f => new LipsiTester(f)
      }
  }
}
