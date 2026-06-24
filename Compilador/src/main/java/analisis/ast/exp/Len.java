package analisis.ast.exp;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;

public class Len implements NodoAST {
    private final NodoAST expr;
    private final int linea;
    private final int columna;

    public Len(NodoAST expr, int linea, int columna) {
        this.expr = expr;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final NodoAST expr;
        public final int linea;
        public final int columna;

        public Context(Len node) {
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
