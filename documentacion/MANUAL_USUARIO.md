# Manual de Usuario — Compilador GoLite

## 1. Introducción

GoLite es un compilador con interfaz gráfica que permite escribir, analizar y ejecutar programas en un subconjunto del lenguaje Go (archivos `.glt`).

## 2. Requisitos del Sistema

- **Java** versión 21 o superior instalado
- **Maven** (opcional, solo para compilar desde código fuente)
- Sistema operativo: Windows, Linux o macOS

## 3. Cómo Ejecutar la Aplicación

### Opción 1 — JAR ejecutable (recomendado)

```bash
java -jar Compilador-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Opción 2 — Desde el código fuente

```bash
cd Compilador
mvn clean package
java -jar target/Compilador-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Opción 3 — Desde un IDE

Ejecutar la clase `analisis.Compilador` (Run → Run 'Compilador').

## 4. Interfaz Gráfica

Al iniciar la aplicación se muestra una ventana con los siguientes componentes:

```
┌────────────────────────────────────────────┐
│  Archivo   Ejecutar   Ver                  │  ← Barra de menús
├────────────────────────────────────────────┤
│                                            │
│  [Área de edición]                         │  ← PanelEditor
│  (resaltado de sintaxis,                   │
│   números de línea)                        │
│                                            │
│                                            │
├────────────────────────────────────────────┤
│  [Consola de salida]                       │  ← PanelConsola
│  (resultados de ejecución,                 │
│   mensajes de error)                       │
└────────────────────────────────────────────┘
```

### Barra de Menús

| Menú | Opción | Descripción |
|---|---|---|
| **Archivo** | Nuevo | Limpia el editor para un nuevo archivo |
| | Abrir | Abre un archivo `.glt` existente |
| | Guardar | Guarda el contenido actual en el archivo abierto |
| | Guardar como | Guarda en una ubicación/nombre nuevo |
| **Ejecutar** | Run | Compila y ejecuta el código actual |
| **Ver** | Ver AST | Muestra el árbol de sintaxis abstracta como gráfico |
| | Ver Tokens | Muestra la lista completa de tokens generados |
| | Ver Errores | Muestra los errores léxicos, sintácticos y semánticos |

## 5. Cómo Usar el Compilador

### Paso 1: Escribir o abrir un programa

Escriba código GoLite directamente en el editor o abra un archivo existente con **Archivo → Abrir**.

### Paso 2: Guardar el archivo

Guarde su programa con **Archivo → Guardar** o **Archivo → Guardar como** (extensión `.glt`).

### Paso 3: Ejecutar

Presione **Ejecutar → Run** o el botón correspondiente. El resultado se mostrará en la consola inferior.

### Paso 4: Depurar

Si hay errores, use **Ver → Ver Errores** para ver los detalles. También puede revisar los tokens generados con **Ver → Ver Tokens**.

### Paso 5: Visualizar el AST

Seleccione **Ver → Ver AST** para ver la representación gráfica del árbol de sintaxis abstracta del programa.

## 6. Sintaxis del Lenguaje GoLite

### Hola Mundo

```
func main() {
    fmt.Println("Hola, mundo!")
}
```

### Variables

```
var x int = 10
var nombre string = "Juan"
edad := 25
var precio float64 = 99.99
var activo bool = true
var letra rune = 'A'
```

### Tipos de Datos

| Tipo | Descripción | Ejemplo |
|---|---|---|
| `int` | Entero con signo de 64 bits | `42` |
| `float64` | Número de punto flotante | `3.14` |
| `string` | Cadena de texto | `"hola"` |
| `bool` | Booleano | `true`, `false` |
| `rune` | Caracter Unicode | `'a'` |
| `[]T` | Slice de tipo T | `[]int{1, 2, 3}` |
| `[][]T` | Slice 2D de tipo T | `[][]int{{1,2},{3,4}}` |
| `struct` | Estructura con campos | `struct Nombre { ... }` |

### Estructuras de Control

**If/Else:**
```
if x > 10 {
    fmt.Println("mayor")
} else if x > 5 {
    fmt.Println("medio")
} else {
    fmt.Println("menor")
}
```

