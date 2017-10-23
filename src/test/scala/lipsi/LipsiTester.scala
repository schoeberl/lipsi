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

  for (i <- 0 until 16) {
    step(1)
    peek(dut.io.pc)
    peek(dut.io.acc)
  }
}

object LipsiTester {
  def main(args: Array[String]): Unit = {
    println("Testing Lipsi")
    chiselMainTest(Array("--genHarness", "--test", "--backend", "c",
      "--compile", "--targetDir", "generated"),
      () => Module(new Lipsi())) {
        f => new LipsiTester(f)
      }
  }
}
