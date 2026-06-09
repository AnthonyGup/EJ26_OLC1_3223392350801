# Plan Fase 1 - GoLite

> **Basado en**: `[OLC1] Proyecto Fase 1.pdf` (versión oficial definitiva)
> **Última actualización**: 08/06/2026

---

## Estado del proyecto

### ✅ Completado

| Componente | Archivos | Notas |
|---|---|---|
| **AST** | 16 clases en `analisis/ast/`, `ast/exp/`, `ast/stm/` | NodoAST interfaz genérica `<T>`, patrón Context, subpaquetes |
| **Visitor interfaz** | `analisis/visitor/Visitor.java` | Genérica `<T>`, 15 métodos `visit(Context)` |
| **VisitorSemantico** | `analisis/visitor/VisitorSemantico.java` | Implementa `Visitor<Void>`, recorre AST, valida tipos/ámbitos |
| **Semántica** | `TipoDato.java`, `Simbolo.java`, `TablaSimbolos.java` | Enum con valorDefecto, stack de ámbitos |
| **Lexer inicial** | `lexer.flex` | ✅ Se reseteó — ahora está limpio para construir desde cero |

### ⛔ Pendiente

| Área | Archivos | Prioridad |
|------|----------|-----------|
| **Lexer** | `lexer.flex` — construir reglas léxicas desde cero | Alta |
| **Parser** | `parser.cup` — construir gramática desde cero sin labels ni %prec | Alta |
| **VisitorEjecucion** | `analisis/visitor/VisitorEjecucion.java` | Media |
| **VisitorASTPrint** | `analisis/visitor/VisitorASTPrint.java` | Media |
| **GUI** | `VentanaPrincipal.java` (multitab, consola, reportes) | Media |
| **Compilador.java** | Orquestar lexer+parser+visitors | Media |
| **Reportes** | `ErrorDTO.java`, `TokenDTO.java`, `ReporteService.java` | Baja |

### Problemas detectados en parser.cup (motivo del reset)

| Problema | Causa | Solución |
|---|---|---|
| `%prec UMENOS` causa error de sintaxis | CUP 11b vbmacher no soporta `%prec` en producciones | Usar gramática con niveles de precedencia explícitos |
| `SYMBOL:label` causa error CODE_STRING | La sintaxis de labels no funciona en esta versión | Evitar labels; usar no-terminales `wrapper` para desambiguar |
| Reduce/Reduce entre `LLAMADA_FUNCION` como INSTRUCCION y EXPRESION | Ambigüedad natural de la gramática | Resolver con estructura de niveles (EXPRESION_PRIMARIA) |

---

## Plan de construcción — Lexer y Parser

### Fase A: Lexer (`lexer.flex`)

| Paso | Qué agregar | Ejemplo |
|------|-------------|---------|
| A1 | Configuración base (clase, cup, line/column) | `%cup`, `%class Lexer`, `%line`, `%column` |
| A2 | Palabras reservadas | `var`, `func`, `main`, `if`, `else`, `for`, `break`, `continue` |
| A3 | Tipos y literales | `int`, `float64`, `string`, `bool`, `rune`, `true`, `false`, `nil` |
| A4 | Funciones embebidas | `fmt`, `strconv`, `reflect`, `println`, `TypeOf`, `Atoi`, `ParseFloat`, `String` |
| A5 | Operadores y símbolos | `+`, `-`, `*`, `/`, `%`, `==`, `!=`, `>`, `<`, `>=`, `<=`, `&&`, `||`, `!`, `=`, `+=`, `-=`, `:=`, `(`, `)`, `{`, `}`, `;`, `.`, `,` |
| A6 | Patrones | `IDENTIFICADOR`, `ENTERO`, `DECIMAL` |
| A7 | Estados especiales | `CADENA` (con escapes), `COMENTARIO_BLOQUE` |
| A8 | Manejo de errores léxicos | Carácter no reconocido → token ERROR |

### Fase B: Parser — declaraciones (`parser.cup`)

| Paso | Qué agregar | Notas |
|------|-------------|-------|
| B1 | Package, imports | `analisis`, `java_cup.runtime.Symbol`, `java.util.*`, `analisis.ast.*` |
| B2 | Terminales | Todos los tokens del lexer |
| B3a | No-terminales básicos | `PROGRAMA`, `FUNCION_MAIN`, `BLOQUE`, `INSTRUCCIONES`, `INSTRUCCION` |
| B3b | No-terminales de instrucciones | `DECLARACION_VAR`, `ASIGNACION`, `ASIGNACION_OP`, `SENTENCIA_IF`, `SENTENCIA_FOR`, `SENTENCIA_BREAK`, `SENTENCIA_CONTINUE`, `LLAMADA_FUNCION` |
| B3c | No-terminales de expresiones | `EXPRESION`, `EXPRESION_OR`, `EXPRESION_AND`, `EXPRESION_COMP`, `EXPRESION_AD`, `EXPRESION_MULT`, `EXPRESION_UNARIA`, `EXPRESION_PRIMARIA` |
| B3d | Wrappers para desambiguar | `ELSE_BODY`, `FOR_INIT`, `FOR_UPDATE` |
| B4 | Precedencia (solo declaraciones `precedence`) | Aunque la gramática tiene niveles, las declaraciones ayudan a documentar |
| B5 | `start with PROGRAMA` | |

