# Gramatica BNF del lenguaje GoLite

Este documento resume la gramatica implementada en `parser.cup` en una forma cercana a Notacion Backus-Naur (BNF). Se usa como apoyo para la entrega del proyecto.

## Simbolos terminales

Los terminales principales del lenguaje son:

- Identificadores y literales: `IDENTIFICADOR`, `ENTERO`, `DECIMAL`, `CADENA`, `CHAR`
- Palabras reservadas: `FUNC`, `MAIN`, `IF`, `ELSE`, `FOR`, `BREAK`, `CONTINUE`, `RETURN`, `SWITCH`, `CASE`, `DEFAULT`, `STRUCT`
- Literales especiales: `TRUE`, `FALSE`, `NIL`
- Tipos: `INT`, `FLOAT64`, `STRING`, `BOOL`, `RUNE`
- Operadores: `MAS`, `MENOS`, `MULT`, `DIV`, `MOD`, `MASMAS`, `MENOSMENOS`, `IGUAL`, `MASIGUAL`, `MENOSIGUAL`, `DOSPUNTOSIGUAL`, `IGUALIGUAL`, `DIFERENTE`, `MAYOR`, `MAYORIGUAL`, `MENOR`, `MENORIGUAL`, `AND`, `OR`, `NOT`
- Delimitadores: `CORCHETE1`, `CORCHETE2`, `PAR1`, `PAR2`, `LLAVE1`, `LLAVE2`, `PUNTO`, `COMA`, `PTOCOMA`, `FIN_LINEA`, `DOSPUNTOS`
- Funciones/bibliotecas especiales: `FMT`, `STRCONV`, `REFLECT`, `TYPEOF`, `ATOI`, `PARSEFLOAT`, `PRINTLN`, `SLICES`, `INDEX`, `STRINGS`, `JOIN`, `LEN`, `APPEND`

## Sintaxis general

```bnf
<programa> ::= <declaraciones_programa> "func" "main" "(" ")" <bloque>
             | <declaraciones_programa> <instrucciones>
```

## Declaraciones globales

```bnf
<declaraciones_programa> ::= <declaraciones_programa> <struct_def>
                           | <declaraciones_programa> <struct_method_def>
                           | <declaraciones_programa> <function_def>
                           | epsilon
```

## Bloques e instrucciones

```bnf
<bloque> ::= "{" "}"
           | "{" <instrucciones> "}"
           | "{" error "}"

<instrucciones> ::= <instrucciones> <fin_de_linea>
                  | <instrucciones> ";"
                  | <instrucciones> <instruccion>
                  | <instruccion>

<instruccion> ::= <declaracion_var>
               | <asignacion>
               | <llamada>
               | <identificador> "++"
               | <identificador> "--"
               | <sentencia_if>
               | <sentencia_for>
               | <sentencia_switch>
               | <sentencia_break>
               | <sentencia_continue>
               | <bloque>
               | <return_stmt>
               | error <fin_de_linea>
               | error ";"
```

## Declaracion de variables

```bnf
<declaracion_var> ::= <tipo> <identificador> "=" <expresion>
                    | <tipo> <identificador> "=" "{" <lista_campos_init> "}"
                    | <identificador> <identificador> "=" <expresion>
                    | <identificador> <identificador> "=" "{" <lista_campos_init> "}"
                    | <identificador> ":=" <expresion>
```

## Asignaciones

```bnf
<asignacion> ::= <acceso> "=" <expresion>
              | <acceso> "+=" <expresion>
              | <acceso> "-=" <expresion>
```

## Llamadas

```bnf
<llamada> ::= <identificador> "(" <lista_expresiones> ")"
           | <identificador> "(" ")"
           | <llamada_funcion>
           | <struct_method_call>
```

## Sentencias de control

