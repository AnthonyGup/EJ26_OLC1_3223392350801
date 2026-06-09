package analisis.ast.stm;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;

public class AsignacionOp implements NodoAST {
    private final String id;
    private final String operador;
    private final NodoAST expr;
    private final int linea;
    private final int columna;

    public AsignacionOp(String id, String operador, NodoAST expr, int linea, int columna) {
        this.id = id;
        this.operador = operador;
        this.expr = expr;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final String id;
        public final String operador;
        public final NodoAST expr;
        public final int linea;
        public final int columna;

        public Context(AsignacionOp node) {
            this.id = node.id;
            this.operador = node.operador;
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
