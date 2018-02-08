/********************************************************************************
*
* File: spl.lex
* The SPL scanner
*
********************************************************************************/

package edu.uta.spl;

import java_cup.runtime.Symbol;

%%
%class SplLex
%public
%line
%column
%cup

%{

  private Symbol symbol ( int type ) {
    return new Symbol(type, yyline+1, yycolumn+1);
  }

  private Symbol symbol ( int type, Object value ) {
    return new Symbol(type, yyline+1, yycolumn+1, value);
  }

  public void lexical_error ( String message ) {
    System.err.println("*** Lexical Error: " + message + " (line: " + (yyline+1)
                       + ", position: " + (yycolumn+1) + ")");
    System.exit(1);
  }

%}

%%
//"print"         { "PRINT";}
//"array"         { return symbol(sym.ARRAY); }
//.               { lexical_error("Illegal character"); }
[ \t\r\n\f]       { /* ignore white spaces. */ }
"print"           { System.out.println("PRINT");}
"("               { return symbol(sym.LP);}
")"               { return symbol(sym.RP);}
";"               { return symbol(sym.SEMI); }

/* GENERIC REs */
\"(.*)\"     { /* Returns the string value in a print statement  */ return symbol(sym.STRING_LITERAL, yytext().substring(1, yytext().length()-1));}
