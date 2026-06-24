package analisis;

import java_cup.runtime.Symbol;
import usac.compi1.gui.reports.GoliteError;
import usac.compi1.gui.reports.TokenInfo;
import java.util.List;
import java.util.ArrayList;

%%

%{
    String cadena = "";
    int comentarioBloque = 0;
    int profundidad = 0;
    int ultimoToken = -1;
    public List<GoliteError> errores = new ArrayList<>();
    public List<TokenInfo> tokens = new ArrayList<>();

    private boolean esFinLinea() {
        switch (ultimoToken) {
            case sym.IDENTIFICADOR:
            case sym.ENTERO:
            case sym.DECIMAL:
            case sym.CHAR:
            case sym.CADENA:
            case sym.TRUE:
            case sym.FALSE:
            case sym.NIL:
            case sym.PAR2:
            case sym.MASMAS:
            case sym.MENOSMENOS:
            case sym.BREAK:
            case sym.CONTINUE:
                return profundidad > 0;
            default:
                return false;
        }
    }

    private Symbol token(int sym, String lexema, String tipo, int line, int col) {
        ultimoToken = sym;
        tokens.add(new TokenInfo(lexema, tipo, line, col));
        return new Symbol(sym, line, col, lexema);
    }
%}

%init{
    yyline = 1;
    yycolumn = 1;
%init}

%eofval{
    return new Symbol(sym.EOF, yyline, yycolumn, "");
%eofval}

%cup
%class Lexer
%public
%line
%column
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

%%

<YYINITIAL> {whitespace}  { /* ignorar */ }
<YYINITIAL> {newline}     {
    if (esFinLinea()) return token(sym.FIN_LINEA, "\\n", "newline", yyline, yycolumn);
}

<YYINITIAL> "func"        { return token(sym.FUNC, yytext(), "func", yyline, yycolumn); }
<YYINITIAL> "main"        { return token(sym.MAIN, yytext(), "main", yyline, yycolumn); }
<YYINITIAL> "var"         { return token(sym.VAR, yytext(), "var", yyline, yycolumn); }
<YYINITIAL> "if"          { return token(sym.IF, yytext(), "if", yyline, yycolumn); }
<YYINITIAL> "else"        { return token(sym.ELSE, yytext(), "else", yyline, yycolumn); }
<YYINITIAL> "for"         { return token(sym.FOR, yytext(), "for", yyline, yycolumn); }
<YYINITIAL> "break"       { return token(sym.BREAK, yytext(), "break", yyline, yycolumn); }
<YYINITIAL> "continue"    { return token(sym.CONTINUE, yytext(), "continue", yyline, yycolumn); }
<YYINITIAL> "switch"      { return token(sym.SWITCH, yytext(), "switch", yyline, yycolumn); }
<YYINITIAL> "case"        { return token(sym.CASE, yytext(), "case", yyline, yycolumn); }
<YYINITIAL> "default"     { return token(sym.DEFAULT, yytext(), "default", yyline, yycolumn); }
<YYINITIAL> "int"         { return token(sym.INT, yytext(), "int", yyline, yycolumn); }
<YYINITIAL> "float64"     { return token(sym.FLOAT64, yytext(), "float64", yyline, yycolumn); }
<YYINITIAL> "string"      { return token(sym.STRING, yytext(), "string", yyline, yycolumn); }
<YYINITIAL> "bool"        { return token(sym.BOOL, yytext(), "bool", yyline, yycolumn); }
<YYINITIAL> "rune"        { return token(sym.RUNE, yytext(), "rune", yyline, yycolumn); }
<YYINITIAL> "true"        { return token(sym.TRUE, yytext(), "bool", yyline, yycolumn); }
<YYINITIAL> "false"       { return token(sym.FALSE, yytext(), "bool", yyline, yycolumn); }
<YYINITIAL> "nil"         { return token(sym.NIL, yytext(), "nil", yyline, yycolumn); }
<YYINITIAL> "fmt"         { return token(sym.FMT, yytext(), "fmt", yyline, yycolumn); }
<YYINITIAL> "strconv"     { return token(sym.STRCONV, yytext(), "strconv", yyline, yycolumn); }
<YYINITIAL> "reflect"     { return token(sym.REFLECT, yytext(), "reflect", yyline, yycolumn); }
<YYINITIAL> "Println"     { return token(sym.PRINTLN, yytext(), "Println", yyline, yycolumn); }
<YYINITIAL> "TypeOf"      { return token(sym.TYPEOF, yytext(), "TypeOf", yyline, yycolumn); }
<YYINITIAL> "Atoi"        { return token(sym.ATOI, yytext(), "Atoi", yyline, yycolumn); }
<YYINITIAL> "ParseFloat"  { return token(sym.PARSEFLOAT, yytext(), "ParseFloat", yyline, yycolumn); }
<YYINITIAL> "slices"      { return token(sym.SLICES, yytext(), "slices", yyline, yycolumn); }
<YYINITIAL> "Index"       { return token(sym.INDEX, yytext(), "Index", yyline, yycolumn); }
<YYINITIAL> "strings"     { return token(sym.STRINGS, yytext(), "strings", yyline, yycolumn); }
<YYINITIAL> "Join"        { return token(sym.JOIN, yytext(), "Join", yyline, yycolumn); }
<YYINITIAL> "len"         { return token(sym.LEN, yytext(), "len", yyline, yycolumn); }
<YYINITIAL> "append"      { return token(sym.APPEND, yytext(), "append", yyline, yycolumn); }

