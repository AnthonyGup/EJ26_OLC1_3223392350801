# Plan Fase 1 - GoLite

> **Última actualización**: 06/06/2026

---

## Estado del proyecto

```
analisis/
├── ast/                    ✅ COMPLETO (16 clases + 1 abstracta)
│   ├── NodoAST.java             ← clase base abstracta (linea, columna, accept)
│   ├── Programa.java            ← raíz: func main()
│   ├── FuncionMain.java         ← contiene Bloque
│   ├── Bloque.java              ← lista de instrucciones
│   ├── DeclaracionVar.java      ← var x int, var x int = e, x := e
│   ├── Asignacion.java          ← x = e
│   ├── AsignacionOp.java        ← x += e, x -= e
│   ├── If.java                  ← if/else/else if
│   ├── For.java                 ← for cond { }, for init;cond;upd { }
│   ├── Break.java               ← break
│   ├── Continue.java            ← continue
│   ├── LlamadaFuncion.java      ← fmt.Println, strconv.Atoi, etc.
│   ├── ExpBinaria.java          ← + - * / % == != > < >= <= && ||
│   ├── ExpUnaria.java           ← -expr, !expr
│   ├── Literal.java             ← 42, 3.14, "hola", 'a', true, false, nil
│   └── Identificador.java       ← referencia a variable
│
└── visitor/
    └── Visitor.java         ← interfaz con visit(...) para cada nodo
```

### Archivos externos ya listos

| Archivo | Estado |
|---------|--------|
| `lexer.flex` | ✅ Completo — todos los tokens Fase 1 |
| `parser.cup` | ⚠️ Gramática lista, faltan acciones semánticas para construir AST |
| `pom.xml` | ✅ Configurado con JFlex 1.9.1, CUP 11b, Java 21 |
| `Compilador.java` | ⛔ Solo esqueleto, vacío |

### Pendiente total

| Área | Archivos | Depende de |
|------|----------|------------|
| **Semántica** | `TipoDato.java`, `Simbolo.java`, `TablaSimbolos.java` | Nada (independiente) |
| **Visitors** | `VisitorSemantico.java`, `VisitorEjecucion.java`, `VisitorASTPrint.java` | TablaSimbolos + AST |
| **Parser (acciones)** | Agregar creación de nodos en cada regla de `parser.cup` | AST completo |
| **GUI** | `VentanaPrincipal.java` + lanzador en `Compilador.java` | Nada (independiente) |
| **Reportes** | `ErrorDTO.java`, `TokenDTO.java`, `ReporteService.java` | Nada (independiente) |

---

## Arquitectura: Patrón Visitor

Cada nodo del AST implementa `accept(Visitor)`, y cada operación sobre el AST es un Visitor distinto:

```
NodoAST (abstract)
├── accept(Visitor v): void
├── linea: int
└── columna: int

Visitor (interfaz)
├── visit(Programa)
├── visit(FuncionMain)
├── visit(Bloque)
├── visit(DeclaracionVar)
├── visit(Asignacion)
├── visit(AsignacionOp)
├── visit(If)
├── visit(For)
├── visit(Break)
├── visit(Continue)
├── visit(LlamadaFuncion)
├── visit(ExpBinaria)
├── visit(ExpUnaria)
├── visit(Literal)
└── visit(Identificador)
```

**Nota**: Las expresiones de comparación (`==`, `!=`, `<`, `>`, `<=`, `>=`) y lógicas (`&&`, `||`) se unificaron en `ExpBinaria` con `String operador` — no hay clases separadas `ExpComparacion` ni `ExpLogica`.

### Visitantes

| Visitor | Función | Retorna |
|---------|---------|---------|
| `VisitorSemantico` | Validar tipos, ámbitos, reglas semánticas | `List<String>` errores |
| `VisitorEjecucion` | Recorrer AST e interpretar el programa | Consola de salida |
| `VisitorASTPrint` | Generar representación textual del AST | `String` del árbol |

