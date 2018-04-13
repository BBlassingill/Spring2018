package edu.uta.spl

import javax.lang.model.`type`.NullType

abstract class TypeChecker {
  var trace_typecheck = false

  /** symbol table to store SPL declarations */
  var st = new SymbolTable

  def expandType(tp: Type): Type

  def typecheck(e: Expr): Type

  def typecheck(e: Lvalue): Type

  def typecheck(e: Stmt, expected_type: Type)

  def typecheck(e: Definition)

  def typecheck(e: Program)
}


class TypeCheck extends TypeChecker {

  /** typechecking error */
  def error(msg: String): Type = {
    System.err.println("*** Typechecking Error: " + msg)
    System.err.println("*** Symbol Table: " + st)
    System.exit(1)
    null
  }

  /** if tp is a named type, expand it */
  def expandType(tp: Type): Type =
    tp match {
      case NamedType(nm)
      => st.lookup(nm) match {
        case Some(TypeDeclaration(t))
        => expandType(t)
        case _ => error("Undeclared type: " + tp)
      }
      case _ => tp
    }

  /** returns true if the types tp1 and tp2 are equal under structural equivalence */
  def typeEquivalence(tp1: Type, tp2: Type): Boolean =
    if (tp1 == tp2 || tp1.isInstanceOf[AnyType] || tp2.isInstanceOf[AnyType])
      true
    else expandType(tp1) match {
      case ArrayType(t1)
      => expandType(tp2) match {
        case ArrayType(t2)
        => typeEquivalence(t1, t2)
        case _ => false
      }
      case RecordType(fs1)
      => expandType(tp2) match {
        case RecordType(fs2)
        => fs1.length == fs2.length &&
          (fs1 zip fs2).map { case (Bind(v1, t1), Bind(v2, t2))
          => v1 == v2 && typeEquivalence(t1, t2)
          }
            .reduce(_ && _)
        case _ => false
      }
      case TupleType(ts1)
      => expandType(tp2) match {
        case TupleType(ts2)
        => ts1.length == ts2.length &&
          (ts1 zip ts2).map { case (t1, t2) => typeEquivalence(t1, t2) }
            .reduce(_ && _)
        case _ => false
      }
      case _
      => tp2 match {
        case NamedType(n) => typeEquivalence(tp1, expandType(tp2))
        case _ => false
      }
    }

  /* tracing level */
  var level: Int = -1

  /** trace typechecking */
  def trace[T](e: Any, result: => T): T = {
    if (trace_typecheck) {
      level += 1
      println(" " * (3 * level) + "** " + e)
    }
    val res = result
    if (trace_typecheck) {
      print(" " * (3 * level))
      if (e.isInstanceOf[Stmt] || e.isInstanceOf[Definition])
        println("->")
      else println("-> " + res)
      level -= 1
    }
    res
  }

  /** typecheck an expression AST */
  def typecheck(e: Expr): Type =
    trace(e, e match {
      case BinOpExp(op, l, r)
      => val ltp = typecheck(l)
        val rtp = typecheck(r)
        if (!typeEquivalence(ltp, rtp))
          error("Incompatible types in binary operation: " + e)
        else if (op.equals("and") || op.equals("or"))
          if (typeEquivalence(ltp, BooleanType()))
            ltp
          else error("AND/OR operation can only be applied to booleans: " + e)
        else if (op.equals("eq") || op.equals("neq"))
          BooleanType()
        else if (!typeEquivalence(ltp, IntType()) && !typeEquivalence(ltp, FloatType()))
          error("Binary arithmetic operations can only be applied to integer or real numbers: " + e)
        else if (op.equals("gt") || op.equals("lt") || op.equals("geq") || op.equals("leq"))
          BooleanType()

        /* PUT YOUR CODE HERE */
        else if (op.equals("times") || op.equals("plus") || op.equals("minus") || op.equals("times") || op.equals("div")) {
          //ltp //We already know at this point that the two types are equal and that they're only either an int or a float so we can just return the type of the ltp
          if (ltp == rtp && typeEquivalence(ltp, IntType()))
            IntType()
          else
            FloatType()
        }
        //             else error("Incompatible types in binary operation: " + e)
        else ltp
      case UnOpExp(op, expr)
      => typecheck(expr)
      case CallExp(name, exprs)
      => st.lookup(name) match {
        case Some(FuncDeclaration(outputType, params, "", 0, 0)) => {
          if (params.length != exprs.length) {
            throw new Error("Number of parameters doesn't match number of arguments")
          }

          else {
            (exprs.map(typecheck(_)) zip params).foreach({
              case (atp, ptp) =>
                if (!typeEquivalence(atp, ptp.value)) {
                  throw new Error("The type of call argument (" + atp + ") does not match the type of the formal parameter: " + ptp)
                }
            })
            outputType
          }
        }

        case _ => throw new Error("Undefined function: " + name)
      }

      case RecordExp(bind_exprs)
      =>

        var bind_list = bind_exprs.map(bind_expr => Bind(bind_expr.name, typecheck(bind_expr.value)))
        bind_list.foreach(bind_expr => st.insert(bind_expr.name, VarDeclaration(bind_expr.value, 0, 0)))

        RecordType(bind_list)

      case ArrayExp(exprs)
      => val initialType = typecheck(exprs.head)
        for (expr <- exprs.drop(1)) {
          if (!typeEquivalence(initialType, typecheck(expr)))
            error("Elements must be of same type: " + exprs)
        }
        ArrayType(initialType)

      case ArrayGen(length, value)
      => typecheck(length)
        ArrayType(typecheck(value))

      case IntConst(value)
      => IntType()

      case FloatConst(value)
      => FloatType()

      case StringConst(value)
      => StringType()

      case BooleanConst(value)
      => BooleanType()

      case LvalExp(lvalue)
      => var t = typecheck(lvalue)
        t

      case NullExp() //TODO: Check what to do with null expressions
      => AnyType()

      case _ => throw new Error("Wrong expression: " + e)
    })

