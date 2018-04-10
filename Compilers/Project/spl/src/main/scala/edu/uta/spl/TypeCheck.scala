package edu.uta.spl

abstract class TypeChecker {
  var trace_typecheck = false

  /** symbol table to store SPL declarations */
  var st = new SymbolTable

  def expandType ( tp: Type ): Type
  def typecheck ( e: Expr ): Type
  def typecheck ( e: Lvalue ): Type
  def typecheck ( e: Stmt, expected_type: Type )
  def typecheck ( e: Definition )
  def typecheck ( e: Program )
}


class TypeCheck extends TypeChecker {

  /** typechecking error */
  def error ( msg: String ): Type = {
    System.err.println("*** Typechecking Error: "+msg)
    System.err.println("*** Symbol Table: "+st)
    System.exit(1)
    null
  }

  /** if tp is a named type, expand it */
  def expandType ( tp: Type ): Type =
    tp match {
      case NamedType(nm)
        => st.lookup(nm) match {
          case Some(TypeDeclaration(t))
              => expandType(t)
          case _ => error("Undeclared type: "+tp)
        }
      case _ => tp
  }

  /** returns true if the types tp1 and tp2 are equal under structural equivalence */
  def typeEquivalence ( tp1: Type, tp2: Type ): Boolean =
    if (tp1 == tp2 || tp1.isInstanceOf[AnyType] || tp2.isInstanceOf[AnyType])
      true
    else expandType(tp1) match {
      case ArrayType(t1)
        => expandType(tp2) match {
              case ArrayType(t2)
                => typeEquivalence(t1,t2)
              case _ => false
           }
      case RecordType(fs1)
        => expandType(tp2) match {
              case RecordType(fs2)
                => fs1.length == fs2.length &&
                   (fs1 zip fs2).map{ case (Bind(v1,t1),Bind(v2,t2))
                                        => v1==v2 && typeEquivalence(t1,t2) }
                                .reduce(_&&_)
              case _ => false
           }
      case TupleType(ts1)
        => expandType(tp2) match {
              case TupleType(ts2)
                => ts1.length == ts2.length &&
                   (ts1 zip ts2).map{ case (t1,t2) => typeEquivalence(t1,t2) }
                                .reduce(_&&_)
              case _ => false
           }
      case _
        => tp2 match {
             case NamedType(n) => typeEquivalence(tp1,expandType(tp2))
             case _ => false
           }
    }

  /* tracing level */
  var level: Int = -1

  /** trace typechecking */
  def trace[T] ( e: Any, result: => T ): T = {
    if (trace_typecheck) {
       level += 1
       println(" "*(3*level)+"** "+e)
    }
    val res = result
    if (trace_typecheck) {
       print(" "*(3*level))
       if (e.isInstanceOf[Stmt] || e.isInstanceOf[Definition])
          println("->")
       else println("-> "+res)
       level -= 1
    }
    res
  }

