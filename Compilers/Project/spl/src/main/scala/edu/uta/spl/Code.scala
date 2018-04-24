/** **************************************************************************************************
  *
  * File: Code.scala
  * The IR code generator for SPL programs
  *
  * ***************************************************************************************************/

package edu.uta.spl

import scala.collection.mutable.ListBuffer


abstract class CodeGenerator(tc: TypeChecker) {
  def typechecker: TypeChecker = tc

  def st: SymbolTable = tc.st

  def code(e: Program): IRstmt

  def allocate_variable(name: String, var_type: Type, fname: String): IRexp
}


class Code(tc: TypeChecker) extends CodeGenerator(tc) {

  var name_counter = 0

  /** generate a new name */
  def new_name(name: String): String = {
    name_counter += 1
    name + "_" + name_counter
  }

  /** IR code to be added at the end of program */
  var addedCode: List[IRstmt] = Nil

  def addCode(code: IRstmt*) {
    addedCode ++= code
  }

  /** allocate a new variable at the end of the current frame and return the access code */
  def allocate_variable(name: String, var_type: Type, fname: String): IRexp =
    st.lookup(fname) match {
      case Some(FuncDeclaration(rtp, params, label, level, min_offset))
      => // allocate variable at the next available offset in frame
        st.insert(name, VarDeclaration(var_type, level, min_offset))
        // the next available offset in frame is 4 bytes below
        st.replace(fname, FuncDeclaration(rtp, params, label, level, min_offset - 4))
        // return the code that accesses the variable
        Mem(Binop("PLUS", Reg("fp"), IntValue(min_offset)))
      case _ => throw new Error("No current function: " + fname)
    }

  /** access a frame-allocated variable from the run-time stack */
  def access_variable(name: String, level: Int): IRexp =
    st.lookup(name) match {
      case Some(VarDeclaration(_, var_level, offset))
      => var res: IRexp = Reg("fp")
        // non-local variable: follow the static link (level-var_level) times
        for (i <- var_level + 1 to level)
          res = Mem(Binop("PLUS", res, IntValue(-8)))
        Mem(Binop("PLUS", res, IntValue(offset)))
      case _ => throw new Error("Undefined variable: " + name)
    }

  /** return the IR code from the Expr e (level is the current function nesting level,
    * fname is the name of the current function/procedure) */
  def code(e: Expr, level: Int, fname: String): IRexp =
    e match {
      case BinOpExp(op, left, right)
      => val cl = code(left, level, fname)
        val cr = code(right, level, fname)
        val nop = op.toUpperCase()
        Binop(nop, cl, cr)
      case ArrayGen(len, v)
      => println("created new loop in arraygen")
        val A = allocate_variable(new_name("A"), typechecker.typecheck(e), fname)
        val L = allocate_variable(new_name("L"), IntType(), fname)
        val V = allocate_variable(new_name("V"), typechecker.typecheck(v), fname)
        val I = allocate_variable(new_name("I"), IntType(), fname)
        val loop = new_name("loop")
        val exit = new_name("exit")
        ESeq(Seq(List(Move(L, code(len, level, fname)), // store length in L
          Move(A, Allocate(Binop("PLUS", L, IntValue(1)))),
          Move(V, code(v, level, fname)), // store value in V
          Move(Mem(A), L), // store length in A[0]
          Move(I, IntValue(0)),
          Label(loop), // for-loop
          CJump(Binop("GEQ", I, L), exit),
          Move(Mem(Binop("PLUS", A, Binop("TIMES", Binop("PLUS", I, IntValue(1)), IntValue(4)))), V), // A[i] = v
          Move(I, Binop("PLUS", I, IntValue(1))),
          Jump(loop),
          Label(exit))),
          A)

      /* PUT YOUR CODE HERE */
      case IntConst(value)
      => IntValue(value)
      case FloatConst(value)
      => FloatValue(value)
      case StringConst(value)
      => StringValue(value)
      case BooleanConst(value)
      => if (value.equals(true))
        IntValue(1)
      else
        IntValue(0)

      case LvalExp(value)
      => code(value, level, fname)
      case ArrayExp(exprs)
      => var ir_stmts = ListBuffer[IRstmt]()
        var ir_exprs = ListBuffer[IRexp]()

        val current_offset = st.lookup(fname) match {
          case Some(FuncDeclaration(outtype, params, label, level_of_func, available_offset)) =>
            st.replace(fname, FuncDeclaration(outtype, params, label, level_of_func, available_offset - 4)) //TODO: Not sure if this correct to do
            available_offset
        }

        ir_stmts += Move(Mem(Binop("PLUS", Reg("fp"), IntValue(current_offset))), Allocate(IntValue(exprs.length + 1)))
        ir_stmts += Move(Mem(Mem(Binop("PLUS", Reg("fp"), IntValue(current_offset)))), IntValue(exprs.length))

        var beginningAddress = 4

        new_name("loop")

        for (expr <- exprs) yield {
          ir_stmts += Move(Mem(Binop("PLUS", Mem(Binop("PLUS", Reg("fp"), IntValue(current_offset))), IntValue(beginningAddress))), code(expr, level, fname))
          beginningAddress += 4
        }

        ESeq(Seq(ir_stmts.toList), Mem(Binop("PLUS", Reg("fp"), IntValue(current_offset))))
      case _ => throw new Error("Wrong expression: " + e)
    }