**For:**
```
// Con condición
for x < 10 {
    x = x + 1
}

// Tres partes
for i := 0; i < 5; i++ {
    fmt.Println(i)
}
```

**Switch:**
```
switch dia {
    case 1:
        fmt.Println("Lunes")
    case 2:
        fmt.Println("Martes")
    default:
        fmt.Println("Otro")
}
```

### Funciones

```
func suma(a int, b int) int {
    return a + b
}

func main() {
    resultado := suma(3, 4)
    fmt.Println(resultado)
}
```

### Structs y Métodos

```
struct Persona {
    string nombre
    int edad
}

func (p Persona) saludar() {
    fmt.Println("Hola, soy " + p.nombre)
}

func main() {
    p := Persona{"Juan", 30}
    p.saludar()
}
```

### Slices

```
nums := []int{1, 2, 3, 4, 5}
fmt.Println(nums[0])      // acceso
nums[1] = 10              // asignación
fmt.Println(len(nums))    // longitud
nums = append(nums, 6)    // agregar
```

### Funciones de Biblioteca

| Función | Descripción |
|---|---|
| `fmt.Println(valor)` | Imprime un valor en consola |
| `strconv.Atoi(cadena)` | Convierte string a int |
| `strconv.ParseFloat(cadena)` | Convierte string a float64 |
| `reflect.TypeOf(valor)` | Devuelve el tipo como string |
| `slices.Index(slice, valor)` | Busca índice de un elemento |
| `strings.Join(slice, separador)` | Une slice de strings |
| `len(valor)` | Longitud de slice o string |
| `append(slice, valores...)` | Agrega elementos a un slice |

### Operadores

| Categoría | Operadores |
|---|---|
| Aritméticos | `+`, `-`, `*`, `/`, `%` |
| Comparación | `==`, `!=`, `<`, `>`, `<=`, `>=` |
| Lógicos | `&&`, `\|\|`, `!` |
| Asignación | `=`, `+=`, `-=`, `:=` |
| Incremento/Decremento | `++`, `--` |

### Comentarios

```
// Comentario de una línea

/*
   Comentario
   multilínea
*/
```

## 7. Manejo de Errores

La aplicación reporta tres tipos de errores:

| Tipo | Descripción | Ejemplo |
|---|---|---|
| **Léxico** | Caracter no reconocido | `@` no es válido |
| **Sintáctico** | Estructura gramatical incorrecta | `if x > 10 {` sin cerrar |
| **Semántico** | Error de tipos o ámbito | variable no declarada, tipos incompatibles |

Los errores se muestran en la consola y también pueden consultarse en **Ver → Ver Errores**.

## 8. Ejemplos de Programas

### Calculadora simple

```
func main() {
    a := 10
    b := 3
    fmt.Println("Suma:", a + b)
    fmt.Println("Resta:", a - b)
    fmt.Println("Multiplicacion:", a * b)
    fmt.Println("Division:", a / b)
}
```

### Promedio de calificaciones

```
func promedio(notas []int) float64 {
    suma := 0
    for i := 0; i < len(notas); i++ {
        suma = suma + notas[i]
    }
    return float64(suma) / float64(len(notas))
}

func main() {
    calif := []int{85, 90, 78, 92, 88}
    prom := promedio(calif)
    fmt.Println("Promedio:", prom)
}
```

## 9. Atajos de Teclado

| Atajo | Acción |
|---|---|
| `Ctrl+N` | Nuevo archivo |
| `Ctrl+O` | Abrir archivo |
| `Ctrl+S` | Guardar |
| `F5` | Ejecutar (Run) |
| `Ctrl+W` | Cerrar aplicación |

## 10. Solución de Problemas

**La aplicación no inicia:**
- Verifique que Java 21+ esté instalado: `java --version`
- Asegúrese de ejecutar el JAR con dependencias

**Error "No se encuentra el archivo":**
- Use la extensión `.glt` para los archivos de GoLite

**Error de compilación:**
- Revise la sintaxis del programa
- Consulte los errores en **Ver → Ver Errores**
- Asegúrese de tener una función `main()` sin parámetros
