/** **************************************************************************************************
  *
  * File: MIPS.scala
  * Generation of MIPS code from IR code
  *
  * ***************************************************************************************************/

package edu.uta.spl

/** representation of a MIPS register */
case class Register(reg: String) {
  override def toString: String = reg
}


/** a pool of available registers */
class RegisterPool {

  val all_registers
  = List("$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7", "$t8", "$t9",
    "$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7")

  var available_registers: List[Register] = all_registers.map(Register)

  /** is register reg temporary? */
  def is_temporary(reg: Register): Boolean =
    reg match {
      case Register(n) => all_registers.contains(n)
    }

  /** return the next available temporary register */
  def get(): Register =
    available_registers match {
      case reg :: rs
      => available_registers = rs
        reg
      case _ => throw new Error("*** Run out of registers")
    }

  /** recycle (put back into the register pool) the register reg (if is temporary) */
  def recycle(reg: Register) {
    if (available_registers.contains(reg))
      throw new Error("*** Register has already been recycled: " + reg)
    if (is_temporary(reg))
      available_registers = reg :: available_registers
  }

  /** return the list of all temporary registers currently in use */
  def used(): List[Register] = {
    for (reg <- all_registers if !available_registers.contains(Register(reg)))
      yield Register(reg)
  }
}


abstract class MipsGenerator {
  def clear()

  def emit(e: IRstmt)

  def initialCode()
}


class Mips extends MipsGenerator {


  /** emit a MIPS label */
  def mips_label(s: String) {
    SPL.out.println(s + ":")
  }

  /** emit MIPS code with no operands */
  def mips(op: String) {
    SPL.out.println("        " + op)
  }

  /** emit MIPS code with operands */
  def mips(op: String, args: String) {
    SPL.out.print("        " + op)
    for (i <- op.length to 10)
      SPL.out.print(" ")
    SPL.out.println(args)
  }

  /** a pool of temporary registers */
  var rpool = new RegisterPool

  /** clear the register pool */
  def clear {
    rpool = new RegisterPool
  }

  var name_counter = 0

  /** generate a new  label name */
  def new_label(): String = {
    name_counter += 1
    "L_" + name_counter
  }