  /** return the IR code from the Lvalue e (level is the current function nesting level,
    * fname is the name of the current function/procedure) */
  def code(e: Lvalue, level: Int, fname: String): IRexp =
    e match {
      case RecordDeref(r, a)
      => val cr = code(r, level, fname)
        typechecker.expandType(typechecker.typecheck(r)) match {
          case RecordType(cl)
          => val i = cl.map(_.name).indexOf(a)
            Mem(Binop("PLUS", cr, IntValue(i * 4)))
          case _ => throw new Error("Unkown record: " + e)
        }

      /* PUT YOUR CODE HERE */
      case ArrayDeref(array, index)
      =>
        val codeForIndex = code(index, level, fname)
        val returnedCode = code(array, level, fname)
        Mem(Binop("PLUS", returnedCode, Binop("TIMES", Binop("PLUS", codeForIndex, IntValue(1)), IntValue(4))))

      case Var(name)
      => access_variable(name, level) //TODO: Not sure if this level needs to consider variables that were declared in higher stack frames
      case _ => throw new Error("Wrong statement: " + e)
    }

  /** return the IR code from the Statement e (level is the current function nesting level,
    * fname is the name of the current function/procedure)
    * and exit_label is the exit label       */
  def code(e: Stmt, level: Int, fname: String, exit_label: String): IRstmt =
    e match {
      case ForSt(v, a, b, c, s)
      => val loop = new_name("loop")
        val exit = new_name("exit")
        val cv = allocate_variable(v, IntType(), fname)
        val ca = code(a, level, fname)
        val cb = code(b, level, fname)
        val cc = code(c, level, fname)
        val cs = code(s, level, fname, exit)
        Seq(List(Move(cv, ca), // needs cv, not Mem(cv)
          Label(loop),
          CJump(Binop("GT", cv, cb), exit),
          cs,
          Move(cv, Binop("PLUS", cv, cc)), // needs cv, not Mem(cv)
          Jump(loop),
          Label(exit)))

      /* PUT YOUR CODE HERE */
      case BlockSt(defs, stmts)
      => val defIRs = for (d <- defs) yield code(d, fname, level)
        val stmtIRs = for (s <- stmts) yield code(s, level, fname, exit_label)

        Seq(defIRs ::: stmtIRs)
      case PrintSt(exprs)
      => var x = ListBuffer[IRstmt]()
        for (expr <- exprs) yield {
          val returnedType = tc.typecheck(expr)
          var systemCall1: IRstmt = null

          returnedType match {
            case _: IntType =>
              systemCall1 = SystemCall("WRITE_INT", code(expr, level, fname))
              x += systemCall1
            case _: FloatType =>
              systemCall1 = SystemCall("WRITE_FLOAT", code(expr, level, fname))
              x += systemCall1
            case _: BooleanType =>
              systemCall1 = SystemCall("WRITE_BOOL", code(expr, level, fname))
              x += systemCall1
            case _ =>
              systemCall1 = SystemCall("WRITE_STRING", code(expr, level, fname))
              x += systemCall1
          }


        }

        val systemCall2 = SystemCall("WRITE_STRING", StringValue("\\n"))
        x += systemCall2

        Seq(x.toList)

      case CallSt(name, exprs)
      =>
        val IRs = for (e <- exprs) yield code(e, level - 1, fname) //TODO: Not sure if level needs to be calculated since these are passed in arguments. Using level-1 for now

        val static_link = Reg("fp")
        val returned_label = st.lookup(name) match {
          case Some(FuncDeclaration(outtype, params, label, level, available_offset)) => label
        }


        CallP(returned_label, static_link, IRs)

      case ReadSt(lvalues)
      => var x = ListBuffer[IRstmt]()
        for (lval <- lvalues) yield {
          val returnedType = tc.typecheck(lval)
          var systemCall1: IRstmt = null

          returnedType match {
            case _: IntType =>
              systemCall1 = SystemCall("READ_INT", code(lval, level, fname))
              x += systemCall1
            case _: FloatType =>
              systemCall1 = SystemCall("READ_FLOAT", code(lval, level, fname))
              x += systemCall1
          }

          //          val systemCall2 = SystemCall("WRITE_STRING", StringValue("\\n"))
          //          x += systemCall2
        }

        Seq(x.toList)

      case WhileSt(condition, body)
      => var x = ListBuffer[IRstmt]()
        val loopAddress = new_name("loop")
        val exitAddress = new_name("exit")

        x += Label(loopAddress)
        x += CJump(Unop("NOT", code(condition, level, fname)), exitAddress)
        x += code(body, level, fname, "exit")
        x += Jump(loopAddress)
        x += Label(exitAddress)

        Seq(x.toList)

      case AssignSt(destination, source)
      => Move(code(destination, level, fname), code(source, level, fname))
      case _ => throw new Error("Wrong statement: " + e)
    }

