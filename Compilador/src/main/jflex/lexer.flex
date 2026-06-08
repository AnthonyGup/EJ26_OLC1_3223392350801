package analisis;

import java_cup.runtime.Symbol;

%%

%{
    String cadena = "";
    String comentario = "";
%}

%init{
    yyline = 1;
    yycolumn = 1;
%init}

%cup
%class Lexer
%public
%line
%column
%full
%state CADENA
%state COMENTARIO_BLOQUE

Letra = [a-zA-Z]
Digito = [0-9]
GuionBajo = "_"
ID = ({Letra}|{GuionBajo})({Letra}|{Digito}|{GuionBajo})*
ENTERO = [0-9]+
DECIMAL = [0-9]+"."[0-9]+
BLANCOS = [\ \r\t\f]+
SALTO = [\n]

%%

<YYINITIAL> "var"              { return new Symbol(sym.VAR, yyline, yycolumn, yytext()); }
<YYINITIAL> "func"             { return new Symbol(sym.FUNC, yyline, yycolumn, yytext()); }
<YYINITIAL> "main"             { return new Symbol(sym.MAIN, yyline, yycolumn, yytext()); }
<YYINITIAL> "if"               { return new Symbol(sym.IF, yyline, yycolumn, yytext()); }
<YYINITIAL> "else"             { return new Symbol(sym.ELSE, yyline, yycolumn, yytext()); }
<YYINITIAL> "for"              { return new Symbol(sym.FOR, yyline, yycolumn, yytext()); }
<YYINITIAL> "break"            { return new Symbol(sym.BREAK, yyline, yycolumn, yytext()); }
<YYINITIAL> "continue"         { return new Symbol(sym.CONTINUE, yyline, yycolumn, yytext()); }
<YYINITIAL> "true"             { return new Symbol(sym.TRUE, yyline, yycolumn, yytext()); }
<YYINITIAL> "false"            { return new Symbol(sym.FALSE, yyline, yycolumn, yytext()); }
<YYINITIAL> "nil"              { return new Symbol(sym.NIL, yyline, yycolumn, yytext()); }
<YYINITIAL> "int"              { return new Symbol(sym.INT, yyline, yycolumn, yytext()); }
<YYINITIAL> "float64"          { return new Symbol(sym.FLOAT64, yyline, yycolumn, yytext()); }
<YYINITIAL> "string"           { return new Symbol(sym.STRING, yyline, yycolumn, yytext()); }
<YYINITIAL> "bool"             { return new Symbol(sym.BOOL, yyline, yycolumn, yytext()); }
<YYINITIAL> "rune"             { return new Symbol(sym.RUNE, yyline, yycolumn, yytext()); }
<YYINITIAL> "println"          { return new Symbol(sym.PRINTLN, yyline, yycolumn, yytext()); }

<YYINITIAL> "fmt"              { return new Symbol(sym.FMT, yyline, yycolumn, yytext()); }
<YYINITIAL> "strconv"          { return new Symbol(sym.STRCONV, yyline, yycolumn, yytext()); }
<YYINITIAL> "reflect"          { return new Symbol(sym.REFLECT, yyline, yycolumn, yytext()); }
<YYINITIAL> "TypeOf"           { return new Symbol(sym.TYPEOF, yyline, yycolumn, yytext()); }
<YYINITIAL> "Atoi"             { return new Symbol(sym.ATOI, yyline, yycolumn, yytext()); }
<YYINITIAL> "ParseFloat"       { return new Symbol(sym.PARSEFLOAT, yyline, yycolumn, yytext()); }
<YYINITIAL> "String"           { return new Symbol(sym.STRINGFUNC, yyline, yycolumn, yytext()); }

