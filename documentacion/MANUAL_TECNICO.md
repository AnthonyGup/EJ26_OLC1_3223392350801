# Manual Técnico — Compilador GoLite

## 1. Descripción General

Compilador e intérprete para el lenguaje **GoLite** (subconjunto de Go). Analiza, valida y ejecuta código fuente `.glt` mediante una interfaz gráfica Swing.

## 2. Arquitectura del Sistema

```
Código Fuente (.glt)
    │
    ▼
┌─────────────────────┐
│   JFlex Lexer       │  →  Tokens
└─────────┬───────────┘
          ▼
┌─────────────────────┐
│   CUP Parser        │  →  AST (Programa)
└─────────┬───────────┘
          ▼
┌─────────────────────┐
│ VisitorSemantico    │  →  Tabla de Símbolos + Errores
└─────────┬───────────┘
          ▼
┌─────────────────────┐
│ VisitorEjecucion    │  →  Salida en consola
└─────────────────────┘
```

## 3. Tecnologías Utilizadas

| Tecnología | Versión | Propósito |
|---|---|---|
| Java | 21 | Lenguaje base |
| Maven | — | Sistema de construcción |
| JFlex | 1.9.1 | Generación del analizador léxico |
| CUP | 11b-20160615-3 | Generación del analizador sintáctico (LALR(1)) |
| RSyntaxTextArea | 3.3.4 | Editor de texto con resaltado de sintaxis |
| Graphviz-Java | 0.18.1 | Renderizado gráfico del AST |
| SLF4J-NOP | 1.7.36 | Supresión de logs |
| Swing | — | Interfaz gráfica |

## 4. Estructura del Proyecto

```
Compilador/
├── pom.xml
└── src/main/
    ├── cup/parser.cup            → Gramática CUP (LALR(1))
    ├── jflex/lexer.flex          → Especificación JFlex
    └── java/
        ├── analisis/
        │   ├── Compilador.java   → Punto de entrada (main)
        │   ├── ast/              → Nodos del AST
        │   │   ├── exp/          → Expresiones
        │   │   └── stm/          → Sentencias
        │   ├── semantic/         → Análisis semántico
        │   │   ├── Simbolo.java
        │   │   ├── TablaSimbolos.java
        │   │   ├── TipoDato.java
        │   │   ├── TipoUtils.java
        │   │   └── ValidacionSemantica.java
        │   └── visitor/          → Patrón Visitor
        │       ├── Visitor.java
        │       ├── VisitorSemantico.java
        │       ├── grafico/VisitorGrafico.java
        │       └── ejecucion/    → Intérprete
        │           ├── GestorAmbitos.java
        │           ├── Operaciones.java
        │           ├── VisitorEjecucion.java
        │           ├── ReturnException.java
        │           ├── BreakException.java
        │           ├── ContinueException.java
        │           └── valor/    → Valores en tiempo de ejecución
        └── usac/compi1/gui/      → Interfaz gráfica
            ├── VentanaPrincipal.java
            ├── PanelEditor.java
            ├── PanelConsola.java
            └── reports/
                ├── GoliteError.java
                ├── ReporteDialog.java
                └── TokenInfo.java
```

## 5. Análisis Léxico (JFlex)

**Archivo:** `Compilador/src/main/jflex/lexer.flex`

Reconoce:
- **Palabras reservadas:** `func`, `main`, `if`, `else`, `for`, `break`, `continue`, `switch`, `case`, `default`, `return`, `struct`, `var`
- **Tipos:** `int`, `float64`, `string`, `bool`, `rune`
- **Literales:** enteros, decimales, cadenas (con interpolación), caracteres, `true`, `false`, `nil`
- **Operadores:** aritméticos (`+`, `-`, `*`, `/`, `%`), comparación, lógicos, asignación (`=`, `+=`, `-=`, `:=`), incremento/decremento (`++`, `--`)
- **Funciones de biblioteca:** `fmt.Println`, `strconv.Atoi`, `strconv.ParseFloat`, `reflect.TypeOf`, `slices.Index`, `strings.Join`
- **Comentarios:** `//` línea, `/* */` bloque
- Generador de tokens con información de línea, columna y tipo

## 6. Análisis Sintáctico (CUP)

**Archivo:** `Compilador/src/main/cup/parser.cup`

- Gramática **LALR(1)** con acciones semánticas para construir el AST
- Recuperación de errores sintácticos (reglas `error`)
- 649 líneas de especificación
- Ver gramática BNF completa en `documentacion/gramatica_bnf.md`

## 7. AST (Abstract Syntax Tree)

Cada nodo implementa la interfaz `NodoAST` con el método `accept(Visitor)`.