```bnf
<sentencia_if> ::= "if" <expresion> <bloque>
                | "if" <expresion> <bloque> "else" <bloque>
                | "if" <expresion> <bloque> "else" <sentencia_if>
                | "if" error <bloque>

<sentencia_for> ::= "for" <expresion> <bloque>
                | "for" <instruccion> ";" <expresion> ";" <instruccion> <bloque>
                | "for" error <bloque>

<sentencia_switch> ::= "switch" <expresion> "{" <lista_casos_switch> <switch_default> "}"
                   | "switch" <expresion> "{" <lista_casos_switch> "}"
                   | "switch" <expresion> "{" <switch_default> "}"
                   | "switch" <expresion> "{" "}"

<lista_casos_switch> ::= <lista_casos_switch> <caso_switch>
                      | <caso_switch>

<caso_switch> ::= "case" <lista_expresiones> ":" <instruccion>

<switch_default> ::= "default" ":" <instruccion>

<sentencia_break> ::= "break"

<sentencia_continue> ::= "continue"
```

## Tipos

```bnf
<tipo> ::= "int"
        | "float64"
        | "string"
        | "bool"
        | "rune"
        | "[" "]" <tipo>
```

## Expresiones

```bnf
<expresion> ::= <expresion> "||" <expresion>
             | <expresion> "&&" <expresion>
             | <expresion> "==" <expresion>
             | <expresion> "!=" <expresion>
             | <expresion> ">" <expresion>
             | <expresion> ">=" <expresion>
             | <expresion> "<" <expresion>
             | <expresion> "<=" <expresion>
             | <expresion> "+" <expresion>
             | <expresion> "-" <expresion>
             | <expresion> "*" <expresion>
             | <expresion> "/" <expresion>
             | <expresion> "%" <expresion>
             | "-" <expresion>
             | "!" <expresion>
             | <entero>
             | <decimal>
             | <cadena>
             | <char>
             | "true"
             | "false"
             | "nil"
             | <acceso>
             | <identificador> "(" <lista_expresiones> ")"
             | <identificador> "(" ")"
             | "(" <expresion> ")"
             | <llamada_funcion>
             | "len" "(" <expresion> ")"
             | <struct_access>
             | <struct_method_call>
             | <new_struct>
             | "append" "(" <expresion> "," <lista_expresiones> ")"
             | "[" "]" <tipo> "{" <lista_expresiones> "}"
             | "[" "]" <identificador> "{" <lista_expresiones> "}"
             | "[" "]" <tipo> "{" "}"
             | "[" "]" <identificador> "{" "}"
             | "[" "]" <tipo> "{" <filas_2d> "}"
             | "[" "]" <identificador> "{" <filas_2d> "}"
```

## Accesos e indices

```bnf
<acceso> ::= <identificador>
          | <acceso> "[" <expresion> "]"
```

## Literales compuestos

```bnf
<new_struct> ::= <identificador> "{" <lista_campos_init> "}"
              | <identificador> "{" "}"

<lista_campos_init> ::= <lista_campos_init> "," <campo_init>
                     | <campo_init>

<campo_init> ::= <identificador> ":" <expresion>

<filas_2d> ::= <filas_2d> "," <fila_2d>
            | <fila_2d>

<fila_2d> ::= "{" <lista_expresiones> "}"
```

## Structs

```bnf
<struct_def> ::= "struct" <identificador> "{" <campos> "}"

<campos> ::= <campos> <campo>
         | <campos> <fin_de_linea>
         | <campos> ";"
         | <campo>

<campo> ::= <tipo> <identificador>
         | <identificador> <identificador>

<struct_access> ::= <identificador> "." <identificador>
```

## Funciones y metodos

```bnf
<function_def> ::= "func" <identificador> "(" <params> ")" <tipo_opcional> <bloque>

<struct_method_def> ::= "func" "(" <identificador> <identificador> ")" <identificador> "(" <params> ")" <bloque>

<struct_method_call> ::= <identificador> "." <identificador> "(" <lista_expresiones> ")"
                     | <identificador> "." <identificador> "(" ")"

<return_stmt> ::= "return" <expresion>
              | "return" <fin_de_linea>

<params> ::= <params> "," <param>
         | <param>
         | epsilon

<param> ::= <identificador> <tipo>
         | <identificador> <identificador>

<tipo_opcional> ::= <tipo>
                | <identificador>
                | epsilon
```

## Listas auxiliares

```bnf
<lista_expresiones> ::= <lista_expresiones> "," <expresion>
                    | <expresion>
```

## Observaciones

- La gramatica real contiene acciones semanticas de CUP para construir el AST.
- Las reglas con `error` representan recuperacion de errores sintacticos.
