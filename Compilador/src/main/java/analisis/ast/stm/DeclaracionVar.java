package analisis.ast.stm;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;

public class DeclaracionVar implements NodoAST {
    private final String id;
    private final String tipo;
    private final NodoAST expr;
    private final int linea;
    private final int columna;

    public DeclaracionVar(String id, String tipo, NodoAST expr, int linea, int columna) {
        this.id = id;
        this.tipo = tipo;
        this.expr = expr;
        this.linea = linea;
        this.columna = columna;
    }

    public DeclaracionVar(String id, String tipo, int linea, int columna) {
        this.id = id;
        this.tipo = tipo;
        this.expr = null;
        this.linea = linea;
        this.columna = columna;
    }

    public DeclaracionVar(String id, NodoAST expr, int linea, int columna) {
        this.id = id;
        this.tipo = null;
        this.expr = expr;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final String id;
        public final String tipo;
        public final NodoAST expr;
        public final int linea;
        public final int columna;

        public Context(DeclaracionVar node) {
            this.id = node.id;
            this.tipo = node.tipo;
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