### Nodos de Expresión (`ast/exp/`)
| Clase | Descripción |
|---|---|
| `Literal` | Literales (int, float64, string, bool, rune, nil) |
| `Identificador` | Referencia a variable |
| `ExpBinaria` | Operación binaria |
| `ExpUnaria` | Operación unaria (-, !) |
| `FuncCall` | Llamada a función definida por usuario |
| `LlamadaFuncion` | Llamada a función de biblioteca |
| `IndexAccess` | Acceso a índice de slice `arr[i]` |
| `SliceLiteral` | Literal de slice `[]T{...}` |
| `SliceLiteral2D` | Literal de slice 2D `[][]T{...}` |
| `Len` | `len()` |
| `Append` | `append()` |
| `StructAccess` | Acceso a campo de struct `s.field` |
| `StructMethodCall` | Llamada a método de struct |
| `NewStruct` | Instanciación de struct |

### Nodos de Sentencia (`ast/stm/`)
| Clase | Descripción |
|---|---|
| `DeclaracionVar` | Declaración de variable |
| `Asignacion` | Asignación simple |
| `AsignacionOp` | Asignación compuesta (+=, -=) |
| `AssignIndex` | Asignación a índice de slice |
| `AssignIndex2D` | Asignación a índice 2D |
| `If` | If/else if/else |
| `For` | Bucle for |
| `Switch` | Switch con casos y default |
| `Break` | Break |
| `Continue` | Continue |
| `ReturnStmt` | Return |
| `FuncDef` | Definición de función |
| `StructDef` | Definición de struct |
| `StructMethodDef` | Definición de método de struct |
| `StructAssign` | Asignación a campo de struct |

## 8. Análisis Semántico

**Clase principal:** `VisitorSemantico.java`

Implementa el patrón Visitor para recorrer el AST y realizar:
- **Verificación de tipos:** compatibilidad en asignaciones, operaciones binarias, retornos
- **Validación de ámbito:** variables declaradas antes de usar, funciones definidas
- **Estructuras:** validación de campos, métodos, herencia de tipos
- **Control de flujo:** `break`/`continue` solo dentro de bucles
- **Tabla de símbolos:** `TablaSimbolos` mantiene todos los identificadores con su tipo, ámbito y valor

### Sistema de Tipos (`TipoDato`)
- `INT`, `FLOAT64`, `STRING`, `BOOL`, `RUNE`
- `SLICE` (con tipo interno)
- `STRUCT` (con nombre del struct)
- `VOID`, `NIL`

## 9. Ejecución (Intérprete)

**Clase principal:** `VisitorEjecucion.java`

Intérprete de recorrido de árbol _(tree-walking)_ que:
- Evalúa expresiones recursivamente
- Ejecuta sentencias en orden
- Gestiona ámbitos con `GestorAmbitos` (pila de scopes)
- Maneja flujo de control mediante excepciones (`ReturnException`, `BreakException`, `ContinueException`)
- Soporta operaciones entre tipos mixtos (promoción automática `int` → `float64`)

### Valores en Tiempo de Ejecución (`ejecucion/valor/`)
| Clase | Tipo |
|---|---|
| `ValorInt` | int |
| `ValorFloat` | float64 |
| `ValorString` | string |
| `ValorBool` | bool |
| `ValorRune` | rune |
| `ValorSlice` | slice |
| `ValorStruct` | struct |
| `ValorNil` | nil |
| `ValorVoid` | void |

## 10. Interfaz Gráfica

**Clase principal:** `VentanaPrincipal.java`

Ventana Swing con:
- **PanelEditor:** editor de texto con resaltado de sintaxis Go (RSyntaxTextArea), números de línea
- **PanelConsola:** salida de la ejecución, errores
- **Menús:** Archivo (Nuevo, Abrir, Guardar, Guardar como), Ejecutar (Run), Ver (AST, Tokens, Errores)
- **Diálogos modales:** Reporte de tokens, reporte de errores
- **Visualización de AST:** genera DOT → Graphviz → PNG → muestra en ventana

## 11. Construcción y Ejecución

**Requisitos:** Java 21+, Maven

```bash
cd Compilador
mvn clean package
java -jar target/Compilador-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## 12. Generación de Código

El proceso de build ejecuta automáticamente:
1. **JFlex** → genera `Lexer.java` desde `lexer.flex`
2. **CUP** → genera `parser.java` y `sym.java` desde `parser.cup`
3. **Compilación** → todos los `.java` se compilan a `.class`
4. **Ensamblado** → se empaqueta en JAR con dependencias

## 13. Características del Lenguaje GoLite

- Tipos primitivos: `int`, `float64`, `string`, `bool`, `rune`
- Tipos compuestos: `[]T` (slices), `[][]T` (slices 2D), `struct`
- Variables: declaración con tipo (`var x int`), inferencia (`:=`), asignación
- Control: `if/else`, `for` (condición y tres partes), `switch/case/default`
- Funciones con parámetros y retorno
- Métodos sobre structs
- Break/continue en bucles
- Funciones de biblioteca: `fmt.Println`, `strconv.Atoi`, `strconv.ParseFloat`, `reflect.TypeOf`, `slices.Index`, `strings.Join`
- Funciones integradas: `len()`, `append()`
