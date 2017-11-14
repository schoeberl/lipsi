/*
 * Copyright: 2017, Technical University of Denmark, DTU Compute
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * License: Simplified BSD License
 * 
 * A software simulator of Lipsi, a very tiny processor.
 */

package lipsi.sim

import lipsi.util._

class LipsiSim(asm: String) {

  val prog = Assembler.getProgram(asm)
  // The complete processor state.
  // We ignore for now using Int instead of bytes.
  // We will mask out the bits when it matters.
  var pc = 0
  var accu = 0
  var mem = new Array[Int](512)

  var accuNext = 0
  var delayOne = false
  var delayUpdate = false
  var delayTwoUpdate = false
  var noPcIncr = false
  var doBranch = false

  var run = true

  for (i <- 0 until prog.length) mem(i) = prog(i)

  def alu(func: Int, op: Int): Int = {
    func match {
      case 0x0 => accu + op
      case 0x1 => accu - op
      case 0x2 => accu + op // TODO: carry
      case 0x3 => accu - op
      case 0x4 => accu & op
      case 0x5 => accu | op
      case 0x6 => accu ^ op
      case 0x7 => op
    }
  }

  def step(): Unit = {

    if (delayOne) {
      delayOne = false
    } else if (delayUpdate) {
      accu = accuNext
      delayUpdate = false
    } else if (delayTwoUpdate) {
      delayUpdate = true
      noPcIncr = true
      delayTwoUpdate = false
    } else {
      val instr = mem(pc)
      if ((instr & 0x80) == 0) {
        accuNext = alu((instr >> 4) & 0xf, mem((instr & 0x0f) + 256))
        delayUpdate = true
        noPcIncr = true
      } else {
        ((instr >> 4) & 0x7) match {
          case 0x0 => mem((instr & 0x0f) + 256) = accu
          case 0x1 =>
          case 0x2 => {
            accuNext = mem(mem((instr & 0x0f) + 256) + 256)
            delayTwoUpdate = true
            noPcIncr = true
          }
          case 0x3 => {
            mem(mem((instr & 0x0f) + 256) + 256) = accu
            delayOne = true
            noPcIncr = true
          }
          case 0x4 => {
            accuNext = alu(instr & 0x07, mem(pc + 1))
            delayUpdate = true
          }
          case 0x5 => {
            doBranch = (instr & 0x03) match {
              case 0x0 => true
              case 0x2 => accu == 0
              case 0x3 => accu != 0
            }
            delayOne = true
          }
          case 0x6 =>
          case 0x7 =>
        }
      }

    }

    if (noPcIncr) {
      noPcIncr = false
    } else {
      if (doBranch && !delayOne) {
        pc = mem(pc)
        doBranch = false
      } else {
        pc += 1
      }
    }
    run = pc < prog.length
  }
}

object LipsiSim extends App {

  val lsim = new LipsiSim(args(0))

  while (lsim.run) {
    lsim.step
    printf("(pc:0x%02x  0x%02x) ", lsim.pc, lsim.accu)
  }
  println

}