  /** typecheck an expression AST */
  def typecheck ( e: Expr ): Type =
    trace(e,e match {
      case BinOpExp(op,l,r)
        => val ltp = typecheck(l)
           val rtp = typecheck(r)
           if (!typeEquivalence(ltp,rtp))
              error("Incompatible types in binary operation: "+e)
           else if (op.equals("and") || op.equals("or"))
                   if (typeEquivalence(ltp,BooleanType()))
                      ltp
                   else error("AND/OR operation can only be applied to booleans: "+e)
           else if (op.equals("eq") || op.equals("neq"))
                   BooleanType()
           else if (!typeEquivalence(ltp,IntType()) && !typeEquivalence(ltp,FloatType()))
                   error("Binary arithmetic operations can only be applied to integer or real numbers: "+e)
           else if (op.equals("gt") || op.equals("lt") || op.equals("geq") || op.equals("leq"))
                   BooleanType()

      /* PUT YOUR CODE HERE */
           else if (op.equals("times") || op.equals("plus") || op.equals("minus") || op.equals("times") || op.equals("div")) {
           //ltp //We already know at this point that the two types are equal and that they're only either an int or a float so we can just return the type of the ltp
             if (ltp == rtp && typeEquivalence(ltp, IntType()))
               IntType()
             else
               FloatType()}
//             else error("Incompatible types in binary operation: " + e)
           else ltp
      case UnOpExp(op, expr)
        => typecheck(expr)
      case CallExp(name, exprs)
        => st.lookup(name) match {
        case Some(FuncDeclaration(outputType, params, "", 0, 0)) => {
          if (params.length != exprs.length) {
            throw new Error("Number of paramters doesn't match number of arguments")
          }

          else {
            (exprs.map(typecheck(_)) zip params).map({
              case (atp, ptp) => //TODO: Can this be in the format of (atp, ptp)?
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
        => var test = bind_exprs.foreach(bind_expr => typecheck(bind_expr.value))
           bind_exprs.map(bind_expr => st.lookup(bind_expr.name) match {
             case Some(TypeDeclaration(t)) => t
             case Some(VarDeclaration(t,_,_)) => t
             case None => error("Why are we getting None cases?") //TODO: This is getting to none because the names weren't added to the symbol table previously. Need to investigate further
           })
        println("Printng out the map")
        println(test)
        NoType() //TODO: Need to figure out a way to return a RecordType

//        if (bind_exprs.isEmpty) {
//          error("Error the hell out")
//        }
//
//        else {//(bind_exprs.isEmpty) {
//          bind_exprs.foreach{bind_expr => st.lookup(bind_expr.name) match {
//            case Some(TypeDeclaration(t)) => t
//            case Some(_) => error(bind_expr.name + " is not a variable.")
//            case None => error("Undefined variable")
//          }
//          }}

//            case Bind(name, expr) => typecheck(expr)
//            case _ => None
//            //case None => None}
//        }

//        bind_exprs.foreach{x => typecheck(x.value)}

//        bind_exprs.foreach { x => typecheck(x.value) }
//        bind_exprs.foreach{
//           bind_expr => st.lookup(bind_expr.name) match {
//            case Some(TypeDeclaration(hastype)) => hastype
//            case Some(_) => error(bind_expr.name + " is not a variable")
//            case None => error("Undefined variable")
//          }
////          case None => error("test error")
//        }

//      case RecordExp(exprs)
//        => exprs.foreach { case Bind(name, expr) => //typecheck(expr)}//
//          st.lookup(name) match {
//            case Some(Bind())
//      //} }
      case IntConst(value)
        => IntType()
      case FloatConst(value)
        => FloatType()
      case StringConst(value)
        => StringType()
      case BooleanConst(value)
        => BooleanType()
      case LvalExp(lvalue)
        => typecheck(lvalue)
      case NullExp() //TODO: Check what to do with null expressions
        => null

      case _ => throw new Error("Wrong expression: "+e)
    } )

  /** typecheck an Lvalue AST */
  def typecheck ( e: Lvalue ): Type =
    trace(e,e match {
      case Var(name)
        => st.lookup(name) match {
              case Some(VarDeclaration(t,_,_)) => t
              case Some(_) => error(name+" is not a variable")
              case None => error("Undefined variable: "+name)
        }

      /* PUT YOUR CODE HERE */

      case _ => throw new Error("Wrong lvalue: "+e)
    } )

  /** typecheck a statement AST using the expected type of the return value from the current function */
  def typecheck ( e: Stmt, expected_type: Type ) {
    trace(e,e match {
      case AssignSt(d,s)
        => if (!typeEquivalence(typecheck(d),typecheck(s)))
              error("Incompatible types in assignment: "+e)

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
        =>
//          st.begin_scope()
          exprs.foreach{ x => typecheck(x)}
//          st.end_scope()
      case ReadSt(lvalues)
        =>  lvalues.foreach { lval => typecheck(lval)}
      case _ => throw new Error("Wrong statement: "+e)
    } )
  }

  /** typecheck a definition */
  def typecheck ( e: Definition ) {
    trace(e,e match {
      case FuncDef(f,ps,ot,b)
        => st.insert(f,FuncDeclaration(ot,ps,"",0,0))
           st.begin_scope()
           ps.foreach{ case Bind(v,tp) => st.insert(v,VarDeclaration(tp,0,0)) }
           typecheck(b,ot)
           st.end_scope()

      /* PUT YOUR CODE HERE */
        //We need to insert for all declarations because it's the first time the variables are being declared
      case VarDef(name, hasType, expr)
        =>  st.insert(name, VarDeclaration(hasType, 0, 0))
            st.begin_scope()
            typecheck(expr)
            st.end_scope()
      case TypeDef(name, isType)
        => st.insert(name, TypeDeclaration(isType)) //TODO: Do we need to do anything with type defs?
//          st.begin_scope()
      //        isType
      //        st.end_scope()
      case _ => throw new Error("Wrong statement: "+e)
    } )
  }

  /** typecheck the main program */
  def typecheck ( e: Program ) {
    typecheck(e.body,NoType())
  }
}