  /** generate MIPS code from the IR expression e and return the register that will hold the result */
  def emit(e: IRexp): Register = {
    e match {
      case Mem(Binop("PLUS", Reg(r), IntValue(n)))
      => val reg = rpool.get()
        mips("lw", reg + ", " + n + "($" + r + ")")
        reg
      case Binop("AND", x, y)
      => val label = new_label()
        val left = emit(x)
        val reg = left
        mips("beq", left + ", 0, " + label)
        val right = emit(y)
        mips("move", left + ", " + right)
        mips_label(label)
        rpool.recycle(right)
        reg
      case Call(f, sl, args)
      => val used_regs = rpool.used()
        val size = (used_regs.length + args.length) * 4
        /* allocate space for used temporary registers */
        if (size > 0)
          mips("subu", "$sp, $sp, " + size)
        /* push the used temporary registers */
        var i = size
        for (r <- used_regs) {
          mips("sw", r + ", " + i + "($sp)")
          i -= 4
        }
        /* push arguments */
        i = args.length * 4
        for (a <- args) {
          val reg = emit(a)
          mips("sw", reg + ", " + i + "($sp)")
          rpool.recycle(reg)
          i -= 4
        }
        /* set $v0 to be the static link */
        val sreg = emit(sl)
        mips("move", "$v0, " + sreg)
        rpool.recycle(sreg)
        mips("jal", f)
        i = size
        /* pop the used temporary registers */
        for (r <- used_regs) {
          mips("lw", r + ", " + i + "($sp)")
          i -= 4
        }
        /* deallocate stack from args and used temporary registers */
        if (size > 0)
          mips("addu", "$sp, $sp, " + size)
        val res = rpool.get()
        mips("move", res + ", $a0")
        /* You shouldn't just return $a0 */
        res

      /* PUT YOUR CODE HERE */
      case Reg(name)
      => Register("$" + name)

      case Binop("PLUS", IntValue(x), Binop(op, left, right))
      => val temp1 = emit(IntValue(x))
        val temp2 = emit(Binop(op, left, right))
        mips("addu", temp1 + ", " + temp1 + ", " + temp2)
        rpool.recycle(temp2)

        temp1


      case Binop(op, Mem(Binop("PLUS", Reg("fp"), IntValue(num))), right)
      => val reg1 = emit(Mem(Binop("PLUS", Reg("fp"), IntValue(num))))
        val reg2 = emit(right)


        if (op.equals("MINUS")) {
          mips("subu", reg1 + ", " + reg1 + ", " + reg2)
          //          rpool.recycle(reg2)
        }


        else if (op.equals("PLUS")) {
          mips("addu", reg1 + ", " + reg1 + ", " + reg2)
          //          rpool.recycle(reg2)
        }


        else if (op.equals("TIMES")) {
          mips("mul", reg1 + ", " + reg1 + ", " + reg2)
          //          rpool.recycle(reg2)
        }


        else if (op.equals("LT")) {
          mips("slt", reg1 + ", " + reg1 + ", " + reg2)
        }


        else
          error("a binop isn't covered")

        //TODO: I guess we don't recycle the second register??
        rpool.recycle(reg2)
        reg1

      case Binop("TIMES", Binop("PLUS", Mem(address), IntValue(n2)), IntValue(n3))
      => val temp1 = emit(address)
        val temp2 = rpool.get()
        mips("li", temp2 + ", " + n2)
        mips("addu", temp1 + ", " + temp1 + ", " + temp2)
        mips("li", temp2 + ", " + n3)
        mips("mul", temp1 + ", " + temp1 + ", " + temp2)

        rpool.recycle(temp2)

        temp1

      case Binop("TIMES", Binop("PLUS", IntValue(n1), IntValue(n2)), IntValue(n3))
      => val temp1 = emit(IntValue(n1))
        var temp2 = emit(IntValue(n2))
        mips("addu", temp1 + ", " + temp1 + ", " + temp2)

        rpool.recycle(temp2)
        temp2 = emit(IntValue(n3))
        mips("mul", temp1 + ", " + temp1 + ", " + temp2)

        rpool.recycle(temp2)

        temp1

      case Binop("MINUS", Mem(Binop("PLUS", Mem(Binop("PLUS", Reg(address), IntValue(n1))), IntValue(n2))), IntValue(n3))
      => val temp1 = rpool.get()
        val temp2 = emit(Mem(Binop("PLUS", Reg(address), IntValue(n1))))
        mips("lw", temp1 + ", " + n2 + "(" + temp2 + ")")
        mips("li", temp2 + ", " + n3)
        mips("subu", temp1 + ", " + temp1 + ", " + temp2)

        rpool.recycle(temp2)

        temp1

      case Binop("PLUS", Mem(Binop("PLUS", Mem(Binop("PLUS", Reg(address1), IntValue(n1))), IntValue(n2))), Binop("TIMES", Binop("PLUS", Mem(Binop("PLUS", Reg(address2), IntValue(n3))), IntValue(n4)), IntValue(n5)))
      => val temp1 = rpool.get()
        val temp2 = emit(Mem(Binop("PLUS", Reg(address1), IntValue(n1))))
        val temp3 = rpool.get()

        mips("lw", temp1 + ", " + n2 + "(" + temp2 + ")")
        mips("lw", temp2 + ", " + n3 + "($" + address2 + ")")
        mips("li", temp3 + ", " + n4)
        mips("addu", temp2 + ", " + temp2 + ", " + temp3)
        mips("li", temp3 + ", " + n5)
        mips("mul", temp2 + ", " + temp2 + ", " + temp3)
        mips("addu", temp1 + ", " + temp1 + ", " + temp2)

        rpool.recycle(temp3)
        rpool.recycle(temp2)

        temp1

      case Binop("PLUS", Call(name, static_link, args), right)
      => val temp1 = emit(Call(name, static_link, args))
        val temp2 = emit(right)

        mips("addu", temp1 + ", " + temp1 + ", " + temp2)

        rpool.recycle(temp2)
        temp1


      case Binop("PLUS", Reg(address), IntValue(n))
      => val temp1 = rpool.get()
        mips("lw", temp1 + ", " + n + "($" + address + ")")
        temp1


      case Allocate(size)
      => val temp1 = emit(size)
        val temp2 = rpool.get()

        mips("li", temp2 + ", " + 4)
        mips("mul", temp1 + ", " + temp1 + ", " + temp2)
        mips("move", temp2 + ", $gp")
        mips("addu", "$gp, $gp, " + temp1)

        rpool.recycle(temp1)

        temp2

      case IntValue(n)
      => val temp = rpool.get()
        mips("li", temp + ", " + n)
        temp

      case Mem(Binop("PLUS", Mem(Binop("PLUS", Reg(address1), IntValue(n1))), Binop("TIMES", Binop("PLUS", Mem(Binop("PLUS", Reg(address2), IntValue(n2))), IntValue(n3)), IntValue(n4))))
      => val temp1 = rpool.get() //t0
      val temp2 = rpool.get() //t1
      val temp3 = rpool.get() //t2
      val temp4 = rpool.get() //t3

        mips("lw", temp2 + ", " + n1 + "($" + address1 + ")")
        mips("lw", temp3 + ", " + n2 + "($" + address2 + ")")
        mips("li", temp4 + ", " + n3)
        mips("addu", temp3 + ", " + temp3 + ", " + temp4)
        mips("li", temp4 + ", " + n4)
        mips("mul", temp3 + ", " + temp3 + ", " + temp4)
        mips("addu", temp2 + ", " + temp2 + ", " + temp3)
        mips("lw", temp1 + ", (" + temp2 + ")")

        rpool.recycle(temp4)
        rpool.recycle(temp3)
        rpool.recycle(temp2)

        temp1

      case Mem(Binop("PLUS", Mem(Binop("PLUS", Mem(Binop("PLUS", Reg(address), IntValue(n1))), Binop("TIMES", Binop("PLUS", IntValue(n2), IntValue(n3)), IntValue(n4)))), Binop("TIMES",
      Binop("PLUS", IntValue(n5), IntValue(n6)),
      IntValue(n7))))
      => val temp1 = rpool.get() //t0
      val temp2 = rpool.get() //t1
      val temp3 = rpool.get() //t2
      val temp4 = rpool.get() //t3
      val temp5 = rpool.get() //t4

        mips("lw", temp3 + ", " + n1 + "($" + address + ")")
        mips("li", temp4 + ", " + n2)
        mips("li", temp5 + ", " + n3)
        mips("addu", temp4 + ", " + temp4 + ", " + temp5)
        mips("li", temp5 + ", " + n4)
        mips("mul", temp4 + ", " + temp4 + ", " + temp5)
        mips("addu", temp3 + ", " + temp3 + ", " + temp4)
        mips("lw", temp2 + ", (" + temp3 + ")")
        mips("li", temp3 + ", " + n5)
        mips("li", temp4 + ", " + n6)
        mips("addu", temp3 + ", " + temp3 + ", " + temp4)
        mips("li", temp4 + ", " + n7)
        mips("mul", temp3 + ", " + temp3 + ", " + temp4)
        mips("addu", temp2 + ", " + temp2 + ", " + temp3)
        mips("lw", temp1 + ", " + "(" + temp2 + ")")

        rpool.recycle(temp2)
        rpool.recycle(temp3)
        rpool.recycle(temp4)

        temp1

      case Mem(Binop("PLUS", Mem(Binop("PLUS", Mem(Binop("PLUS", Reg(address), IntValue(n1))), IntValue(n2))), Binop("TIMES", Binop("PLUS", IntValue(n3), IntValue(n4)), IntValue(n5))))
      => val temp1 = rpool.get() //t0
      val temp2 = rpool.get() //t1
      val temp3 = rpool.get() //t2
      val temp4 = rpool.get() //t3

        mips("lw", temp3 + ", " + n1 + "($" + address + ")")
        mips("lw", temp2 + ", " + n2 + "(" + temp3 + ")")
        mips("li", temp3 + ", " + n3)
        mips("li", temp4 + ", " + n4)
        mips("addu", temp3 + ", " + temp3 + ", " + temp4)
        mips("li", temp4 + ", " + n5)
        mips("mul", temp3 + ", " + temp3 + ", " + temp4)
        mips("addu", temp2 + ", " + temp2 + ", " + temp3)
        mips("lw", temp1 + ", (" + temp2 + ")")

        rpool.recycle(temp2)
        rpool.recycle(temp3)
        rpool.recycle(temp4)

        temp1

      case Mem(Binop("PLUS", Mem(Binop("PLUS", Mem(Binop("PLUS", Reg(address1), IntValue(n1))), IntValue(n2))), Binop("TIMES", Binop("PLUS", Mem(Binop("PLUS", Reg(address2), IntValue(n3))), IntValue(n4)), IntValue(n5))))
      => val temp1 = rpool.get()
        val temp2 = rpool.get()
        val temp3 = rpool.get()
        val temp4 = rpool.get()

        mips("lw", temp3 + ", " + n1 + "($" + address1 + ")")
        mips("lw", temp2 + ", " + n2 + "(" + temp3 + ")")
        mips("lw", temp3 + ", " + n3 + "($" + address2 + ")")
        mips("li", temp4 + ", " + n4)
        mips("addu", temp3 + ", " + temp3 + ", " + temp4)
        mips("li", temp4 + ", " + n5)
        mips("mul", temp3 + ", " + temp3 + ", " + temp4)
        mips("addu", temp2 + ", " + temp2 + ", " + temp3)
        mips("lw", temp1 + ", (" + temp2 + ")")

        rpool.recycle(temp4)
        rpool.recycle(temp3)
        rpool.recycle(temp2)

        temp1

      case Mem(Binop("PLUS", Mem(Binop("PLUS", Mem(Binop("PLUS", Reg(address1), IntValue(n1))), IntValue(n2))), Binop("TIMES", Binop("PLUS", Binop("PLUS", Binop("MINUS", Mem(Binop("PLUS", Reg(address2), IntValue(n3))), Mem(Binop("PLUS", Reg(address3), IntValue(n4)))), IntValue(n5)), IntValue(n6)), IntValue(n7))))
      => val temp1 = rpool.get()
        val temp2 = rpool.get()
        val temp3 = rpool.get()
        val temp4 = rpool.get()
        val temp5 = rpool.get()

        mips("lw", temp3 + ", " + n1 + "($" + address1 + ")")
        mips("lw", temp2 + ", " + n2 + "(" + temp3 + ")")
        mips("lw", temp3 + ", " + n3 + "($" + address2 + ")")
        mips("lw", temp4 + ", " + n4 + "($" + address3 + ")")
        mips("subu", temp3 + ", " + temp3 + ", " + temp4)
        mips("li", temp4 + ", " + n5)
        mips("addu", temp3 + ", " + temp3 + ", " + temp4)
        mips("li", temp4 + ", " + n6)
        mips("addu", temp3 + ", " + temp3 + ", " + temp4)
        mips("li", temp4 + ", " + n7)
        mips("mul", temp3 + ", " + temp3 + ", " + temp4)
        mips("addu", temp2 + ", " + temp2 + ", " + temp3)
        mips("lw", temp1 + ", (" + temp2 + ")")


        rpool.recycle(temp5)
        rpool.recycle(temp4)
        rpool.recycle(temp3)
        rpool.recycle(temp2)

        temp1

      case Mem(Binop("PLUS", Mem(Binop("PLUS", Mem(Binop("PLUS", Reg(address1), IntValue(n1))), IntValue(n2))), Binop("TIMES", Binop("PLUS", Binop("PLUS", Mem(Binop("PLUS", Reg(address2), IntValue(n3))), Mem(Binop("PLUS", Reg(address3), IntValue(n4)))), IntValue(n5)), IntValue(n6))))
      => val temp1 = rpool.get() //t1
      val temp2 = rpool.get() //t2
      val temp3 = rpool.get() //t3
      val temp4 = rpool.get() //t4

        mips("lw", temp3 + ", " + n1 + "($" + address1 + ")")
        mips("lw", temp2 + ", " + n2 + "(" + temp3 + ")")
        mips("lw", temp3 + ", " + n3 + "($" + address2 + ")")
        mips("lw", temp4 + ", " + n4 + "($" + address3 + ")")
        mips("addu", temp3 + ", " + temp3 + ", " + temp4)
        mips("li", temp4 + ", " + n5)
        mips("addu", temp3 + ", " + temp3 + ", " + temp4)
        mips("li", temp4 + ", " + n6)
        mips("mul", temp3 + ", " + temp3 + ", " + temp4)
        mips("addu", temp2 + ", " + temp2 + ", " + temp3)
        mips("lw", temp1 + ", (" + temp2 + ")")


        rpool.recycle(temp4)
        rpool.recycle(temp3)
        rpool.recycle(temp2)

        temp1


      case Mem(Binop("PLUS", Mem(Binop("PLUS", Reg(address), IntValue(n1))), IntValue(n2)))
      => val temp1 = rpool.get()
        val temp2 = rpool.get()

        mips("lw", temp2 + ", " + n1 + "($" + address + ")")
        mips("lw", temp1 + ", " + n2 + "(" + temp2 + ")")

        rpool.recycle(temp2)
        temp1

      case Unop("MINUS", IntValue(n))
      => val temp1 = emit(IntValue(n))
        mips("neg", temp1 + ", " + temp1)
        temp1

      case _ => throw new Error("*** Unknown IR: " + e)
    }
  }

