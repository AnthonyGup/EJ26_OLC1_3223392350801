package analisis.ast;

public class Parametro {
    public final String nombre;
    public final String tipo;
    public final int linea, columna;

    public Parametro(String nombre, String tipo, int linea, int columna) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.linea = linea;
        this.columna = columna;
    }
}
