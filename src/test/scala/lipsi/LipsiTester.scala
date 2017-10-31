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

  for (i <- 0 until lipsi.util.Assembler.prog.length+3) {
    peek(dut.io.pc)
    peek(dut.io.acc)
    peek(dut.io.data)
    peek(dut.mem.io.rdAddr)
    peek(dut.stateReg)
    step(1)
  }
}

object LipsiTester {
  def main(args: Array[String]): Unit = {
    println("Testing Lipsi")
    chiselMainTest(Array("--genHarness", "--test", "--backend", "c",
      "--compile", "--vcd", "--targetDir", "generated"),
      () => Module(new Lipsi())) {
        f => new LipsiTester(f)
      }
  }
}
