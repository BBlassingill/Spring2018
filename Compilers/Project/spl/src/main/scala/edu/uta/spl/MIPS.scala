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

  179

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

      case IntValue(n)
      => val temp1 = rpool.get()
        mips("li", temp1 + ", " + n)

        temp1

      case Binop("PLUS", left, right)
      => val temp1 = emit(left)
        val temp2 = emit(right)

        mips("addu", temp1 + ", " + temp1 + ", " + temp2)

        rpool.recycle(temp2)
        temp1

      case Binop("TIMES", left, right)
      => val temp1 = emit(left)
        val temp2 = emit(right)

        mips("mul", temp1 + ", " + temp1 + ", " + temp2)

        rpool.recycle(temp2)
        temp1

      case Binop("MINUS", left, right)
      => val temp1 = emit(left)
        val temp2 = emit(right)

        mips("subu", temp1 + ", " + temp1 + ", " + temp2)

        rpool.recycle(temp2)
        temp1

      case Binop("LT", left, right)
      => val temp1 = emit(left)
        val temp2 = emit(right)

        mips("slt", temp1 + ", " + temp1 + ", " + temp2)

        rpool.recycle(temp2)

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

      case Mem(Binop("PLUS", left, right))
      => val temp1 = emit(left)
        val temp2 = emit(right)

        rpool.recycle(temp2)

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
      case Label(label)
      => mips_label(label)

      case Jump(name)
      => mips("j", name)

      case Move(Mem(Reg(destination)), Reg(source))
      => mips("sw", source + ", " + "($" + destination + ")")

      case Move(Reg(destination), Reg(source))
      => mips("move", "$" + destination + ", $" + source)

      case Move(Reg(destination), Mem(Reg(source)))
      => mips("lw", "$" + destination + ", ($" + source + ")")

      case Move(Reg(source), Binop("PLUS", Reg(destination), IntValue(n)))
      => val temp1 = rpool.get()
        val temp2 = rpool.get()

        mips("li", temp1 + ", " + n)
        mips("addu", temp2 + ", $" + source + ", " + temp1)
        mips("move", "$" + destination + ", " + temp2)

        rpool.recycle(temp2)
        rpool.recycle(temp1)

      case Move(Reg(destination), Mem(Binop("PLUS", Reg(source), IntValue(n))))
      => mips("lw", "$" + destination + ", " + n + "($" + source + ")")

      case Move(Mem(Binop("PLUS", left, IntValue(n))), source)
      => val temp1 = emit(left)
        val temp2 = emit(source)

        mips("sw", temp2 + ", " + n + "(" + temp1 + ")")

        rpool.recycle(temp2)
        rpool.recycle(temp1)

      case Move(Mem(address), source)
      => val temp1 = emit(address)
        val temp2 = emit(source)

        mips("sw", temp2 + ", (" + temp1 + ")")

        rpool.recycle(temp2)
        rpool.recycle(temp1)

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

      case SystemCall("READ_INT", Mem(Binop("PLUS", Reg(address), IntValue(n))))
      => val temp1 = rpool.get()
        val temp2 = rpool.get()
        mips("li", temp1 + ", " + n)
        mips("addu", temp2 + ", $" + address + ", " + temp1)
        mips("li", "$v0, 5")
        mips("syscall")
        mips("sw", "$v0, (" + temp2 + ")")

        rpool.recycle(temp2)
        rpool.recycle(temp1)

      case Return()
      => mips("jr", "$ra")

      case CJump(Binop("GT", left, right), label)
      => val temp1 = emit(left)
        val temp2 = emit(right)

        mips("sgt", temp1 + ", " + temp1 + ", " + temp2)
        mips("beq", temp1 + ", 1" + ", " + label)

        rpool.recycle(temp2)
        rpool.recycle(temp1)

      case CJump(Unop("NOT", op), label)
      => val temp1 = emit(op)

        mips("seq", temp1 + ", " + temp1 + ", 0")
        mips("beq", temp1 + ", 1" + ", " + label)

        rpool.recycle(temp1)

      case CJump(Binop("AND", left, right), label)
      => val newlabel = new_label()
        val temp1 = emit(left)

        mips("beq", temp1 + ", 0" + ", " + newlabel)
        val temp2 = emit(right)

        mips("move", temp1 + ", " + temp2)

        mips_label(label)

        mips("beq", temp1 + ", 1, " + label)

        rpool.recycle(temp2)
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
