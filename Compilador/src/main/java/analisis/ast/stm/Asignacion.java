package analisis.ast.stm;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;

public class Asignacion implements NodoAST {
    private final String id;
    private final NodoAST expr;
    private final int linea;
    private final int columna;

    public Asignacion(String id, NodoAST expr, int linea, int columna) {
        this.id = id;
        this.expr = expr;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final String id;
        public final NodoAST expr;
        public final int linea;
        public final int columna;

        public Context(Asignacion node) {
            this.id = node.id;
            this.expr = node.expr;
            this.linea = node.linea;
            this.columna = node.columna;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