  /** return the IR code for the declaration block of function fname
    * (level is the current function nesting level) */
  def code(e: Definition, fname: String, level: Int): IRstmt =
    e match {
      case FuncDef(f, ps, ot, b)
      => val flabel = if (f == "main") f else new_name(f)
        /* initial available offset in frame f is -12 */
        st.insert(f, FuncDeclaration(ot, ps, flabel, level + 1, -12))
        st.begin_scope()
        /* formal parameters have positive offsets */
        ps.zipWithIndex.foreach { case (Bind(v, tp), i)
        => st.insert(v, VarDeclaration(tp, level + 1, (ps.length - i) * 4))
        }
        val body = code(b, level + 1, f, "")
        st.end_scope()
        st.lookup(f) match {
          case Some(FuncDeclaration(_, _, _, _, offset))
          => addCode(Label(flabel),
            /* prologue */
            Move(Mem(Reg("sp")), Reg("fp")),
            Move(Reg("fp"), Reg("sp")),
            Move(Mem(Binop("PLUS", Reg("fp"), IntValue(-4))), Reg("ra")),
            Move(Mem(Binop("PLUS", Reg("fp"), IntValue(-8))), Reg("v0")),
            Move(Reg("sp"), Binop("PLUS", Reg("sp"), IntValue(offset))),
            body,
            /* epilogue */
            Move(Reg("ra"), Mem(Binop("PLUS", Reg("fp"), IntValue(-4)))),
            Move(Reg("sp"), Reg("fp")),
            Move(Reg("fp"), Mem(Reg("fp"))),
            Return())
            Seq(List())
          case _ => throw new Error("Unkown function: " + f)
        }

      /* PUT YOUR CODE HERE */
      case VarDef(name, hasType, expr)
      =>
        val access_code = allocate_variable(name, tc.typecheck(expr), fname)

        Move(access_code, code(expr, level, fname))

      case TypeDef(name, isType)
      => Seq(List()) //TODO: Come back and expand the typedef case


      case _ => throw new Error("Wrong statement: " + e)
    }

  def code(e: Program): IRstmt =
    e match {
      case Program(b@BlockSt(_, _))
      => st.begin_scope()
        val res = code(FuncDef("main", List(), NoType(), b), "", 0)
        st.end_scope()
        Seq(res :: addedCode)
      case _ => throw new Error("Wrong program " + e);
    }
}