---

## Orden de implementación restante

### 1. Semántica (`analisis/semantic/`)
- `TipoDato` (enum): `INT, FLOAT64, STRING, BOOL, RUNE, NIL`
- `Simbolo`: nombre, tipo, línea, columna, valor, ámbito
- `TablaSimbolos`: pila de ámbitos, insertar/buscar símbolos

### 2. Visitors (`analisis/visitor/`)
- `VisitorSemantico.java`: recorre AST, llena tabla de símbolos, valida tipos
- `VisitorEjecucion.java`: interpreta el programa y retorna la salida de consola
- `VisitorASTPrint.java`: genera string con la estructura del AST

### 3. Parser — acciones semánticas (`parser.cup`)
- Agregar `import java.util.LinkedList;`
- Declarar `nonterminal LinkedList<NodoAST> INSTRUCCIONES;`
- Declarar `nonterminal LinkedList<NodoAST> LISTA_EXPRESIONES;`
- En cada regla, construir el nodo AST correspondiente con new + línea/columna

### 4. GUI (`usac/compi1/gui/`)
- `VentanaPrincipal.java`: editor de texto, botón Ejecutar, consola de salida
- Pestañas de reportes (Tokens, Errores)
- Abrir/guardar archivos `.glt`

### 5. Compilador.java
- Lanzar la ventana principal (`VentanaPrincipal`)

### 6. Reportes (`usac/compi1/reportes/`)
- `ErrorDTO`: No., Descripción, Línea, Columna, Tipo
- `TokenDTO`: No., Lexema, Tipo, Línea, Columna
- `ReporteService`: generar tablas HTML o tablas para mostrar en GUI

---

## Flujo de ejecución

```
Código fuente (.glt)
    → Lexer (JFlex) → tokens
    → Parser (CUP) → AST (tree de NodoAST)
    → VisitorSemantico → valida tipos y ámbitos
        → si hay errores → mostrar en reporte
    → VisitorEjecucion → interpreta el AST
        → salida en Consola
    → VisitorASTPrint → representación del árbol
```

---

## Incluido en Fase 1
- [x] Identificadores (case sensitive)
- [x] Comentarios `//` y `/* */`
- [x] Tipos estáticos: `int`, `float64`, `string`, `bool`, `rune`
- [x] Valor `nil`
- [x] Bloques de sentencias `{ }` (ámbitos)
- [x] Signos de agrupación `( )`
- [x] Variables: `var` y `:=`
- [x] Op. aritméticos: `+`, `-`, `*`, `/`, `%`
- [x] Op. asignación: `+=`, `-=`
- [x] Negación unaria: `-`
- [x] Comparación: `==`, `!=`, `>`, `>=`, `<`, `<=`
- [x] Lógicos: `&&`, `||`, `!`
- [x] Precedencia y asociatividad
- [x] If-Else
- [x] For (excluye for range)
- [x] Break, Continue
- [x] Funciones embebidas: `fmt.Println`, `strconv.Atoi`, `strconv.ParseFloat`, `reflect.TypeOf().String()`
- [ ] Reporte de errores
- [ ] Tabla de tokens

## Excluido de Fase 1
- [ ] Switch-Case
- [ ] For range
- [ ] Return
- [ ] Slices
- [ ] Structs
- [ ] Funciones definidas por usuario
- [ ] Reporte de tabla de símbolos
- [ ] Reporte de AST

---

## Bitácora de sesiones

### 06/06/2026 — Sesión 1: AST + Visitor
- Creados todos los nodos del AST (16 clases en `analisis/ast/`).
- Creada interfaz `Visitor.java` con `visit()` para cada nodo.
- Decisión de diseño: `ExpComparacion` y `ExpLogica` se unificaron en `ExpBinaria`.
- Corregido `If.java`: constructor acepta `NodoAST` en else (soporta `else if`).
- Pendiente: semántica, visitors concretos, acciones del parser, GUI, reportes.
