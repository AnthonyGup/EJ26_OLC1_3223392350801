# Plan Fase 1 - GoLite

## 1. Cambios sobre el proyecto actual

| Archivo | Acción |
|---------|--------|
| `lexer.flex` | Reescribir: eliminar `imprimir`, agregar todos los tokens de GoLite |
| `parser.cup` | Reescribir: gramática completa de GoLite + generación de AST |
| `Compilador.java` | Convertir en lanzador de GUI |
| `pom.xml` | Sin cambios mayores |

## 2. Paquetes nuevos

```
analisis/
├── ast/                    # Nodos del AST (20+ clases)
│   └── visitor/            # Patrón Visitor (3 visitantes)
│       ├── Visitor.java           # Interfaz visitante
│       ├── VisitorSemantico.java  # Validación de tipos y ámbitos
│       ├── VisitorEjecucion.java  # Interpretación del programa
│       └── VisitorASTPrint.java   # Generar string del AST
├── semantic/               # Tabla de símbolos y tipos
usac/compi1/
├── gui/                    # Ventana principal (editor + consola)
├── reportes/               # DTOs y generación de reportes
```

## 3. Arquitectura: Patrón Visitor

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
├── visit(ExpComparacion)
├── visit(ExpLogica)
├── visit(Literal)
├── visit(Identificador)
└── getResultado(): Object
```

### Visitantes

| Visitor | Función | Retorna |
|---------|---------|---------|
| `VisitorSemantico` | Validar tipos, ámbitos, reglas semánticas | `List<String>` errores |
| `VisitorEjecucion` | Recorrer AST e interpretar el programa | Consola de salida |
| `VisitorASTPrint` | Generar representación textual del AST | `String` del árbol |

## 4. Orden de implementación

### A. Léxico (`lexer.flex`)
- Keywords: `var`, `func`, `main`, `if`, `else`, `for`, `break`, `continue`, `true`, `false`, `nil`, `int`, `float64`, `string`, `bool`, `rune`, `fmt`, `strconv`, `reflect`, `print`, `println`
- Operadores: `%`, `&&`, `||`, `!`, `==`, `!=`, `>=`, `<=`, `>`, `<`, `=`, `+=`, `-=`
- Comentarios: `//` y `/* */`
- Literales: rune `'A'`, strings `"..."`, enteros, decimales
- `;` opcional

### B. Sintaxis + AST (`parser.cup`)
- `func main() { ... }`
- Variables: `var x int`, `var x int = expr`, `x := expr`
- Asignaciones: `x = expr`, `x += expr`, `x -= expr`
- If-else, For (2 formas)
- Break, Continue
- Llamadas a funciones embebidas
- Expresiones completas (aritméticas, comparación, lógicas)

### C. Clases AST + Visitor
Cada nodo tiene constructor con línea/columna y método `accept(Visitor)`:

- `NodoAST` (abstracto), `Programa`, `FuncionMain`, `Bloque`
- `Instruccion`: `DeclaracionVar`, `Asignacion`, `AsignacionOp`, `If`, `For`, `Break`, `Continue`, `LlamadaFuncion`
- `Expresion`: `ExpBinaria`, `ExpUnaria`, `ExpComparacion`, `ExpLogica`, `Literal`, `Identificador`
- `Visitor.java`: interfaz con `visit(...)` para cada nodo
- `VisitorEjecucion.java`: interpreta el programa y retorna la salida de consola
- `VisitorSemantico.java`: recorre AST, llena tabla de símbolos, valida tipos
- `VisitorASTPrint.java`: genera string con la estructura del AST

### D. Tabla de Símbolos y Tipos
- `TipoDato` (enum): INT, FLOAT64, STRING, BOOL, RUNE, NIL
- `Simbolo`: nombre, tipo, línea, columna, valor, ámbito
- `TablaSimbolos`: pila de ámbitos, insertar/buscar símbolos

### E. GUI (Swing)
- Editor de texto, botón Ejecutar, consola de salida
- Pestañas de reportes (Tokens, Errores)
- Abrir/guardar archivos `.glt`

### F. Reportes
- Tabla de errores (No., Descripción, Línea, Columna, Tipo)
- Tabla de tokens (No., Lexema, Tipo, Línea, Columna)

## 5. Flujo de ejecución

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

## 6. Incluido en Fase 1
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
- [x] Reporte de errores
- [x] Tabla de tokens

## 7. Excluido de Fase 1
- [ ] Switch-Case
- [ ] For range
- [ ] Return
- [ ] Slices
- [ ] Structs
- [ ] Funciones definidas por usuario
- [ ] Reporte de tabla de símbolos
- [ ] Reporte de AST