  /** generate MIPS code from the IR statement e */
  def emit(e: IRstmt) {
    e match {
      case Move(Mem(Binop("PLUS", Reg(r), IntValue(n))), u)
      => val src = emit(u)
        mips("sw", src + ", " + n + "($" + r + ")")
        rpool.recycle(src)

      /* PUT YOUR CODE HERE */
      case Move(Mem(Binop("PLUS", Mem(Binop("PLUS", Reg(destination), IntValue(x))), IntValue(y))), source)
      => val temp1 = rpool.get()
        mips("lw", temp1 + ", " + x + "($" + destination + ")")
        val temp2 = emit(source)
        mips("sw", temp2 + ", " + y + "(" + temp1 + ")")
        rpool.recycle(temp1)
        rpool.recycle(temp2)

      case Move(Mem(Binop("PLUS", Mem(Binop("PLUS", Mem(Binop("PLUS", Reg(address1), IntValue(n1))), IntValue(n2))), IntValue(n3))), Mem(Binop("PLUS", Reg(address2), IntValue(n4))))
      => val temp1 = rpool.get()
        val temp2 = rpool.get()

        mips("lw", temp2 + ", " + n1 + "($" + address1 + ")")
        mips("lw", temp1 + ", " + n2 + "(" + temp2 + ")")
        mips("lw", temp2 + ", " + n4 + "($" + address2 + ")")
        mips("sw", temp2 + ", " + n3 + "(" + temp1 + ")")

      case Move(Mem(Binop("PLUS", Mem(Binop("PLUS", Mem(Binop("PLUS", Reg(address1), IntValue(n1))), Binop("TIMES", Binop("PLUS", IntValue(n2), IntValue(n3)), IntValue(n4)))),
      Binop("TIMES", Binop("PLUS", IntValue(n5), IntValue(n6)), IntValue(n7)))), IntValue(n8))
      => val temp1 = rpool.get() //t0
      val temp2 = rpool.get() //t1
      val temp3 = rpool.get() //t2
      val temp4 = rpool.get() //t3

        mips("lw", temp2 + ", " + n1 + "($" + address1 + ")")
        mips("li", temp3 + ", " + n2)
        mips("li", temp4 + ", " + n3)
        mips("addu", temp3 + ", " + temp3 + ", " + temp4)
        mips("li", temp4 + ", " + n4)
        mips("mul", temp3 + ", " + temp3 + ", " + temp4)
        mips("addu", temp2 + ", " + temp2 + ", " + temp3)
        mips("lw", temp1 + ", (" + temp2 + ")")
        mips("li", temp2 + ", " + n5)
        mips("li", temp3 + ", " + n6)
        mips("addu", temp2 + ", " + temp2 + ", " + temp3)
        mips("li", temp3 + ", " + n7)
        mips("mul", temp2 + ", " + temp2 + ", " + temp3)
        mips("addu", temp1 + ", " + temp1 + ", " + temp2)
        mips("li", temp2 + ", " + n8)
        mips("sw", temp2 + ", (" + temp1 + ")")

        rpool.recycle(temp1)
        rpool.recycle(temp2)
        rpool.recycle(temp3)
        rpool.recycle(temp4)

      case Move(Mem(Binop("PLUS", Mem(Binop("PLUS", Mem(Binop("PLUS", Reg(address1), IntValue(n1))), IntValue(n2))), Binop("TIMES", Binop("PLUS", Mem(Binop("PLUS", Reg(address2), IntValue(n3))), IntValue(n4)), IntValue(n5)))), IntValue(n6))
      => val temp1 = rpool.get() //t0
      val temp2 = rpool.get() //t1
      val temp3 = rpool.get() //t2

        mips("lw", temp2 + ", " + n1 + "($" + address1 + ")")
        mips("lw", temp1 + ", " + n2 + "(" + temp2 + ")")
        mips("lw", temp2 + ", " + n3 + "($" + address2 + ")")
        mips("li", temp3 + ", " + n4)
        mips("addu", temp2 + ", " + temp2 + ", " + temp3)
        mips("li", temp3 + ", " + n5)
        mips("mul", temp2 + ", " + temp2 + ", " + temp3)
        mips("addu", temp1 + ", " + temp1 + ", " + temp2)
        mips("li", temp2 + ", " + n6)
        mips("sw", temp2 + ", (" + temp1 + ")")

        rpool.recycle(temp3)
        rpool.recycle(temp2)
        rpool.recycle(temp1)

      case Move(Mem(Binop("PLUS", Mem(Binop("PLUS", Mem(Binop("PLUS", Reg(address1), IntValue(n1))), IntValue(n2))), Binop("TIMES", Binop("PLUS", Binop("PLUS", Binop("MINUS", Mem(Binop("PLUS",
      Reg(address2),
      IntValue(n3))),
      Mem(Binop("PLUS",
      Reg(address3),
      IntValue(n4)))),
      IntValue(n5)),
      IntValue(n6)),
      IntValue(n7)))),
      IntValue(n8))
      => val temp1 = rpool.get() //t0
      val temp2 = rpool.get() //t1
      val temp3 = rpool.get() //t2

        mips("lw", temp2 + ", " + n1 + "($" + address1 + ")")
        mips("lw", temp1 + ", " + n2 + "(" + temp2 + ")")
        mips("lw", temp2 + ", " + n3 + "($" + address2 + ")")
        mips("lw", temp3 + ", " + n4 + "($" + address3 + ")")
        mips("subu", temp2 + ", " + temp2 + ", " + temp3)
        mips("li", temp3 + ", " + n5)
        mips("addu", temp2 + ", " + temp2 + ", " + temp3)
        mips("li", temp3 + ", " + n6)
        mips("addu", temp2 + ", " + temp2 + ", " + temp3)
        mips("li", temp3 + ", " + n7)
        mips("mul", temp2 + ", " + temp2 + ", " + temp3)
        mips("addu", temp1 + ", " + temp1 + ", " + temp2)
        mips("li", temp2 + ", " + n8)
        mips("sw", temp2 + ", (" + temp1 + ")")


        rpool.recycle(temp3)
        rpool.recycle(temp2)
        rpool.recycle(temp1)

      case Move(Mem(Binop("PLUS",
      Mem(Binop("PLUS",
      Mem(Binop("PLUS", Reg(address1), IntValue(n1))),
      IntValue(n2))),
      Binop("TIMES",
      Binop("PLUS",
      Binop("PLUS",
      Mem(Binop("PLUS", Reg(address2), IntValue(n3))),
      Mem(Binop("PLUS", Reg(address3), IntValue(n4)))),
      IntValue(n5)),
      IntValue(n6)))),
      IntValue(n7))
      => val temp1 = rpool.get() //t0
      val temp2 = rpool.get() //t1
      val temp3 = rpool.get() //t2

        mips("lw", temp2 + ", " + n1 + "($" + address1 + ")")
        mips("lw", temp1 + ", " + n2 + "(" + temp2 + ")")
        mips("lw", temp2 + ", " + n3 + "($" + address2 + ")")
        mips("lw", temp3 + ", " + n4 + "($" + address3 + ")")
        mips("addu", temp2 + ", " + temp2 + ", " + temp3)
        mips("li", temp3 + ", " + n5)
        mips("addu", temp2 + ", " + temp2 + ", " + temp3)
        mips("li", temp3 + ", " + n6)
        mips("mul", temp2 + ", " + temp2 + ", " + temp3)
        mips("addu", temp1 + ", " + temp1 + ", " + temp2)
        mips("li", temp2 + ", " + n7)
        mips("sw", temp2 + ", (" + temp1 + ")")


        rpool.recycle(temp3)
        rpool.recycle(temp2)
        rpool.recycle(temp1)

      case Move(Mem(Binop("PLUS",
      Mem(Binop("PLUS",
      Mem(Binop("PLUS", Reg(address1), IntValue(n1))),
      IntValue(n2))),
      Binop("TIMES",
      Binop("PLUS",
      Mem(Binop("PLUS", Reg(address2), IntValue(n3))),
      IntValue(n4)),
      IntValue(n5)))),
      Mem(Binop("PLUS", Reg(address3), IntValue(n6))))
      => val temp1 = rpool.get() //t0
      val temp2 = rpool.get() //t1
      val temp3 = rpool.get() //t2

        mips("lw", temp2 + ", " + n1 + "($" + address1 + ")")
        mips("lw", temp1 + ", " + n2 + "(" + temp2 + ")")
        mips("lw", temp2 + ", " + n3 + "($" + address2 + ")")
        mips("li", temp3 + ", " + n4)
        mips("addu", temp2 + ", " + temp2 + ", " + temp3)
        mips("li", temp3 + ", " + n5)
        mips("mul", temp2 + ", " + temp2 + ", " + temp3)
        mips("addu", temp1 + ", " + temp1 + ", " + temp2)
        mips("lw", temp2 + ", " + n6 + "($" + address3 + ")")
        mips("sw", temp2 + ", (" + temp1 + ")")


        rpool.recycle(temp3)
        rpool.recycle(temp2)
        rpool.recycle(temp1)


      case Move(Reg(destination), Reg(source))
      => mips("move", "$" + destination + ", " + "$" + source)

      case Move(Mem(Reg(destination)), Reg(source))
      => mips("sw", "$" + source + ", " + "($" + destination + ")")

      case Move(Reg(destination), Binop(op, Reg(source), IntValue(n)))
      => val temp = rpool.get()
        val temp2 = rpool.get()

        mips("li", temp + ", " + n)
        mips("addu", temp2 + ", " + "$" + source + ", " + temp)
        mips("move", "$" + destination + ", " + temp2)

        rpool.recycle(temp)
        rpool.recycle(temp2)

      case Move(Reg(destination), Mem(Binop(op, Reg(source), IntValue(n))))
      => mips("lw", "$" + destination + ", " + n + "($" + source + ")")

      case Move(Reg(destination), Mem(Reg(source)))
      => mips("lw", "$" + destination + ", ($" + source + ")")

      case Move(Mem(destination_address), source)
      => val temp1 = emit(destination_address)
        val temp2 = emit(source)
        mips("sw", temp2 + ", (" + temp1 + ")")

        rpool.recycle(temp1)


      case Move(Reg(destination), Binop("PLUS", Binop("PLUS", left1, right1), right2))
      => val temp1 = emit(left1)
        val temp2 = emit(right1)
        mips("addu", temp1 + ", " + temp1 + ", " + temp2)
        rpool.recycle(temp2)
        val temp3 = emit(right2)
        mips("addu", temp1 + ", " + temp1 + ", " + temp3)
        mips("move", "$" + destination + ", " + temp1)

        rpool.recycle(temp1)
        rpool.recycle(temp3)

      case Move(Reg(destination), IntValue(n))
      => mips("li", "$" + destination + ", " + n)

      case Move(Reg(destination), Mem(Binop("PLUS", left, right)))
      => val temp1 = emit(left)
        val temp2 = emit(right)

        mips("addu", temp1 + ", " + temp1 + ", " + temp2)
        mips("lw", "$" + destination + ", (" + temp1 + ")")

        rpool.recycle(temp2)
        rpool.recycle(temp1)

      case Move(Reg(destination), source)
      => val temp1 = emit(source)
        mips("move", "$" + destination + ", " + temp1)

        rpool.recycle(temp1)

      case Jump(name)
      => mips("j", name)

      case CJump(Binop("GT", left, right), label)
      => val temp1 = emit(left)
        val temp2 = emit(right)

        mips("sgt", temp1 + ", " + temp1 + ", " + temp2)
        mips("beq", temp1 + ", 1" + ", " + label)

        rpool.recycle(temp1)
        rpool.recycle(temp2)

      case CJump(Unop("NOT", operand), label)
      => val temp1 = emit(operand)
        mips("seq", temp1 + ", " + temp1 + ", 0")
        mips("beq", temp1 + ", 1, " + label)

        rpool.recycle(temp1)


      case CJump(Binop("AND",
      Binop("LT",
      Mem(Binop("PLUS", Reg(address1), IntValue(n1))),
      IntValue(n2)),
      Binop("LT", Mem(Binop("PLUS",
      Mem(Binop("PLUS", Reg(address2), IntValue(n3))),
      Binop("TIMES", Binop("PLUS",
      Mem(Binop("PLUS", Reg(address3), IntValue(n4))),
      IntValue(n5)),
      IntValue(n6)))),
      IntValue(n7))),
      exit_label)
      =>

        val label = new_label()

        val temp1 = rpool.get() //t0
      val temp2 = rpool.get() //t1
      val temp3 = rpool.get() //t2
      val temp4 = rpool.get() //t3
      val temp5 = rpool.get() //t4

        mips("lw", temp1 + ", " + n1 + "($" + address1 + ")")
        mips("li", temp2 + ", " + n2)
        mips("slt", temp1 + ", " + temp1 + ", " + temp2)
        mips("beq", temp1 + ", " + 0 + ", " + label)
        mips("lw", temp3 + ", " + n3 + "($" + address2 + ")")
        mips("lw", temp4 + ", " + n4 + "($" + address3 + ")")
        mips("li", temp5 + ", " + n5)
        mips("addu", temp4 + ", " + temp4 + ", " + temp5)
        mips("li", temp5 + ", " + n6)
        mips("mul", temp4 + ", " + temp4 + ", " + temp5)
        mips("addu", temp3 + ", " + temp3 + ", " + temp4)
        mips("lw", temp2 + ", (" + temp3 + ")")
        mips("li", temp3 + ", " + n7)
        mips("slt", temp2 + ", " + temp2 + ", " + temp3)
        mips("move", temp1 + ", " + temp2)
        mips_label(label)
        mips("beq", temp1 + ", " + 1 + ", " + exit_label)

        rpool.recycle(temp1)
        rpool.recycle(temp2)
        rpool.recycle(temp3)
        rpool.recycle(temp4)
        rpool.recycle(temp5)

      case CJump(Binop("AND", Binop("AND", left, right1), right2), label)
      => val new_label1 = new_label()
        val new_label2 = new_label()
        val temp1 = emit(left)

        mips("beq", temp1 + ", " + 0 + ", " + new_label2)
        val temp2 = emit(right1)

        mips("move", temp1 + ", " + temp2)
        mips_label(new_label2)
        mips("beq", temp1 + ", 0, " + new_label1)
        rpool.recycle(temp2)
        val temp3 = emit(right2)

        mips("move", temp1 + ", " + temp3)
        mips_label(new_label1)
        mips("beq", temp1 + ", 1, " + label)

        rpool.recycle(temp3)
        rpool.recycle(temp1)

      case CJump(Binop("NEQ", left, right), label)
      => val temp1 = emit(left)
        val temp2 = emit(right)
        mips("sne", temp1 + ", " + temp1 + ", " + temp2)
        mips("beq", temp1 + ", " + 1 + ", " + label)

        rpool.recycle(temp1)
        rpool.recycle(temp2)

      case CJump(Binop("EQ", left, right), label)
      => val temp1 = emit(left)
        val temp2 = emit(right)
        mips("seq", temp1 + ", " + temp1 + ", " + temp2)
        mips("beq", temp1 + ", " + 1 + ", " + label)

        rpool.recycle(temp1)
        rpool.recycle(temp2)

      case CJump(Binop("LEQ", left, right), label)
      => val temp1 = emit(left)
        val temp2 = emit(right)

        mips("sle", temp1 + ", " + temp1 + ", " + temp2)
        mips("beq", temp1 + ", " + 1 + ", " + label)

        rpool.recycle(temp1)
        rpool.recycle(temp2)

      case CJump(Binop("GEQ", left, right), label)
      => val temp1 = emit(left)
        val temp2 = emit(right)

        mips("sge", temp1 + ", " + temp1 + ", " + temp2)
        mips("beq", temp1 + ", " + 1 + ", " + label)

        rpool.recycle(temp1)
        rpool.recycle(temp2)

      case CJump(IntValue(n), label)
      => val temp1 = emit(IntValue(n))
        mips("beq", temp1 + ", " + 1 + ", " + label)

        rpool.recycle(temp1)

      case CallP(name, static_link, List())
      => val temp1 = emit(static_link)

        mips("move", "$v0, " + temp1)
        mips("jal", name)

        rpool.recycle(temp1)

      case CallP(name, static_link, args)
      => val args_leng = args.length
        var offset = 4 * args_leng
        mips("subu", "$sp, $sp, " + offset)

        val temp1 = emit(args.head)

        if (rpool.used().contains(temp1))
          rpool.recycle(temp1)

        mips("sw", temp1 + ", " + offset + "($sp)")

        for (arg <- args.slice(1, args_leng)) {
          offset -= 4
          val temp2 = emit(arg)
          rpool.recycle(temp2)
          mips("sw", temp1 + ", " + offset + "($sp)")
        }

        val temp3 = emit(static_link)

        mips("move", "$v0, " + temp3)
        mips("jal", name)
        mips("addu", "$sp, $sp, " + (args_leng * 4))

        if (rpool.used().contains(temp1))
          rpool.recycle(temp1)

        if (rpool.used().contains(temp1))
          rpool.recycle(temp3)


      case Label(label)
      => mips_label(label)


      case SystemCall("WRITE_STRING", StringValue(str))
      => str match {
        case "\\n" =>
          mips("li", "$v0, " + 4)
          mips("la", "$a0, ENDL_")
          mips("syscall")
        case _ =>
          val temp = rpool.get()
          val label = new_label()
          mips(".data")
          mips(".align", "2")
          mips_label(label)
          mips(".asciiz", "\"" + str + "\"")
          mips(".text")
          mips("la", "$t0, " + label)
          mips("move", "$a0, " + temp)
          mips("li", "$v0, " + 4)
          mips("syscall")

          rpool.recycle(temp)
      }

      case SystemCall("WRITE_INT", arg)
      => val reg = emit(arg)
        mips("move", "$a0, " + reg)
        mips("li", "$v0, 1")
        mips("syscall")

        rpool.recycle(reg)


      case SystemCall("READ_INT", Mem(Binop("PLUS", Reg("fp"), IntValue(n))))
      => val temp = rpool.get()
        val temp2 = rpool.get()
        mips("li", temp + ", " + n)
        mips("addu", temp2 + ", $fp, " + temp)
        mips("li", "$v0, 5")
        mips("syscall")
        mips("sw", "$v0, (" + temp2 + ")")

        rpool.recycle(temp)
        rpool.recycle(temp2)

      case SystemCall("READ_INT", arg)
      => val reg = emit(arg)
        val temp = rpool.get()
        mips("addu", temp + ", $fp, " + reg)
        mips("li", "$v0, 5")
        mips("syscall")
        mips("sw", "$v0, (" + temp + ")")

        rpool.recycle(reg)
        rpool.recycle(temp)

      case Return()
      => mips("jr", "$ra")

      case _ => throw new Error("*** Unknown IR " + e)
    }
  }

  /** generate initial MIPS code from the program */
  def initialCode() {
    mips(".globl", "main")
    mips(".data")
    mips_label("ENDL_")
    mips(".asciiz", "\"\\n\"")
    mips(".text")
  }
}
