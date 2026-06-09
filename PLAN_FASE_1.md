# Plan Fase 1 - GoLite

> **Basado en**: `[OLC1] Proyecto Fase 1.pdf` (versión oficial definitiva)
> **Estilo**: Ejemplo oficial en `EVJ26_OLC1/clase05/`
> **Última actualización**: 09/06/2026

---

## Estado del proyecto

### ✅ Completado

| Componente | Archivos | Notas |
|---|---|---|
| **AST (16 nodos)** | `analisis/ast/`, `ast/exp/`, `ast/stm/` | NodoAST interfaz genérica `<T>`, patrón Context, subpaquetes |
| **Visitor interfaz** | `analisis/visitor/Visitor.java` | Genérica `<T>`, 15 métodos `visit(Context)` |
| **VisitorSemantico** | `analisis/visitor/VisitorSemantico.java` | 412 líneas, validación tipos/ámbitos/break/continue completa |
| **Semántica** | `TipoDato.java`, `Simbolo.java`, `TablaSimbolos.java` | Enum con valorDefecto, stack de ámbitos |
| **Lexer skeleton** | `lexer.flex` | Setup base, macros, `%eofval`, estados CADENA/COMENTARIO_BLOQUE declarados |
| **Parser skeleton** | `parser.cup` | Gramática completa SIN acciones, 0 conflictos, usa `%prec` y labels |

### ✅ Resuelto (vs plan anterior)

| Problema anterior | Resolución |
|---|---|
| `%prec UMENOS` no funcionaba | **Sí funciona** — el error era del archivo previo, no de CUP. El ejemplo oficial lo usa. Ya compila OK. |
| Labels `SYMBOL:label` no funcionaban | **Sí funcionan** — el ejemplo oficial las usa. Ya compila OK. |
| Reduce/Reduce entre LLAMADA_FUNCION | Resuelto con la gramática plana (una sola EXPRESION con `%prec`). |

### ⛔ Pendiente

| Área | Archivos | Prioridad |
|------|----------|-----------|
| **Lexer completo** | `lexer.flex` — agregar reglas léxicas (Fase A) | Alta |
| **Parser con acciones** | `parser.cup` — agregar acciones semánticas (Fase C) | Alta |
| **VisitorEjecucion** | `visitor/ejecucion/VisitorEjecucion.java` + sistema `Valor` | Media |
| **Compilador.java** | Orquestar lexer + parser + visitors | Media |
| **GUI** | `VentanaPrincipal.java` (multitab, consola, reportes) | Media |
| **VisitorASTPrint** | `analisis/visitor/VisitorASTPrint.java` | Baja |
| **Reportes** | `ErrorDTO.java`, `TokenDTO.java`, `ReporteService.java` | Baja |

---

## Plan de construcción — Lexer (`lexer.flex`)

**Fase A: Agregar reglas incrementalmente**, cada paso compila y se verifica.

| Paso | Reglas | Detalle |
|------|--------|---------|
| A1 | **Base** | Setup `%cup %class Lexer %public %line %column %full`, `%init{ yyline=1; yycolumn=1; %init}`, `%eofval` retorna `sym.EOF`, macros (`digit`, `letter`, `id`, `decimal`, `integer`, `whitespace`, `newline`), ignorar blancos/saltos, catch-all `[^]` → `sym.ERROR` |
| A2 | **Palabras reservadas** | `var` `func` `main` `if` `else` `for` `break` `continue` (antes que `{id}`) |
| A3 | **Tipos y literales** | `int` `float64` `string` `bool` `rune` `true` `false` `nil` |
| A4 | **Funciones embebidas** | `fmt` `strconv` `reflect` `Println` `TypeOf` `Atoi` `ParseFloat` `String` |
| A5 | **Operadores y símbolos** | Multi-char primero (`==` `!=` `>=` `<=` `+=` `-=` `:=` `&&` `||`), luego single-char (`+` `-` `*` `/` `%` `=` `>` `<` `!` `(` `)` `{` `}` `;` `.` `,`) |
| A6 | **Patrones** | `{id}` `{decimal}` `{integer}` |
| A7 | **Estados especiales** | Estado `CADENA` con escapes `\n \r \t \\ \"`; comentarios `//` (hasta salto); estado `COMENTARIO_BLOQUE` con anidación `/* /* */ */` |
| A8 | **Prueba final** | Verificar orden de reglas (keywords > multi-op > single-op > patterns > catch-all), probar con entrada |

---

## Plan de construcción — Parser (`parser.cup`)

**Fase B: Gramática sin acciones** (ya completo, 0 conflictos).

**Fase C: Agregar acciones por grupo**, cada paso compila y se prueba.

