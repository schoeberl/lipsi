/*
 * Copyright: 2017, Technical University of Denmark, DTU Compute
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * License: Simplified BSD License
 * 
 * Test Lipsi.
 */

package lipsi

import lipsi.sim._
import Chisel._

class LipsiCoSim(dut: Lipsi, arg0: String) extends Tester(dut) {

  val lsim = new LipsiSim(arg0)

  var run = true
  var maxInstructions = 30
  while(run) {

    expect(dut.pcReg, lsim.pc, "PC shall be equal.\n")
    expect(dut.accuReg, lsim.accu, "Accu shall be equal.\n")
    
    step(1)
    lsim.step()
    maxInstructions -= 1
    run = peek(dut.exitReg) == 0 && maxInstructions > 0
  }
  expect(dut.io.acc, 0, "Accu shall be zero at the end of a test case.\n")
}

object LipsiCoSim {
  def main(args: Array[String]): Unit = {
    println("Co-simulation of Lipsi")
        
    chiselMainTest(Array("--genHarness", "--test", "--backend", "c",
      "--compile", "--vcd", "--targetDir", "generated"),
      () => Module(new Lipsi(args(0)))) {
        f => new LipsiCoSim(f, args(0))
      }
  }
}
