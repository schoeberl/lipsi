/*
 * Copyright: 2017, Technical University of Denmark, DTU Compute
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * License: Simplified BSD License
 */

package lipsi.util

import scala.io.Source
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

object Assembler extends App {

  val prog = Array[Int](
    0xc7, 0x12, // ldi 0x12
    0xc0, 0x34, // addi 0x34
    0xc1, 0x12, // subi 0x12
    0xc4, 0xf0, // andi 0xf0
    0xc5, 0x03, // ori 0x03
    0xc6, 0xff, // xori 0xff
    0x82, // st r2
    0x00)

  // def getProgram() = Vec(prog.map(Bits(_)))

  def getProgram() = {
    assemble()
    Vec(prog.map(Bits(_)))
  }

  def assemble() = {
    val source = Source.fromFile("asm/test.asm")
    val program = List[Int]()
    
    def toInt(s: String): Int = {
      if (s.startsWith("0x")) {
        Integer.parseInt(s.substring(2), 16)
      } else {
        Integer.parseInt(s)
      }
      
    }
    
    for (line <- source.getLines()) {
      println(line)
      val tokens = line.trim.split(" ")
      println(s"length: ${tokens.length}")
      tokens.foreach(println)
      val x = tokens(0) match {
        case "addi" => (0xc0, toInt(tokens(1))) // not so elegant...
        case _ => println("Nothing")
      }
      
      x match {
        case (a: Int, b: Int) => println(s"Two ints $a $b")
        case _ => println("Something else")
      }
      println(x)
    }
  }
  
  assemble()
  
}
