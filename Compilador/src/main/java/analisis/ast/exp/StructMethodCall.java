package analisis.ast.exp;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;
import java.util.List;

public class StructMethodCall implements NodoAST {
    private final String id;
    private final String nombre;
    private final List<NodoAST> argumentos;
    private final int linea, columna;

    public StructMethodCall(String id, String nombre, List<NodoAST> argumentos, int linea, int columna) {
        this.id = id;
        this.nombre = nombre;
        this.argumentos = argumentos;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final String id;
        public final String nombre;
        public final List<NodoAST> argumentos;
        public final int linea, columna;

        public Context(StructMethodCall node) {
            this.id = node.id;
            this.nombre = node.nombre;
            this.argumentos = node.argumentos;
            this.linea = node.linea;
            this.columna = node.columna;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
