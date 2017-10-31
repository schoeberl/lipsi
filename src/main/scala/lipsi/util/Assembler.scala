/*
 * Copyright: 2017, Technical University of Denmark, DTU Compute
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * License: Simplified BSD License
 */

package lipsi.util

import Chisel._

/*

Instruction encoding:

0fff rrrr ALU register
1000 rrrr st rx
1001 rrrr brl rx
1010 rrrr ld (rx)
1011 rrrr st (rx)
1100 -fff + nnnn nnnn ALU imm
1101 -ccc + aaaa aaaa br, br cond
1110 --ff ALU shift
1111 aaaa IO

ALU function:

add, sub, adc, sbb, and, or, xor, ld

*/


object Assembler {

  val prog = Array[Int](
    0xc7, 0x12, // ldi 0x12
    0xc0, 0x34, // addi 0x34
    0xc1, 0x12, // subi 0x12
    0xc4, 0xf0, // andi 0xf0
    0xc5, 0x03, // ori 0x03
    0xc6, 0xff, // xori 0xff
    0x82,       // st r2
    0x00)

  def getProgram() = Vec(prog.map(Bits(_)))
}