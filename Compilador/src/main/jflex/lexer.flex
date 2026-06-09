package analisis;

import java_cup.runtime.Symbol;

%%

%{
    String cadena = "";
    int comentarioBloque = 0;
%}

%init{
    yyline = 1;
    yycolumn = 1;
%init}

%eofval{
    return new Symbol(sym.EOF, yyline, yycolumn, yytext());
%eofval}

%cup
%class Lexer
%public
%line
%column
%full
%state CADENA
%state COMENTARIO_LINEA
%state COMENTARIO_BLOQUE

digit      = [0-9]
letter     = [a-zA-Z]
underscore = "_"
id         = ({letter}|{underscore})({letter}|{digit}|{underscore})*
integer    = [0-9]+
decimal    = [0-9]+"."[0-9]+
whitespace = [\ \r\t\f]+
newline    = [\n]
escape_seq = \\ [\"\\nrt]
normal_char = [^\"\\\n\r]
str_content = ({normal_char}|{escape_seq})*

%%

<YYINITIAL> {whitespace}  { /* ignorar */ }
<YYINITIAL> {newline}     { /* ignorar */ }

<YYINITIAL> "var"         { return new Symbol(sym.VAR, yyline, yycolumn, yytext()); }
<YYINITIAL> "func"        { return new Symbol(sym.FUNC, yyline, yycolumn, yytext()); }
<YYINITIAL> "main"        { return new Symbol(sym.MAIN, yyline, yycolumn, yytext()); }
<YYINITIAL> "if"          { return new Symbol(sym.IF, yyline, yycolumn, yytext()); }
<YYINITIAL> "else"        { return new Symbol(sym.ELSE, yyline, yycolumn, yytext()); }
<YYINITIAL> "for"         { return new Symbol(sym.FOR, yyline, yycolumn, yytext()); }
<YYINITIAL> "break"       { return new Symbol(sym.BREAK, yyline, yycolumn, yytext()); }
<YYINITIAL> "continue"    { return new Symbol(sym.CONTINUE, yyline, yycolumn, yytext()); }
<YYINITIAL> "int"         { return new Symbol(sym.INT, yyline, yycolumn, yytext()); }
<YYINITIAL> "float64"     { return new Symbol(sym.FLOAT64, yyline, yycolumn, yytext()); }
<YYINITIAL> "string"      { return new Symbol(sym.STRING, yyline, yycolumn, yytext()); }
<YYINITIAL> "bool"        { return new Symbol(sym.BOOL, yyline, yycolumn, yytext()); }
<YYINITIAL> "rune"        { return new Symbol(sym.RUNE, yyline, yycolumn, yytext()); }
<YYINITIAL> "true"        { return new Symbol(sym.TRUE, yyline, yycolumn, yytext()); }
<YYINITIAL> "false"       { return new Symbol(sym.FALSE, yyline, yycolumn, yytext()); }
<YYINITIAL> "nil"         { return new Symbol(sym.NIL, yyline, yycolumn, yytext()); }
<YYINITIAL> "fmt"         { return new Symbol(sym.FMT, yyline, yycolumn, yytext()); }
<YYINITIAL> "strconv"     { return new Symbol(sym.STRCONV, yyline, yycolumn, yytext()); }
<YYINITIAL> "reflect"     { return new Symbol(sym.REFLECT, yyline, yycolumn, yytext()); }
<YYINITIAL> "Println"     { return new Symbol(sym.PRINTLN, yyline, yycolumn, yytext()); }
<YYINITIAL> "TypeOf"      { return new Symbol(sym.TYPEOF, yyline, yycolumn, yytext()); }
<YYINITIAL> "Atoi"        { return new Symbol(sym.ATOI, yyline, yycolumn, yytext()); }
<YYINITIAL> "ParseFloat"  { return new Symbol(sym.PARSEFLOAT, yyline, yycolumn, yytext()); }
<YYINITIAL> "String"      { return new Symbol(sym.STRINGFUNC, yyline, yycolumn, yytext()); }

<YYINITIAL> "=="          { return new Symbol(sym.IGUALIGUAL, yyline, yycolumn, yytext()); }
<YYINITIAL> "!="          { return new Symbol(sym.DIFERENTE, yyline, yycolumn, yytext()); }
<YYINITIAL> ">="          { return new Symbol(sym.MAYORIGUAL, yyline, yycolumn, yytext()); }
<YYINITIAL> "<="          { return new Symbol(sym.MENORIGUAL, yyline, yycolumn, yytext()); }
<YYINITIAL> "+="          { return new Symbol(sym.MASIGUAL, yyline, yycolumn, yytext()); }
<YYINITIAL> "-="          { return new Symbol(sym.MENOSIGUAL, yyline, yycolumn, yytext()); }
<YYINITIAL> ":="          { return new Symbol(sym.DOSPUNTOSIGUAL, yyline, yycolumn, yytext()); }
<YYINITIAL> "&&"          { return new Symbol(sym.AND, yyline, yycolumn, yytext()); }
<YYINITIAL> "||"          { return new Symbol(sym.OR, yyline, yycolumn, yytext()); }
<YYINITIAL> ">"           { return new Symbol(sym.MAYOR, yyline, yycolumn, yytext()); }
<YYINITIAL> "<"           { return new Symbol(sym.MENOR, yyline, yycolumn, yytext()); }
<YYINITIAL> "+"           { return new Symbol(sym.MAS, yyline, yycolumn, yytext()); }
<YYINITIAL> "-"           { return new Symbol(sym.MENOS, yyline, yycolumn, yytext()); }
<YYINITIAL> "*"           { return new Symbol(sym.MULT, yyline, yycolumn, yytext()); }
<YYINITIAL> "/"           { return new Symbol(sym.DIV, yyline, yycolumn, yytext()); }
<YYINITIAL> "%"           { return new Symbol(sym.MOD, yyline, yycolumn, yytext()); }
<YYINITIAL> "//"          { yybegin(COMENTARIO_LINEA); }
<YYINITIAL> "/*"          { comentarioBloque = 1; yybegin(COMENTARIO_BLOQUE); }
<YYINITIAL> "\""          { cadena = ""; yybegin(CADENA); }
<YYINITIAL> "'"[^'\\]"'" { return new Symbol(sym.CHAR, yyline, yycolumn, yytext()); }
<YYINITIAL> "'\\"[\\'nrt]"'" { return new Symbol(sym.CHAR, yyline, yycolumn, yytext()); }
<YYINITIAL> "="           { return new Symbol(sym.IGUAL, yyline, yycolumn, yytext()); }
<YYINITIAL> "!"           { return new Symbol(sym.NOT, yyline, yycolumn, yytext()); }
<YYINITIAL> "("           { return new Symbol(sym.PAR1, yyline, yycolumn, yytext()); }
<YYINITIAL> ")"           { return new Symbol(sym.PAR2, yyline, yycolumn, yytext()); }
<YYINITIAL> "{"           { return new Symbol(sym.LLAVE1, yyline, yycolumn, yytext()); }
<YYINITIAL> "}"           { return new Symbol(sym.LLAVE2, yyline, yycolumn, yytext()); }
<YYINITIAL> ";"           { return new Symbol(sym.PTOCOMA, yyline, yycolumn, yytext()); }
<YYINITIAL> "."           { return new Symbol(sym.PUNTO, yyline, yycolumn, yytext()); }
<YYINITIAL> ","           { return new Symbol(sym.COMA, yyline, yycolumn, yytext()); }

<YYINITIAL> {decimal}     { return new Symbol(sym.DECIMAL, yyline, yycolumn, yytext()); }
<YYINITIAL> {integer}     { return new Symbol(sym.ENTERO, yyline, yycolumn, yytext()); }
<YYINITIAL> {id}          { return new Symbol(sym.IDENTIFICADOR, yyline, yycolumn, yytext()); }

<COMENTARIO_LINEA> [^\n\r]+ { /* ignorar */ }
<COMENTARIO_LINEA> \n       { yybegin(YYINITIAL); }
<COMENTARIO_LINEA> \r       { yybegin(YYINITIAL); }

<COMENTARIO_BLOQUE> "/*"   { comentarioBloque++; }
<COMENTARIO_BLOQUE> "*/"   { comentarioBloque--; if (comentarioBloque == 0) yybegin(YYINITIAL); }
<COMENTARIO_BLOQUE> [^]    { /* ignorar */ }

<CADENA> "\""              { yybegin(YYINITIAL); return new Symbol(sym.CADENA, yyline, yycolumn, cadena); }
<CADENA> "\\n"             { cadena += "\n"; }
<CADENA> "\\r"             { cadena += "\r"; }
<CADENA> "\\t"             { cadena += "\t"; }
<CADENA> "\\\\"            { cadena += "\\"; }
<CADENA> "\\\""            { cadena += "\""; }
<CADENA> [^\"\\\n\r]       { cadena += yytext(); }
<CADENA> \n                { yybegin(YYINITIAL); return new Symbol(sym.ERROR, yyline, yycolumn, "cadena no terminada"); }

<YYINITIAL> [^]           { return new Symbol(sym.ERROR, yyline, yycolumn, yytext()); }
