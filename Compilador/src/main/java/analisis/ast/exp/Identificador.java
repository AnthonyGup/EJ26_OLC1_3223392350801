package analisis.ast.exp;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;

public class Identificador implements NodoAST {
    private final String nombre;
    private final int linea;
    private final int columna;

    public Identificador(String nombre, int linea, int columna) {
        this.nombre = nombre;
        this.linea = linea;
        this.columna = columna;
    }

    public String getNombre() { return nombre; }

    public static class Context {
        public final String nombre;
        public final int linea;
        public final int columna;

        public Context(Identificador node) {
            this.nombre = node.nombre;
            this.linea = node.linea;
            this.columna = node.columna;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
