package analisis;

import java_cup.runtime.Symbol;

%%

%{
    String cadena = "";
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

<YYINITIAL> [^]           { return new Symbol(sym.ERROR, yyline, yycolumn, yytext()); }
