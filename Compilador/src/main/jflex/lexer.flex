package analisis;

// importaciones
import java_cup.runtime.Symbol;

%%

// Codido de usurio

%{
    String cadena = "";
%}

%init{
    yyline = 1;
    yycolumn = 1;
%init}

// Configuración de FLEX
%cup //Indicamos que vamos a usar CUP
%class Lexer
%public
%line // conteo de lienas
%column // conteo de columnas
%8bit  // recibir caracteres en formato UTF-8
// %debug 
//%ignorecase // ignorar mayusculas y minusculas

%state CADENA

//Macros
Letra = [a-zA-Z]
Digito = [0-9]
GuionBajo = "_"

// simbolos del sistema
PAR1 = "("
PAR2 = ")"
FINCADENA = ";"
MAS = "+"
MENOS = "-"
MULT = "*"
DIV = "/"
BLANCOS = [\ \r\t\f\n]+
ENTERO = [0-9]+
DECIMAL = [0-9]+"."[0-9]+

// Palabras reservadas
IMPRIMIR = "imprimir"



//identificador
IDENTIFICADOR = ({Letra}|{GuionBajo})({Letra}|{Digito}|{GuionBajo})*

%%
// primero se ponen las reservadas <NOMBRE_TOKEN, LEXEMA>
<YYINITIAL> {IMPRIMIR}  {return new Symbol(sym.IMPRIMIR, yyline, yycolumn, yytext());}

<YYINITIAL> {DECIMAL}  {return new Symbol(sym.DECIMAL, yyline, yycolumn, yytext());}
<YYINITIAL> {ENTERO}  {return new Symbol(sym.ENTERO, yyline, yycolumn, yytext());}

//SIMBOLOS
<YYINITIAL> {FINCADENA}  {return new Symbol(sym.FINCADENA, yyline, yycolumn, yytext());}
<YYINITIAL> {MAS}  {return new Symbol(sym.MAS, yyline, yycolumn, yytext());}
<YYINITIAL> {MENOS}  {return new Symbol(sym.MENOS, yyline, yycolumn, yytext());}
<YYINITIAL> {MULT}  {return new Symbol(sym.MULT, yyline, yycolumn, yytext());}
<YYINITIAL> {DIV}  {return new Symbol(sym.DIV, yyline, yycolumn, yytext());}
<YYINITIAL> {PAR1}  {return new Symbol(sym.PAR1, yyline, yycolumn, yytext());}
<YYINITIAL> {PAR2}  {return new Symbol(sym.PAR2, yyline, yycolumn, yytext());}

<YYINITIAL> {IDENTIFICADOR}  {return  new  Symbol(sym.IDENTIFICADOR, yyline, yycolumn, yytext());}

<YYINITIAL> {BLANCOS}  {/*Ignorar*/}

// Moverme a estado cadena
<YYINITIAL> [\"]    {yybegin(CADENA); cadena=""; }

<CADENA> {
    [\"]    {String temporal = cadena; cadena = "";
            yybegin(YYINITIAL);
            return new Symbol(sym.CADENA, yyline, yycolumn, temporal);}
    [^\"]   {cadena+=yytext();}
}
