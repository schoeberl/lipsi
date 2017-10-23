/*
 * Copyright: 2017, Technical University of Denmark, DTU Compute
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * License: Simplified BSD License
 */

package lipsi.util

import Chisel._

object Assembler {
  
  def getProgram() = {
    val program = new Array[Bits](4)
    program(0) = Bits(0x00, 8)
    program(1) = Bits(0x01, 8)
    program(2) = Bits(0x01, 8)
    program(3) = Bits(0x00, 8)
    
    val rom = Vec(program)
    rom
  }
}