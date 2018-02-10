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
COMMENT_BLOCK=(\/\*[^\*\/]*\*\/)
%%
[ \t\r\n\f]       	{ /* ignore white spaces. */ }
{COMMENT_BLOCK}     { /* ignore comment block */}
"("               	{ return symbol(sym.LP);}
")"               	{ return symbol(sym.RP);}
";"               	{ return symbol(sym.SEMI);}
":"					{ return symbol(sym.COLON);}
","                 { return symbol(sym.COMMA);}
"\."                { return symbol(sym.DOT);}
"="					{ return symbol(sym.EQUAL);}
"<"					{ return symbol(sym.LT);}
">"					{ return symbol(sym.GT);}
"<>"                { return symbol(sym.NEQ);}
"<="                { return symbol(sym.LEQ);}
">="                { return symbol(sym.GEQ);}
"{"					{ return symbol(sym.LB);}
"}"					{ return symbol(sym.RB);}
"["					{ return symbol(sym.LSB);}
"]"					{ return symbol(sym.RSB);}
"&&"                { return symbol(sym.AND);}
"=="                { return symbol(sym.EQ);}
"!"                 { return symbol(sym.NOT);}
"||"                { return symbol(sym.OR);}
"true"              { return symbol(sym.TRUE);}
"false"             { return symbol(sym.FALSE);}
"+"					{ return symbol(sym.PLUS);}
"*"					{ return symbol(sym.TIMES);}
"-"                 { return symbol(sym.MINUS);}
"/"                 { return symbol(sym.DIV);}
"%"                 { return symbol(sym.MOD);}
"#"                 { return symbol(sym.SHARP);}
(-DIGIT+)           { /*represents a negative number */ return symbol(sym.INTEGER_LITERAL, new Integer(yytext()));}
(DIGIT+)            { return symbol(sym.INTEGER_LITERAL, new Integer(yytext()));}
(\"[^\"]*\")        { return symbol(sym.STRING_LITERAL, yytext().substring(1, yytext().length()-1));}
(DIGIT+.DIGIT+)     { return symbol(sym.FLOAT_LITERAL, new Float(yytext()));}
"var"				{ return symbol(sym.VAR);}
"def"				{ return symbol(sym.DEF);}
"read"				{ return symbol(sym.READ);}
"type"              { return symbol(sym.TYPE);}
"int"				{ return symbol(sym.INT);}
"boolean"           { return symbol(sym.BOOLEAN);}
"float"             { return symbol(sym.FLOAT);}
"string"            { return symbol(sym.STRING);}
"if"                { return symbol(sym.IF);}
"else"              { return symbol(sym.ELSE);}
"return"            { return symbol(sym.RETURN);}
"by"                { return symbol(sym.BY);}
"array"             { return symbol(sym.ARRAY);}
"for"               { return symbol(sym.FOR);}
"to"                { return symbol(sym.TO);}
"while"             { return symbol(sym.WHILE);}
"loop"              { return symbol(sym.LOOP);}
"print"           	{ return symbol(sym.PRINT);}
"error"             { return symbol(sym.error);}
"exit"              { return symbol(sym.EXIT);}
{DIGIT}+			{ return symbol(sym.INTEGER_LITERAL, new Integer(yytext()));}
{ID}				{ return symbol(sym.ID, yytext());}
.					{ lexical_error("Illegal character");}