### Fase C: Parser — acciones semánticas (por grupo)

| Paso | Reglas | Constructor |
|------|--------|------------|
| C1 | `PROGRAMA`, `FUNCION_MAIN`, `BLOQUE` | `Programa`, `FuncionMain`, `Bloque` |
| C2 | `INSTRUCCIONES`, `INSTRUCCION` | `ArrayList`, paso de valor |
| C3 | `DECLARACION_VAR`, `ASIGNACION`, `ASIGNACION_OP` | `DeclaracionVar`, `Asignacion`, `AsignacionOp` |
| C4 | `SENTENCIA_IF`, `ELSE_BODY` | `If` |
| C5 | `SENTENCIA_FOR`, `FOR_INIT`, `FOR_UPDATE` | `For` |
| C6 | `SENTENCIA_BREAK`, `SENTENCIA_CONTINUE` | `Break`, `Continue` |
| C7 | `LLAMADA_FUNCION`, `LISTA_EXPRESIONES` | `LlamadaFuncion`, `ArrayList` |
| C8 | `TIPO` | String `"int"`, `"float64"`, etc. |
| C9 | Niveles de `EXPRESION_*` | `ExpBinaria` |
| C10 | `EXPRESION_UNARIA` | `ExpUnaria` |
| C11 | `EXPRESION_PRIMARIA` | `Literal`, `Identificador`, paso de `LLAMADA_FUNCION` |

---

### Estrategia de pruebas

1. **Test A**: Compilar solo el lexer → verificar que produce tokens correctos
2. **Test B**: Compilar parser sin acciones → verificar que la gramática es LALR(1) sin conflictos
3. **Test C**: Agregar acciones de a 1 grupo, compilar después de cada uno
4. **Test D**: Pipeline completo con `Compilador.java` temporal

---

## Arquitectura: Patrón Visitor (estilo Context)

```
NodoAST (interfaz genérica)
└── <T> T accept(Visitor<T> visitor)

Cada nodo concreto:
├── campos private final
├── Context (clase pública anidada)
│   └── expone los campos como public final
└── accept(visitor) -> visitor.visit(new Context(this))

Visitor<T> (interfaz genérica)
├── T visit(Programa.Context)
├── T visit(FuncionMain.Context)
├── T visit(Bloque.Context)
├── T visit(DeclaracionVar.Context)
├── T visit(Asignacion.Context)
├── T visit(AsignacionOp.Context)
├── T visit(If.Context)
├── T visit(For.Context)
├── T visit(Break.Context)
├── T visit(Continue.Context)
├── T visit(LlamadaFuncion.Context)
├── T visit(ExpBinaria.Context)
├── T visit(ExpUnaria.Context)
├── T visit(Literal.Context)
└── T visit(Identificador.Context)
```

---

## Gramática de expresiones (sin %prec, sin labels)

```
EXPRESION       ::= EXPRESION_OR
EXPRESION_OR    ::= EXPRESION_OR OR EXPRESION_AND | EXPRESION_AND
EXPRESION_AND   ::= EXPRESION_AND AND EXPRESION_COMP | EXPRESION_COMP
EXPRESION_COMP  ::= EXPRESION_COMP (==|!=|>|>=|<|<=) EXPRESION_AD | EXPRESION_AD
EXPRESION_AD    ::= EXPRESION_AD (+|-) EXPRESION_MULT | EXPRESION_MULT
EXPRESION_MULT  ::= EXPRESION_MULT (*|/|%) EXPRESION_UNARIA | EXPRESION_UNARIA
EXPRESION_UNARIA ::= (-|!) EXPRESION_UNARIA | EXPRESION_PRIMARIA
EXPRESION_PRIMARIA ::= ENTERO | DECIMAL | CADENA | CHAR | TRUE | FALSE | NIL
                     | IDENTIFICADOR
                     | PAR1 EXPRESION PAR2
                     | LLAMADA_FUNCION
```

---

## Gramática de instrucciones (sin labels, con wrappers)

```
SENTENCIA_IF  ::= IF EXPRESION BLOQUE
                | IF EXPRESION BLOQUE ELSE ELSE_BODY
ELSE_BODY     ::= BLOQUE | SENTENCIA_IF

SENTENCIA_FOR ::= FOR EXPRESION BLOQUE
                | FOR FOR_INIT EXPRESION PTOCOMA FOR_UPDATE BLOQUE
FOR_INIT      ::= INSTRUCCION
FOR_UPDATE    ::= INSTRUCCION
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
- [ ] Lexer completo
- [ ] Parser completo (con acciones)
- [ ] VisitorEjecucion
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
