# Plan Fase 1 - GoLite

> **Basado en**: `[OLC1] Proyecto Fase 1.pdf` (versión oficial definitiva)
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
| `lexer.flex` | ⚠️ Faltan escapes `\"`, `\\`, `\n`, `\r`, `\t` en estado CADENA |
| `parser.cup` | ⚠️ Gramática lista, faltan acciones semánticas para construir AST |
| `pom.xml` | ✅ Configurado con JFlex 1.9.1, CUP 11b, Java 21 |
| `Compilador.java` | ⛔ Solo esqueleto, vacío |
| `Proyecto_Fase_1.md` | ✅ PDF oficial extraído (2873 líneas) |

### Pendiente total

| Área | Archivos | Depende de |
|------|----------|------------|
| **Semántica** | `TipoDato.java`, `Simbolo.java`, `TablaSimbolos.java` | Nada (independiente) |
| **Visitors** | `VisitorSemantico.java`, `VisitorEjecucion.java`, `VisitorASTPrint.java` | TablaSimbolos + AST |
| **Parser (acciones)** | Agregar creación de nodos en cada regla de `parser.cup` | AST completo |
| **Lexer (mejoras)** | Escapes en CADENA (`\n`, `\t`, `\"`, `\\`, `\r`) | Nada |
| **GUI** | `VentanaPrincipal.java` (múltiples pestañas, línea actual) + `Compilador.java` | Nada |
| **Reportes** | `ErrorDTO.java`, `TokenDTO.java`, `ReporteService.java` | Nada |

---

## Cambios detectados del PDF preliminar al definitivo

### Lo que cambia en el código existente:

1. **Lexer — secuencias de escape** (nuevo requisito):
   - En el estado `CADENA`, procesar: `\"`, `\\`, `\n`, `\r`, `\t`
   - Actualmente solo lee caracteres literalmente sin interpretar escapes

2. **Lexer — `reflect.TypeOf().string`** (posible ajuste):
   - El PDF usa minúscula `string` para la función
   - Actualmente el lexer tiene `"String"` (mayúscula) como token STRINGFUNC
   - ⚠️ `string` minúscula ya es token STRING (tipo de dato) → conflicto
   - **Decisión**: Mantener `"String"` mayúscula en el lexer para evitar ambigüedad, y el parser lo usará como STRINGFUNC

### Lo que cambia en la lógica de los Visitors (futuro):

3. **Concatenación de strings con `+`**: `"ho" + "la" = "hola"` — el operador `+` también funciona para strings
4. **Concatenación con `+=`**: `str += "cad"` concatena strings también
5. **Conversión implícita int→float64**: Asignar int a variable float64 es válido (ej: `var a float64 = 5`)
6. **Tablas de tipos por operación**: Cada operador tiene reglas específicas de tipos permitidos y resultado (PDF secciones detalladas):
   - Suma: int+int→int, int+float64→float64, float64+float64→float64, string+string→string
   - Resta/Mult/Div: numéricos solamente (int, float64), con conversión implícita
   - Módulo: solo int%int→int
   - Comparación: mismo tipo, resultado bool
   - Lógicos: solo bool && bool, bool || bool, !bool
7. **División por cero**: Error semántico (reportar)
8. **nil en operaciones**: Error semántico (no se puede usar nil en ops aritméticas/lógicas)
9. **Valores por defecto**: int=0, float64=0.0, string="", bool=false, rune=0

### Lo que cambia en la GUI (futuro):

10. **Múltiples pestañas**: Editor debe permitir abrir varios archivos a la vez
11. **Línea actual**: Mostrar el número de línea donde está el cursor
12. **Consola**: Debe mostrar exactamente: elementos separados por espacio, con salto de línea al final

### Lo que cambia en entregables:

13. **README**: Nuevo requisito — descripción general del proyecto e indicaciones de cómo ejecutarlo
14. **Nombre del repo**: Cambia de `EJ26_OLC1_P1_#Carnet` a `EJ26_OLC1_#Carnet` (sin `_P1`)

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
| `VisitorSemantico` | Validar tipos, ámbitos, reglas semánticas | `List<ErrorDTO>` errores |
| `VisitorEjecucion` | Recorrer AST e interpretar el programa | Consola de salida |
| `VisitorASTPrint` | Generar representación textual del AST | `String` del árbol |

---

## Orden de implementación restante

### 1. Semántica (`analisis/semantic/`)
- `TipoDato` (enum): `INT, FLOAT64, STRING, BOOL, RUNE, NIL`
- `Simbolo`: nombre, tipo, línea, columna, valor, ámbito
- `TablaSimbolos`: pila de ámbitos, insertar/buscar símbolos
- Valores por defecto: int=0, float64=0.0, string="", bool=false, rune=0

### 2. Lexer — mejoras (`lexer.flex`)
- Procesar secuencias de escape en estado CADENA:
  - `\"` → comilla doble
  - `\\` → barra invertida
  - `\n` → salto de línea
  - `\r` → retorno de carro
  - `\t` → tabulación

### 3. Visitors (`analisis/visitor/`)
- `VisitorSemantico.java`: recorre AST, llena tabla de símbolos, valida tipos
- `VisitorEjecucion.java`: interpreta el programa y retorna la salida de consola
- `VisitorASTPrint.java`: genera string con la estructura del AST

