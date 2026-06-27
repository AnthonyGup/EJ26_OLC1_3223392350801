package analisis.ast.exp;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;
import java.util.List;

public class FuncCall implements NodoAST {
    private final String nombre;
    private final List<NodoAST> argumentos;
    private final int linea, columna;

    public FuncCall(String nombre, List<NodoAST> argumentos, int linea, int columna) {
        this.nombre = nombre;
        this.argumentos = argumentos;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final String nombre;
        public final List<NodoAST> argumentos;
        public final int linea, columna;

        public Context(FuncCall node) {
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