  /** typecheck an Lvalue AST */
  def typecheck(e: Lvalue): Type =
    trace(e, e match {
      case Var(name)
      => st.lookup(name) match {
        case Some(VarDeclaration(t, _, _)) => t
        case Some(_) => error(name + " is not a variable")
        case None => error("Undefined variable: " + name)
      }

      /* PUT YOUR CODE HERE */
      case RecordDeref(record, attribute)
      => st.begin_scope()
        val t = typecheck(record)

        if (t.isInstanceOf[RecordType]) {
          val returnedType = t.asInstanceOf[RecordType]
          val bindList = returnedType.components

          (for (e <- bindList.toIterator if e.name == attribute) yield e.value).next()
        }

        else if (t.isInstanceOf[NamedType]) {
          val returnedType = t.asInstanceOf[NamedType]
          val namedType = returnedType.typename

          st.lookup(namedType) match {
            case Some(TypeDeclaration(t2)) =>
              val recordType = t2.asInstanceOf[RecordType]
              val bindList = recordType.components

              (for (e <- bindList.toIterator if e.name == attribute) yield e.value).next()
          }
        }

        else {
          print("This is just a test")
          FloatType()
        }


      case ArrayDeref(array, index)
      =>
        val type1 = typecheck(index)
        val type2 = typecheck(array).asInstanceOf[ArrayType]

        //if (typeEquivalence(type1, type2))
        type2.element_type //TODO:Not sure if this is right


      case _ => throw new Error("Wrong lvalue: " + e)
    })

  /** typecheck a statement AST using the expected type of the return value from the current function */
  def typecheck(e: Stmt, expected_type: Type) {
    trace(e, e match {
      case AssignSt(d, s)
      => if (!typeEquivalence(typecheck(d), typecheck(s)))
        error("Incompatible types in assignment: " + e)

      /* PUT YOUR CODE HERE */
      case BlockSt(defs, stmts) //TODO: Need to handle defs case
      =>
        st.begin_scope()
        if (defs.isEmpty) {
          stmts.foreach { x => typecheck(x, NoType()) }
        }
        else if (stmts.isEmpty) {
          defs.foreach { x => typecheck(x) }
        }

        else {
          defs.foreach { x => typecheck(x) }
          stmts.foreach { x => typecheck(x, NoType()) }
        }
        st.end_scope()

      case PrintSt(exprs) //TODO: Not sure if these beginning and ending scopes are necessary for a print statement/Read statements
      => exprs.foreach { x => typecheck(x) }

      case ReadSt(lvalues)
      => lvalues.foreach { lval => typecheck(lval) }

      case ForSt(variable, initial, step, increment, body)
      => st.begin_scope()
        var type1 = typecheck(initial)
        var type2 = typecheck(step)
        var type3 = typecheck(increment)

        if (typeEquivalence(type1, type2)) {
          if (typeEquivalence(type1, type3)) {
            st.insert(variable, VarDeclaration(type1, 0, 0))
          }

          else error("Incompatible types for For loop iterations")
        }

        else error("Incompatible types for For loop iterations")

        typecheck(body, expected_type) //TODO: Don't think expected_type here is correct lol
        st.end_scope()

      case WhileSt(condition, body)
      => typecheck(condition)
        typecheck(body, expected_type)

      case IfSt(condition, then_stmt, else_stmt)
      => typecheck(condition)
        typecheck(then_stmt, expected_type)

        if (else_stmt != null)
          typecheck(else_stmt, expected_type)


      case CallSt(name, list_of_exprs)
      => list_of_exprs.foreach(x => typecheck(x))

      case ReturnValueSt(value)
      => typecheck(value)

      case ReturnSt()
        => st.begin_scope()
        expected_type

      case _ => throw new Error("Wrong statement: " + e)
    })
  }

  /** typecheck a definition */
  def typecheck(e: Definition) {
    trace(e, e match {
      case FuncDef(f, ps, ot, b)
      => st.insert(f, FuncDeclaration(ot, ps, "", 0, 0))
        st.begin_scope()
        ps.foreach { case Bind(v, tp) => st.insert(v, VarDeclaration(tp, 0, 0)) }
        typecheck(b, ot)
        st.end_scope()

      /* PUT YOUR CODE HERE */
      //We need to insert for all declarations because it's the first time the variables are being declared
      case VarDef(name, hasType, expr)
      =>
        //st.begin_scope()
        st.insert(name, VarDeclaration(typecheck(expr), 0, 0))
      // st.begin_scope() //TODO: Don't think it makes sense to define the scope here
      //typecheck(expr)
      // st.end_scope()
      case TypeDef(name, isType)
      => st.insert(name, TypeDeclaration(isType)) //TODO: Do we need to do anything with type defs?
        //st.begin_scope()
      case _ => throw new Error("Wrong statement: " + e)
    })
  }

  /** typecheck the main program */
  def typecheck(e: Program) {
    typecheck(e.body, NoType())
  }
}