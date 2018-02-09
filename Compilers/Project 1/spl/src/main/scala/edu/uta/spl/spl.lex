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
ID=[a-zA-Z][a-zA-Z0-9_]*
DIGIT=[0-9]

%%
//"print"         	{ "PRINT";}
//"array"         	{ return symbol(sym.ARRAY); }
[ \t\r\n\f]       	{ /* ignore white spaces. */ }
"print"           	{ System.out.println("PRINT");}
"("               	{ return symbol(sym.LP);}
")"               	{ return symbol(sym.RP);}
";"               	{ return symbol(sym.SEMI);}
":"					{ return symbol(sym.COLON);}
","                 { return symbol(sym.COMMA);}
"="					{ System.out.println("EQUAL");}
"<"					{ return symbol(sym.LT);}
">"					{ return symbol(sym.GT);}
"{"					{ return symbol(sym.LB);}
"}"					{ return symbol(sym.RB);}
"["					{ return symbol(sym.LSB);}
"]"					{ return symbol(sym.RSB);}
"&&"                { return symbol(sym.AND);}
"=="                { return symbol(sym.EW);}
"true"              { return symbol(sym.TRUE);}
"false"             { return symbol(sym.FALSE);}
"+"					{ return symbol(sym.PLUS);}
"*"					{ return symbol(sym.TIMES);}
"-"                 { return symbol(sym.MINUS);}
[-DIGIT+]           { /*represents a negative number */ return symbol(sym.INTEGER_LITERAL, new Integer(yytext()));}
\"(.*)\"     		{ /* Returns a string */ return symbol(sym.STRING_LITERAL, yytext().substring(1, yytext().length()-1));}
[ID+.ID+]           { /* Returns a dot if it's in between 2 IDs */ return symbol(sym.DOT);}
"var"				{ return symbol(sym.VAR);}
"def"				{ return symbol(sym.DEF);}
"read"				{ System.out.println("READ");}
"type"              { return symbol(sym.TYPE);}
"int"				{ System.out.println("INT");}
"if"                { return symbol(sym.IF);}
"else"              { return symbol(sym.ELSE);}
"array"             { return symbol(sym.ARRAY);}
"for"               { return symbol(sym.FOR);}
"to"                { return symbol(sym.TO);}
"while"             { return symbol(sym.WHILE);}
{DIGIT}+			{ return symbol(sym.INTEGER_LITERAL, new Integer(yytext()));}
{ID}				{ return symbol(sym.ID, yytext());}
.					{ lexical_error("Illegal character");}