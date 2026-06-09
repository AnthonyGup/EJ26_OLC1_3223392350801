# Plan Lexer + Parser — Fase 1 (basado en ejemplo oficial)

## Referencia

Este plan sigue el estilo del proyecto ejemplo en `EVJ26_OLC1/clase05/`:
- **Lexer**: macros con nombres similares (`digit`, `letter`, `id`, `whitespace`, `integer`, `decimal`), `%eofval` retornando `sym.EOF`, cada regla retorna `new Symbol(sym.xxx, yyline, yycolumn, yytext())`
- **Parser**: usa `%prec` para unario menos, labels (`SYMBOL:label`) para acceder a valores, `precedence` para operadores, `terminal String xxx` para tokens con valor, gramática plana con una sola `EXPRESION`

## Principios

1. **Incremental** — cada paso se compila y verifica antes del siguiente.
2. **Sí usar `%prec`** — el ejemplo oficial lo usa con `UMENOS`, funciona en CUP 11b vbmacher.
3. **Sí usar labels** — `EXPRESION:izq EXPRESION:der`, funcionan en CUP 11b vbmacher.
4. **Gramática plana** — una sola `EXPRESION` no-terminal con `%prec`, en lugar de niveles separados.
5. **Dangling else** — se resuelve con shift por defecto de CUP (el comportamiento correcto).
6. **Sin acciones hasta Fase C** — primero la gramática vacía debe compilar sin conflictos.

---

## Fase A — Lexer (`lexer.flex`)

Cada paso: agregar reglas → `mvn clean compile` → verificar que genera `Lexer.java`.

| Paso | Cambio | Reglas agregadas |
|------|--------|------------------|
| A1 | **Base funcional** | Setup `%cup %class Lexer %public %line %column %full`, `%init{ yyline=1; yycolumn=1; %init}`, `%eofval { return new Symbol(sym.EOF,...) }`, macros `digit letter underscore id integer decimal whitespace newline`, ignorar blancos/saltos, catch-all `[^]` → `sym.ERROR` |
| A2 | **Palabras reservadas** | `"var"` → `sym.VAR`, `"func"` → `sym.FUNC`, `"main"` → `sym.MAIN`, `"if"` → `sym.IF`, `"else"` → `sym.ELSE`, `"for"` → `sym.FOR`, `"break"` → `sym.BREAK`, `"continue"` → `sym.CONTINUE` (orden: keywords antes que `{id}`) |
| A3 | **Tipos y literales** | `"int"` → `sym.INT`, `"float64"` → `sym.FLOAT64`, `"string"` → `sym.STRING`, `"bool"` → `sym.BOOL`, `"rune"` → `sym.RUNE`, `"true"` → `sym.TRUE`, `"false"` → `sym.FALSE`, `"nil"` → `sym.NIL` |
| A4 | **Funciones embebidas** | `"fmt"` → `sym.FMT`, `"strconv"` → `sym.STRCONV`, `"reflect"` → `sym.REFLECT`, `"Println"` → `sym.PRINTLN`, `"TypeOf"` → `sym.TYPEOF`, `"Atoi"` → `sym.ATOI`, `"ParseFloat"` → `sym.PARSEFLOAT`, `"String"` → `sym.STRINGFUNC` (`.` se maneja como token separado) |
| A5 | **Operadores y símbolos** | `"+"` `MAS`, `"-"` `MENOS`, `"*"` `MULT`, `"/"` `DIV`, `"%"` `MOD`, `"=="` `IGUALIGUAL`, `"!="` `DIFERENTE`, `">"` `MAYOR`, `">="` `MAYORIGUAL`, `"<"` `MENOR`, `"<="` `MENORIGUAL`, `"&&"` `AND`, `"||"` `OR`, `"!"` `NOT`, `"="` `IGUAL`, `"+="` `MASIGUAL`, `"-="` `MENOSIGUAL`, `":="` `DOSPUNTOSIGUAL`, `"("` `PAR1`, `")"` `PAR2`, `"{"` `LLAVE1`, `"}"` `LLAVE2`, `";"` `PTOCOMA`, `"."` `PUNTO`, `","` `COMA` (orden: operadores multi-char antes que single-char) |
| A6 | **Patrones** | `{id}` → `sym.IDENTIFICADOR`, `{decimal}` → `sym.DECIMAL`, `{integer}` → `sym.ENTERO` |
| A7 | **Estados especiales** | `\"` inicia estado `CADENA` con `str_content` y escapes `\n \r \t \\ \"`; `"//"` ignora hasta salto de línea; `"/*"` inicia estado `COMENTARIO_BLOQUE` con anidación (`"/*"` incrementa contador, `"*/"` decrementa, vuelve a YYINITIAL cuando contador=0) |
| A8 | **Ajustes finales** | Verificar que `{id}` está después de keywords (JFlex prioriza reglas más largas o primer match); probar con entrada de ejemplo |