| Paso | Reglas | Acción |
|------|--------|--------|
| C1 | `PROGRAMA` `FUNCION_MAIN` `BLOQUE` | `new Programa(FUNCION_MAIN)`, `new FuncionMain(BLOQUE)`, `new Bloque((List)INSTRUCCIONES)` |
| C2 | `INSTRUCCIONES` `INSTRUCCION` | Construir `ArrayList`, pasar valor |
| C3 | `DECLARACION_VAR` `ASIGNACION` `ASIGNACION_OP` | `new DeclaracionVar(id, tipo, expr, ...)`, `new Asignacion(...)`, `new AsignacionOp(...)` |
| C4 | `SENTENCIA_IF` | `new If(EXPRESION, BLOQUE)` y `new If(EXPRESION, BLOQUE, BLOQUE)` / `new If(EXPRESION, BLOQUE, SENTENCIA_IF)` |
| C5 | `SENTENCIA_FOR` | `new For(EXPRESION, BLOQUE)` y `new For(init, cond, update, BLOQUE)` |
| C6 | `BREAK` `CONTINUE` | `new Break()`, `new Continue()` |
| C7 | `LLAMADA_FUNCION` `LISTA_EXPRESIONES` | `new LlamadaFuncion(paquete, funcion, args, ...)`, construir lista |
| C8 | `TIPO` | `RESULT = "int"`, `"float64"`, etc. |
| C9 | `EXPRESION` binarias | `new ExpBinaria(izq, op, der, linea, col)` — el operador se pasa como string `"+"`, `"-"`, etc. |
| C10 | `EXPRESION` unarias | `new ExpUnaria("-", expr, linea, col)` para `MENOS EXPRESION %prec UMENOS`, `new ExpUnaria("!", expr, ...)` para `NOT EXPRESION` |
| C11 | `EXPRESION` primarias | `new Literal(TipoDato.INT, ENTERO, ...)`, `new Identificador(ID, ...)`, paso de `LLAMADA_FUNCION` |

---

## Plan de construcción — VisitorEjecucion

El intérprete que ejecuta el AST en runtime. Sigue el patrón del ejemplo oficial pero en español.

### Archivos a crear

```
analisis/visitor/ejecucion/
├── VisitorEjecucion.java       (implementa Visitor<Valor>)
├── BreakException.java         (RuntimeException para break)
├── ContinueException.java      (RuntimeException para continue)
└── valor/
    ├── Valor.java               (interfaz sellada)
    ├── ValorInt.java            (record: int valor, int linea, int columna)
    ├── ValorFloat.java          (record: double valor, int linea, int columna)
    ├── ValorBool.java           (record: boolean valor, int linea, int columna)
    ├── ValorString.java         (record: String valor, int linea, int columna)
    ├── ValorRune.java           (record: int valor, int linea, int columna)
    ├── ValorNil.java            (record: int linea, int columna)
    └── ValorVoid.java           (record: int linea, int columna)
```

### Detalle de Valor.java (interfaz sellada)

```java
public sealed interface Valor permits ValorInt, ValorFloat, ValorBool, ValorString, ValorRune, ValorNil, ValorVoid {
    int linea();
    int columna();
    String obtenerTipoNombre(); // "int", "float64", "string", "bool", "rune", "nil", "void"
}
```

### Detalle de VisitorEjecucion.java

Sigue el patrón exacto de `InterpreterVisitor` en clase05:

```java
public class VisitorEjecucion implements Visitor<Valor> {
    public String output = "";
    private final Valor defaultVoid = new ValorVoid(-1, -1);
    // ...

    public Valor Visitar(NodoAST nodo) {
        return nodo.accept(this);
    }
    // ...
}
```

| Método `visit` | Comportamiento |
|---|---|
| `Programa` | `ctx.main.accept(this)` → retorna `defaultVoid` |
| `FuncionMain` | `ctx.bloque.accept(this)` → retorna `defaultVoid` |
| `Bloque` | Push `HashMap` → ejecuta cada instrucción → pop → retorna `defaultVoid` |
| `DeclaracionVar` | Evalúa expresión (o usa valor defecto), guarda en ámbito actual → retorna `defaultVoid` |
| `Asignacion` | Busca variable, actualiza valor → retorna `defaultVoid` |
| `AsignacionOp` | Busca, aplica `+=` / `-=` según tipo → retorna `defaultVoid` |
| `If` | Si condición es `ValorBool(b) && b`: ejecuta bloqueIf; si no y existe bloqueElse: ejecuta bloqueElse |
| `For` | Guarda `dentroDeFor`, init → while(cond) { bloque; update } atrapando Break/Continue |
| `Break` | Lanza `BreakException` |
| `Continue` | Lanza `ContinueException` |
| `LlamadaFuncion` | Switch por paquete: `fmt.Println` → args.toString acumula en `output`; `strconv.Atoi` → `Integer.parseInt`; `strconv.ParseFloat` → `Double.parseDouble`; `reflect.TypeOf` → `expr.obtenerTipoNombre()` |
| `ExpBinaria` | Pattern matching tipo clase05: `switch (izq) { case ValorInt i when der instanceof ValorInt r -> ... }` |
| `ExpUnaria` | `-` numérico, `!` bool |
| `Literal` | `new ValorInt(valor, linea, col)` según `ctx.tipo` |
| `Identificador` | Busca en pila de ámbitos, error si no existe |