<YYINITIAL> "=="          { return token(sym.IGUALIGUAL, yytext(), "==", yyline, yycolumn); }
<YYINITIAL> "!="          { return token(sym.DIFERENTE, yytext(), "!=", yyline, yycolumn); }
<YYINITIAL> ">="          { return token(sym.MAYORIGUAL, yytext(), ">=", yyline, yycolumn); }
<YYINITIAL> "<="          { return token(sym.MENORIGUAL, yytext(), "<=", yyline, yycolumn); }
<YYINITIAL> "+="          { return token(sym.MASIGUAL, yytext(), "+=", yyline, yycolumn); }
<YYINITIAL> "-="          { return token(sym.MENOSIGUAL, yytext(), "-=", yyline, yycolumn); }
<YYINITIAL> ":="          { return token(sym.DOSPUNTOSIGUAL, yytext(), ":=", yyline, yycolumn); }
<YYINITIAL> "&&"          { return token(sym.AND, yytext(), "&&", yyline, yycolumn); }
<YYINITIAL> "||"          { return token(sym.OR, yytext(), "||", yyline, yycolumn); }
<YYINITIAL> ">"           { return token(sym.MAYOR, yytext(), ">", yyline, yycolumn); }
<YYINITIAL> "<"           { return token(sym.MENOR, yytext(), "<", yyline, yycolumn); }
<YYINITIAL> "++"          { return token(sym.MASMAS, yytext(), "++", yyline, yycolumn); }
<YYINITIAL> "--"          { return token(sym.MENOSMENOS, yytext(), "--", yyline, yycolumn); }
<YYINITIAL> "+"           { return token(sym.MAS, yytext(), "+", yyline, yycolumn); }
<YYINITIAL> "-"           { return token(sym.MENOS, yytext(), "-", yyline, yycolumn); }
<YYINITIAL> "*"           { return token(sym.MULT, yytext(), "*", yyline, yycolumn); }
<YYINITIAL> "/"           { return token(sym.DIV, yytext(), "/", yyline, yycolumn); }
<YYINITIAL> "%"           { return token(sym.MOD, yytext(), "%", yyline, yycolumn); }
<YYINITIAL> "//"          { yybegin(COMENTARIO_LINEA); }
<YYINITIAL> "/*"          { comentarioBloque = 1; yybegin(COMENTARIO_BLOQUE); }
<YYINITIAL> "\""          { cadena = ""; yybegin(CADENA); }
<YYINITIAL> "'"[^'\\]"'" { return token(sym.CHAR, yytext(), "rune", yyline, yycolumn); }
<YYINITIAL> "'\\"[\\'nrt]"'" { return token(sym.CHAR, yytext(), "rune", yyline, yycolumn); }
<YYINITIAL> "="           { return token(sym.IGUAL, yytext(), "=", yyline, yycolumn); }
<YYINITIAL> "!"           { return token(sym.NOT, yytext(), "!", yyline, yycolumn); }
<YYINITIAL> "["           { return token(sym.CORCHETE1, yytext(), "[", yyline, yycolumn); }
<YYINITIAL> "]"           { return token(sym.CORCHETE2, yytext(), "]", yyline, yycolumn); }
<YYINITIAL> "("           { return token(sym.PAR1, yytext(), "(", yyline, yycolumn); }
<YYINITIAL> ")"           { return token(sym.PAR2, yytext(), ")", yyline, yycolumn); }
<YYINITIAL> "{"           { profundidad++; return token(sym.LLAVE1, yytext(), "{", yyline, yycolumn); }
<YYINITIAL> "}"           { profundidad--; return token(sym.LLAVE2, yytext(), "}", yyline, yycolumn); }
<YYINITIAL> ";"           { return token(sym.PTOCOMA, yytext(), ";", yyline, yycolumn); }
<YYINITIAL> "."           { return token(sym.PUNTO, yytext(), ".", yyline, yycolumn); }
<YYINITIAL> ","           { return token(sym.COMA, yytext(), ",", yyline, yycolumn); }
<YYINITIAL> ":"           { return token(sym.DOSPUNTOS, yytext(), ":", yyline, yycolumn); }

<YYINITIAL> {decimal}     { return token(sym.DECIMAL, yytext(), "float64", yyline, yycolumn); }
<YYINITIAL> {integer}     { return token(sym.ENTERO, yytext(), "int", yyline, yycolumn); }
<YYINITIAL> {id}          { return token(sym.IDENTIFICADOR, yytext(), "id", yyline, yycolumn); }

<COMENTARIO_LINEA> [^\n\r]+ { /* ignorar */ }
<COMENTARIO_LINEA> \n       { yybegin(YYINITIAL); }
<COMENTARIO_LINEA> \r       { yybegin(YYINITIAL); }

<COMENTARIO_BLOQUE> "/*"   { comentarioBloque++; }
<COMENTARIO_BLOQUE> "*/"   { comentarioBloque--; if (comentarioBloque == 0) yybegin(YYINITIAL); }
<COMENTARIO_BLOQUE> [^]    { /* ignorar */ }

<CADENA> "\""              { yybegin(YYINITIAL); return token(sym.CADENA, cadena, "string", yyline, yycolumn); }
<CADENA> "\\n"             { cadena += "\n"; }
<CADENA> "\\r"             { cadena += "\r"; }
<CADENA> "\\t"             { cadena += "\t"; }
<CADENA> "\\\\"            { cadena += "\\"; }
<CADENA> "\\\""            { cadena += "\""; }
<CADENA> [^\"\\\n\r]       { cadena += yytext(); }
<CADENA> \n                { yybegin(YYINITIAL); errores.add(new GoliteError("Lexico", "cadena no terminada", yyline, yycolumn)); }

<YYINITIAL> [^]           { errores.add(new GoliteError("Lexico", "caracter no reconocido: " + yytext(), yyline, yycolumn)); }