---

## Fase B — Parser sin acciones (`parser.cup`)

Gramática plana con `%prec` y labels, siguiendo el estilo del ejemplo oficial.

| Paso | Cambio | Reglas agregadas |
|------|--------|------------------|
| B1 | **Skeleton** | Package `analisis`, `import` de CUP, `java.util.*`, `analisis.ast.*`, `analisis.ast.exp.*`, `analisis.ast.stm.*`. `parser code` con `syntax_error` y `unrecovered_syntax_error`. `action code` vacío. |
| B2 | **Terminales** | Declarar todos los tokens del lexer con sus tipos: `terminal String CADENA, ENTERO, DECIMAL, IDENTIFICADOR, CHAR, ERROR;`, `terminal VAR, FUNC, MAIN, IF, ELSE, FOR, BREAK, CONTINUE;`, etc. Incluir pseudo-terminal `UMENOS` para `%prec`. |
| B3 | **No-terminales** | `nonterminal Programa PROGRAMA;`, `NodoAST FUNCION_MAIN, BLOQUE, INSTRUCCION, DECLARACION_VAR, ASIGNACION, ASIGNACION_OP, SENTENCIA_IF, SENTENCIA_FOR, SENTENCIA_BREAK, SENTENCIA_CONTINUE, LLAMADA_FUNCION, EXPRESION;`, `String TIPO;`, `List INSTRUCCIONES, LISTA_EXPRESIONES;` |
| B4 | **Precedencia** | `precedence left OR`, `left AND`, `left IGUALIGUAL, DIFERENTE`, `left MAYOR, MAYORIGUAL, MENOR, MENORIGUAL`, `left MAS, MENOS`, `left MULT, DIV, MOD`, `right NOT`, `right UMENOS` |
| B5 | **PROGRAMA + FUNCION_MAIN + BLOQUE** | `PROGRAMA ::= FUNCION_MAIN`, `FUNCION_MAIN ::= FUNC MAIN PAR1 PAR2 BLOQUE`, `BLOQUE ::= LLAVE1 INSTRUCCIONES LLAVE2` |
| B6 | **Instrucciones** | `INSTRUCCIONES ::= INSTRUCCIONES INSTRUCCION | INSTRUCCION`, `INSTRUCCION ::= DECLARACION_VAR | ASIGNACION | ASIGNACION_OP | SENTENCIA_IF | SENTENCIA_FOR | SENTENCIA_BREAK | SENTENCIA_CONTINUE | BLOQUE` |
| B7 | **Declaración/Asignación** | `DECLARACION_VAR ::= VAR IDENTIFICADOR TIPO IGUAL EXPRESION | VAR IDENTIFICADOR TIPO | IDENTIFICADOR DOSPUNTOSIGUAL EXPRESION`, `ASIGNACION ::= IDENTIFICADOR IGUAL EXPRESION`, `ASIGNACION_OP ::= IDENTIFICADOR MASIGUAL EXPRESION | IDENTIFICADOR MENOSIGUAL EXPRESION` |
| B8 | **If-Else** | `SENTENCIA_IF ::= IF EXPRESION BLOQUE | IF EXPRESION BLOQUE ELSE BLOQUE | IF EXPRESION BLOQUE ELSE SENTENCIA_IF` (el shift/resolve default de CUP resuelve dangling else correctamente: 1 conflicto esperado) |
| B9 | **For** | `SENTENCIA_FOR ::= FOR EXPRESION BLOQUE | FOR INSTRUCCION EXPRESION PTOCOMA INSTRUCCION BLOQUE` |
| B10 | **Break/Continue** | `SENTENCIA_BREAK ::= BREAK`, `SENTENCIA_CONTINUE ::= CONTINUE` |
| B11 | **LlamadaFuncion + Tipo** | `LLAMADA_FUNCION ::= FMT PUNTO PRINTLN PAR1 LISTA_EXPRESIONES PAR2 | ...` (4 formas), `LISTA_EXPRESIONES ::= LISTA_EXPRESIONES COMA EXPRESION | EXPRESION`, `TIPO ::= INT | FLOAT64 | STRING | BOOL | RUNE` |
| B12 | **Expresión** | `EXPRESION ::= EXPRESION OR EXPRESION | EXPRESION AND EXPRESION | EXPRESION IGUALIGUAL EXPRESION | ... | MENOS EXPRESION %prec UMENOS | NOT EXPRESION | ENTERO | DECIMAL | CADENA | CHAR | TRUE | FALSE | NIL | IDENTIFICADOR | PAR1 EXPRESION PAR2 | LLAMADA_FUNCION` |

---

## Fase C — Acciones semánticas

Cada paso: agregar acciones a un grupo de reglas → `mvn clean compile` → probar con entrada simple.

Se usan labels estilo `EXPRESION:izq EXPRESION:der` y se accede como `izq`, `der` en la acción.