#### Reglas semánticas clave a implementar:
- Concatenación: `string + string` válido
- Concatenación: `string += string` válido
- Conversión implícita: `int` puede asignarse a `float64`, e `int + float64 → float64`
- Módulo: solo `int % int` válido
- División por cero: error
- nil en operaciones: error
- Comparación: solo entre mismos tipos
- Lógicos: solo entre bools

#### Reglas de ejecución clave:
- `+` con strings: concatenar
- `+=` con strings: concatenar
- fmt.Println: espacio entre argumentos, salto de línea al final
- strconv.Atoi / ParseFloat: convertir string a número, error si no se puede
- reflect.TypeOf().String(): devolver el tipo como string

### 4. Parser — acciones semánticas (`parser.cup`)
- Agregar `import java.util.LinkedList;`
- Declarar `nonterminal LinkedList<NodoAST> INSTRUCCIONES;`
- Declarar `nonterminal LinkedList<NodoAST> LISTA_EXPRESIONES;`
- En cada regla, construir el nodo AST correspondiente con new + línea/columna

### 5. GUI (`usac/compi1/gui/`)
- `VentanaPrincipal.java`:
  - Editor con **múltiples pestañas** (abrir varios archivos .glt)
  - **Mostrar línea actual** del cursor
  - Botón Ejecutar → llama al intérprete (Lexer → Parser → Semántico → Ejecución)
  - Consola de salida (texto con resultados de ejecución)
  - Pestañas de reportes: Tokens, Errores
  - Abrir/guardar archivos

### 6. Compilador.java
- Lanzar la ventana principal (`VentanaPrincipal`)

### 7. Reportes (`usac/compi1/reportes/`)
- `ErrorDTO`: No., Descripción, Línea, Columna, Tipo (léxico/sintáctico/semántico)
- `TokenDTO`: No., Lexema, Tipo, Línea, Columna
- `ReporteService`: recolectar tokens durante análisis léxico, recolectar errores

### 8. README.md (nuevo requisito)
- Descripción general del proyecto
- Indicaciones de cómo ejecutarlo

---

## Flujo de ejecución

```
Código fuente (.glt)
    → Lexer (JFlex) → tokens (guardar para Reporte de Tokens)
    → Parser (CUP) → AST (tree de NodoAST)
    → VisitorSemantico → valida tipos, ámbitos, reglas
        → si hay errores → mostrar en Reporte de Errores
    → VisitorEjecucion → interpreta el AST
        → salida en Consola
    → VisitorASTPrint → representación del árbol (solo si se requiere)
```

---

## Incluido en Fase 1
- [x] Identificadores (case sensitive, letra/underscore + dígitos)
- [x] Comentarios `//` y `/* */`
- [x] Tipos estáticos: `int`, `float64`, `string`, `bool`, `rune`
- [x] Valor `nil`
- [x] Bloques de sentencias `{ }` (ámbitos)
- [x] Signos de agrupación `( )`
- [x] Variables: `var` (con/sin valor inicial) y `:=`
- [x] Op. aritméticos: `+`, `-`, `*`, `/`, `%`
- [x] Op. asignación: `+=`, `-=`
- [x] String concatenación: `+` y `+=` con strings ✅ (nuevo del PDF definitivo)
- [x] Negación unaria: `-`
- [x] Comparación: `==`, `!=`, `>`, `>=`, `<`, `<=`
- [x] Lógicos: `&&`, `||`, `!`
- [x] Precedencia y asociatividad
- [x] If-Else (incluye else if)
- [x] For (2 formas: solo condición, o init;cond;upd)
- [x] Break, Continue
- [x] Funciones embebidas: `fmt.Println`, `strconv.Atoi`, `strconv.ParseFloat`, `reflect.TypeOf().String()`
- [x] Secuencias de escape: `\"`, `\\`, `\n`, `\r`, `\t` ✅ (nuevo del PDF definitivo)
- [x] Valores por defecto según tipo ✅ (nuevo del PDF definitivo)
- [ ] Reporte de errores
- [ ] Tabla de tokens

## Excluido de Fase 1 (va en Fase 2)
- [ ] Switch-Case
- [ ] For range (para slices)
- [ ] Return
- [ ] Slices (creación, slices.Index, strings.Join, len, append, acceso, multidimensionales, matrices)
- [ ] Structs (definición, atributos, funciones asociadas)
- [ ] Funciones definidas por usuario
- [ ] Parámetros de funciones
- [ ] Reporte de tabla de símbolos
- [ ] Reporte de AST

---

## Bitácora de sesiones

### 06/06/2026 — Sesión 1: AST + Visitor
- Creados todos los nodos del AST (16 clases en `analisis/ast/`).
- Creada interfaz `Visitor.java` con `visit()` para cada nodo.
- Decisión de diseño: `ExpComparacion` y `ExpLogica` se unificaron en `ExpBinaria`.
- Corregido `If.java`: constructor acepta `NodoAST` en else (soporta `else if`).
- Extraído PDF oficial a `Proyecto_Fase_1.md` (2873 líneas).
- Comparado PDF definitivo vs preliminar → identificadas diferencias.
- Actualizado el plan con todos los cambios del PDF definitivo.
- Pendiente: semántica, escapes en lexer, visitors concretos, acciones del parser, GUI, reportes.