### Mecánica de Break/Continue

```java
// En For:
try {
    while (condicion) {
        try {
            bloque.accept(this);
        } catch (ContinueException e) { /* seguir */ }
        // ejecutar update
    }
} catch (BreakException e) { /* salir */ }
```

### Mecánica de ámbitos

```java
private final Deque<Map<String, Valor>> ambitos = new ArrayDeque<>();

// constructor
public VisitorEjecucion() {
    ambitos.push(new HashMap<>()); // ambito global
}

// en Bloque:
ambitos.push(new HashMap<>());
for (NodoAST instr : ctx.instrucciones) { instr.accept(this); }
ambitos.pop();

// busqueda (busca desde el ambito mas interno al mas externo):
for (Map<String, Valor> ambito : ambitos) {
    Valor v = ambito.get(nombre);
    if (v != null) return v;
}
```

---

## Gramática actual del parser

**Una sola `EXPRESION`** con `%prec` y labels (estilo ejemplo oficial):

```
EXPRESION ::= EXPRESION OR EXPRESION
            | EXPRESION AND EXPRESION
            | EXPRESION IGUALIGUAL EXPRESION
            | EXPRESION DIFERENTE EXPRESION
            | EXPRESION (MAYOR|MAYORIGUAL|MENOR|MENORIGUAL) EXPRESION
            | EXPRESION (MAS|MENOS) EXPRESION
            | EXPRESION (MULT|DIV|MOD) EXPRESION
            | MENOS EXPRESION %prec UMENOS
            | NOT EXPRESION
            | ENTERO | DECIMAL | CADENA | CHAR | TRUE | FALSE | NIL
            | IDENTIFICADOR
            | PAR1 EXPRESION PAR2
            | LLAMADA_FUNCION
;

SENTENCIA_IF ::= IF EXPRESION BLOQUE
               | IF EXPRESION BLOQUE ELSE BLOQUE
               | IF EXPRESION BLOQUE ELSE SENTENCIA_IF
;

SENTENCIA_FOR ::= FOR EXPRESION BLOQUE
                | FOR INSTRUCCION EXPRESION PTOCOMA INSTRUCCION BLOQUE
;
```

Precedencia:
```
precedence left OR;
precedence left AND;
precedence left IGUALIGUAL, DIFERENTE;
precedence left MAYOR, MAYORIGUAL, MENOR, MENORIGUAL;
precedence left MAS, MENOS;
precedence left MULT, DIV, MOD;
precedence right NOT;
precedence right UMENOS;
```

---

## Incluido en Fase 1

- [x] Identificadores (case sensitive)
- [x] Comentarios `//` y `/* */`
- [x] Tipos estáticos: `int`, `float64`, `string`, `bool`, `rune`
- [x] Valor `nil`
- [x] Bloques `{ }` (ámbitos)
- [x] Variables: `var` (con/sin inicial) y `:=`
- [x] Op. aritméticos: `+`, `-`, `*`, `/`, `%`
- [x] Op. asignación: `+=`, `-=`
- [x] String concatenación: `+` y `+=`
- [x] Negación unaria: `-`
- [x] Comparación: `==`, `!=`, `>`, `>=`, `<`, `<=`
- [x] Lógicos: `&&`, `||`, `!`
- [x] Precedencia y asociatividad
- [x] If-Else (incluye else if)
- [x] For (2 formas)
- [x] Break, Continue
- [x] Funciones embebidas: `fmt.Println`, `strconv.Atoi`, `strconv.ParseFloat`, `reflect.TypeOf().String()`
- [x] Secuencias de escape
- [x] Valores por defecto según tipo
- [ ] Lexer completo (Fase A1→A8)
- [ ] Parser con acciones (Fase C1→C11)
- [ ] VisitorEjecucion (con sistema Valor)
- [ ] VisitorASTPrint
- [ ] GUI (VentanaPrincipal)
- [ ] Compilador.java (orquestador)
- [ ] Reporte de errores
- [ ] Tabla de tokens

## Excluido de Fase 1 (va en Fase 2)

- [ ] Switch-Case
- [ ] For range
- [ ] Return
- [ ] Slices
- [ ] Structs
- [ ] Funciones definidas por usuario
- [ ] Parámetros
- [ ] Reporte de tabla de símbolos
- [ ] Reporte de AST
