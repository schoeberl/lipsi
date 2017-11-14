/*
 * Copyright: 2017, Technical University of Denmark, DTU Compute
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * License: Simplified BSD License
 */

package lipsi.util

import scala.io.Source
//import Chisel._

object Assembler {

  val prog = Array[Int](
    0xc7, 0x12, // ldi 0x12
    0xc0, 0x34, // addi 0x34
    0xc1, 0x12, // subi 0x12
    0xc4, 0xf0, // andi 0xf0
    0xc5, 0x03, // ori 0x03
    0xc6, 0xff, // xori 0xff
    0x82, // st r2
    0x00)

  // collect destination addresses in first pass
  val symbols = collection.mutable.Map[String, Int]()

  def getProgramFix() = prog

  def getProgram(prog: String) = assemble(prog)

  def assemble(prog: String): Array[Int] = {
    assemble(prog, false)
    assemble(prog, true)
  }

  def assemble(prog: String, pass2: Boolean): Array[Int] = {

    val source = Source.fromFile(prog)
    var program = List[Int]()
    var pc = 0

    def toInt(s: String): Int = {
      if (s.startsWith("0x")) {
        Integer.parseInt(s.substring(2), 16)
      } else {
        Integer.parseInt(s)
      }
    }

    def regNumber(s: String): Int = {
      assert(s.startsWith("r"))
      s.substring(1).toInt
    }

    def regIndirect(s: String): Int = {
      assert(s.startsWith("(r"))
      assert(s.endsWith(")"))
      s.substring(2, s.length - 1).toInt
    }

    for (line <- source.getLines()) {
      if (!pass2) println(line)
      val tokens = line.trim.split(" ")
      // println(s"length: ${tokens.length}")
      // tokens.foreach(println)
      val Pattern = "(.*:)".r
      val instr = tokens(0) match {
        case "#" => // comment
        case Pattern(l) => if (!pass2) symbols += (l.substring(0, l.length - 1) -> pc)
        case "add" => 0x00 + regNumber(tokens(1))
        case "sub" => 0x10 + regNumber(tokens(1))
        case "adc" => 0x20 + regNumber(tokens(1))
        case "sbb" => 0x30 + regNumber(tokens(1))
        case "and" => 0x40 + regNumber(tokens(1))
        case "or" => 0x50 + regNumber(tokens(1))
        case "xor" => 0x60 + regNumber(tokens(1))
        case "ld" => 0x70 + regNumber(tokens(1))
        case "addi" => (0xc0, toInt(tokens(1)))
        case "subi" => (0xc1, toInt(tokens(1)))
        case "adci" => (0xc2, toInt(tokens(1)))
        case "sbbi" => (0xc3, toInt(tokens(1)))
        case "andi" => (0xc4, toInt(tokens(1)))
        case "ori" => (0xc5, toInt(tokens(1)))
        case "xori" => (0xc6, toInt(tokens(1)))
        case "ldi" => (0xc7, toInt(tokens(1)))
        case "st" => 0x80 + regNumber(tokens(1))
        case "ldind" => 0xa0 + regIndirect(tokens(1))
        case "stind" => 0xb0 + regIndirect(tokens(1))
        case "br" => (0xd0, if (pass2) symbols(tokens(1)) else 0)
        case "brz" => (0xd2, if (pass2) symbols(tokens(1)) else 0)
        case "brnz" => (0xd3, if (pass2) symbols(tokens(1)) else 0)
        case "io" => 0xf0 + toInt(tokens(1))
        case "exit" => (0xff)
        case "" => // println("Empty line")
        case t: String => throw new Exception("Assembler error: unknown instruction")
        case _ => throw new Exception("Assembler error")
      }
      // println(instr)

      instr match {
        case (a: Int) => {
          program = a :: program
          pc += 1
        }
        case (a: Int, b: Int) => {
          program = a :: program
          program = b :: program
          pc += 2
        }
        case _ => // println("Something else")
      }
    }
    val finalProg = program.reverse.toArray
    if (!pass2) {
      println(s"The program:")
      finalProg.foreach(printf("0x%02x ", _))
      println()
    }
    finalProg
  }
}