<YYINITIAL> "="                { return new Symbol(sym.IGUAL, yyline, yycolumn, yytext()); }
<YYINITIAL> "+="               { return new Symbol(sym.MASIGUAL, yyline, yycolumn, yytext()); }
<YYINITIAL> "-="               { return new Symbol(sym.MENOSIGUAL, yyline, yycolumn, yytext()); }
<YYINITIAL> "+"                { return new Symbol(sym.MAS, yyline, yycolumn, yytext()); }
<YYINITIAL> "-"                { return new Symbol(sym.MENOS, yyline, yycolumn, yytext()); }
<YYINITIAL> "*"                { return new Symbol(sym.MULT, yyline, yycolumn, yytext()); }
<YYINITIAL> "/"                { return new Symbol(sym.DIV, yyline, yycolumn, yytext()); }
<YYINITIAL> "%"                { return new Symbol(sym.MOD, yyline, yycolumn, yytext()); }
<YYINITIAL> "=="               { return new Symbol(sym.IGUALIGUAL, yyline, yycolumn, yytext()); }
<YYINITIAL> "!="               { return new Symbol(sym.DIFERENTE, yyline, yycolumn, yytext()); }
<YYINITIAL> ">"                { return new Symbol(sym.MAYOR, yyline, yycolumn, yytext()); }
<YYINITIAL> ">="               { return new Symbol(sym.MAYORIGUAL, yyline, yycolumn, yytext()); }
<YYINITIAL> "<"                { return new Symbol(sym.MENOR, yyline, yycolumn, yytext()); }
<YYINITIAL> "<="               { return new Symbol(sym.MENORIGUAL, yyline, yycolumn, yytext()); }
<YYINITIAL> "&&"               { return new Symbol(sym.AND, yyline, yycolumn, yytext()); }
<YYINITIAL> "||"               { return new Symbol(sym.OR, yyline, yycolumn, yytext()); }
<YYINITIAL> "!"                { return new Symbol(sym.NOT, yyline, yycolumn, yytext()); }
<YYINITIAL> "("                { return new Symbol(sym.PAR1, yyline, yycolumn, yytext()); }
<YYINITIAL> ")"                { return new Symbol(sym.PAR2, yyline, yycolumn, yytext()); }
<YYINITIAL> "{"                { return new Symbol(sym.LLAVE1, yyline, yycolumn, yytext()); }
<YYINITIAL> "}"                { return new Symbol(sym.LLAVE2, yyline, yycolumn, yytext()); }
<YYINITIAL> ";"                { return new Symbol(sym.PTOCOMA, yyline, yycolumn, yytext()); }
<YYINITIAL> "."                { return new Symbol(sym.PUNTO, yyline, yycolumn, yytext()); }
<YYINITIAL> ","                { return new Symbol(sym.COMA, yyline, yycolumn, yytext()); }
<YYINITIAL> ":="               { return new Symbol(sym.DOSPUNTOSIGUAL, yyline, yycolumn, yytext()); }

<YYINITIAL> {ENTERO}           { return new Symbol(sym.ENTERO, yyline, yycolumn, yytext()); }
<YYINITIAL> {DECIMAL}          { return new Symbol(sym.DECIMAL, yyline, yycolumn, yytext()); }
<YYINITIAL> {ID}               { return new Symbol(sym.IDENTIFICADOR, yyline, yycolumn, yytext()); }

<YYINITIAL> {BLANCOS}          { /* ignorar */ }
<YYINITIAL> {SALTO}            { /* ignorar */ }

<YYINITIAL> "//"               { /* comentario linea - ignorar hasta salto */ }
<YYINITIAL> "/*"               { yybegin(COMENTARIO_BLOQUE); comentario = ""; }

<YYINITIAL> [\"]               { yybegin(CADENA); cadena = ""; }

<YYINITIAL> [\']               { /* literal rune */ return new Symbol(sym.CHAR, yyline, yycolumn, yytext()); }

<CADENA> {
    \"                          { yybegin(YYINITIAL); return new Symbol(sym.CADENA, yyline, yycolumn, cadena); }
    "\\\""                      { cadena += '"'; }
    "\\\\"                      { cadena += '\\'; }
    "\\n"                       { cadena += '\n'; }
    "\\r"                       { cadena += '\r'; }
    "\\t"                       { cadena += '\t'; }
    .                           { cadena += yytext(); }
}

<COMENTARIO_BLOQUE> {
    "*/"                        { yybegin(YYINITIAL); }
    [^]                         { /* ignorar */ }
}

[^]                             { return new Symbol(sym.ERROR, yyline, yycolumn, yytext()); }
