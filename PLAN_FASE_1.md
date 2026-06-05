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
├── ast/           # Nodos del AST (20+ clases)
├── semantic/      # Tabla de símbolos, análisis semántico
usac/compi1/
├── gui/           # Ventana principal (editor + consola)
├── reportes/      # DTOs y generación de reportes
```

## 3. Orden de implementación

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

### C. Clases AST
- `NodoAST` (interfaz), `Programa`, `FuncionMain`, `Bloque`
- `Instruccion`: `DeclaracionVar`, `Asignacion`, `AsignacionOp`, `If`, `For`, `Break`, `Continue`, `LlamadaFuncion`
- `Expresion`: `ExpBinaria`, `ExpUnaria`, `ExpComparacion`, `ExpLogica`, `Literal`, `Identificador`, `LlamadaFuncion`

### D. Análisis Semántico
- `TipoDato` (enum), `Simbolo`, `TablaSimbolos` (scopes)
- `AnalizadorSemantico`: verifica tipos, ámbitos, conversiones implícitas

### E. GUI (Swing)
- Editor de texto, botón Ejecutar, consola de salida
- Pestañas de reportes (Tokens, Errores)

### F. Reportes
- Tabla de tokens (No., Lexema, Tipo, Línea, Columna)
- Tabla de errores (No., Descripción, Línea, Columna, Tipo)

## 4. Incluido en Fase 1
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

## 5. Excluido de Fase 1
- [ ] Switch-Case
- [ ] For range
- [ ] Return
- [ ] Slices
- [ ] Structs
- [ ] Funciones definidas por usuario
- [ ] Reporte de tabla de símbolos
- [ ] Reporte de AST