| Paso | Grupo | Acción |
|------|-------|--------|
| C1 | PROGRAMA, FUNCION_MAIN, BLOQUE | `RESULT = new Programa(FUNCION_MAIN);`, `RESULT = new FuncionMain(BLOQUE);`, `RESULT = new Bloque((List)INSTRUCCIONES);` |
| C2 | INSTRUCCIONES, INSTRUCCION | `is.add(i); RESULT = is;`, `RESULT = new Statments(i);` (o `RESULT = i;` según diseño) |
| C3 | DECLARACION_VAR, ASIGNACION, ASIGNACION_OP | `new DeclaracionVar(ID, TIPO, EXPRESION,...)`, `new Asignacion(...)`, `new AsignacionOp(...)` |
| C4 | SENTENCIA_IF | `new If(EXPRESION, BLOQUE)` y `new If(EXPRESION, BLOQUE, BLOQUE)` / `new If(EXPRESION, BLOQUE, SENTENCIA_IF)` |
| C5 | SENTENCIA_FOR | `new For(EXPRESION, BLOQUE)` y `new For(INSTRUCCION:i, EXPRESION, INSTRUCCION:u, BLOQUE)` |
| C6 | BREAK, CONTINUE | `new Break()`, `new Continue()` |
| C7 | LLAMADA_FUNCION, LISTA_EXPRESIONES | `new LlamadaFuncion(...)`, construcción de lista |
| C8 | TIPO | `RESULT = "int";`, `RESULT = "float64";`, etc. |
| C9 | EXPRESION binarias | `new ExpBinaria(izq, op, der, izqleft, derright)` — se pasa el operador como string: `"+"`, `"-"`, etc. (usando `new Symbol(...)` para obtener el texto o hardcodeando según el terminal) |
| C10 | EXPRESION unarias | `new ExpUnaria("-", e, eleft, eright)` para `MENOS EXPRESION %prec UMENOS`, `new ExpUnaria("!", e, ...)` para `NOT EXPRESION` |
| C11 | EXPRESION primarias | `new Literal(TipoDato.ENTERO, ENTERO, ...)`, `new Identificador(ID, ...)`, paso de `LLAMADA_FUNCION` |

---

## Fase D — Integración y pruebas

| Paso | Descripción |
|------|-------------|
| D1 | Crear `Compilador.java` temporal que enlaza lexer→parser |
| D2 | Probar con `test/entrada.golite` (programa mínimo: `func main() {}`) |
| D3 | Probar con todas las construcciones del lenguaje |
| D4 | Verificar que el AST se construye correctamente |

---

## Notas técnicas

### Labels en CUP

Funcionan con sintaxis `SYMBOL:label` y se accede como `label` en la acción:

```
EXPRESION ::= EXPRESION:izq MAS EXPRESION:der
              {: RESULT = new ExpBinaria(izq, "+", der, izqleft, derright); :}
;
```

### `%prec` para unario menos

```
precedence right UMENOS;
EXPRESION ::= MENOS EXPRESION:e %prec UMENOS
              {: RESULT = new ExpUnaria("-", e, eleft, eright); :}
;
```

### Dangling else

La gramática:
```
SENTENCIA_IF ::= IF EXPRESION BLOQUE
               | IF EXPRESION BLOQUE ELSE BLOQUE
               | IF EXPRESION BLOQUE ELSE SENTENCIA_IF
```
Produce 1 shift/reduce conflict (dangling else). CUP resuelve por defecto con shift, que es el comportamiento correcto (el `else` se asocia al `if` más cercano). Confirmar con `mvn compile` que solo sea 1 conflicto (o que se resuelva automáticamente sin error).

### Orden de reglas en JFlex

1. Palabras reservadas (`"var"`, `"if"`, `"int"`, etc.) — antes que `{id}`
2. Operadores multi-char (`"=="`, `">="`, `"+="`, etc.) — antes que single-char (`"="`, `">"`, `"+"`)
3. Patrones (`{decimal}`, `{integer}`, `{id}`) — después de todo lo anterior
4. Estados especiales (`<CADENA>`, `<COMENTARIO_BLOQUE>`) — donde corresponda
5. Catch-all (`[^]`) — al final

### Cambios respecto al plan anterior

| Anterior | Nuevo | Motivo |
|----------|-------|--------|
| 7 niveles de EXPRESION (`_OR`, `_AND`, `_COMP`, etc.) | 1 sola EXPRESION con `%prec` | El ejemplo oficial demuestra que `%prec` funciona |
| Wrappers `ELSE_BODY`, `FOR_INIT`, `FOR_UPDATE` | Labels directas | El ejemplo oficial demuestra que labels funcionan |
| Sin `%prec` | Con `%prec UMENOS` | Ejemplo oficial lo usa y compila |
| `parser code` mínimo | `parser code` con `syntax_error` + `unrecovered_syntax_error` | Estilo del ejemplo |
