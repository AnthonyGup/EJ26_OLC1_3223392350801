Proyecto Compilador
===================

Descripción
-----------
Proyecto de un compilador con interfaz gráfica (Swing). La clase principal es `analisis.Compilador`, que inicia la ventana `VentanaPrincipal`.

Requisitos
---------
- Java 21 (o compatible con la configuración de Maven)
- Maven

Compilar y generar JAR ejecutable
---------------------------------
1. Generar el proyecto y el JAR con dependencias:

```bash
mvn clean package
```

2. El ensamblado genera un JAR con dependencias en `target/`, por ejemplo:

`target/Compilador-1.0-SNAPSHOT-jar-with-dependencies.jar`

3. Ejecutar la aplicación:

```bash
java -jar target/Compilador-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Ejecución desde el IDE
---------------------
La clase `analisis.Compilador` puede ejecutarse directamente desde un IDE (Run → `analisis.Compilador`) para lanzar la interfaz gráfica.

Estructura relevante
--------------------
- `src/main/java/analisis/Compilador.java`: clase con `main`.
- `src/main/java/usac/compi1/gui`: ventanas e interfaz gráfica.
- `src/main/cup` y `src/main/jflex`: archivos para el analizador léxico y sintáctico.

Notas
-----
- El `pom.xml` está configurado para generar código desde JFlex/CUP y para crear un JAR con dependencias.
- En caso de problemas al ejecutar, se debe comprobar que Maven esté configurado y que la generación de fuentes (JFlex/CUP) se complete sin errores.
