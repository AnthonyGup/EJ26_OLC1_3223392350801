package analisis.ast.stm;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;

public class ReturnStmt implements NodoAST {
    private final NodoAST valor;
    private final int linea, columna;

    public ReturnStmt(NodoAST valor, int linea, int columna) {
        this.valor = valor;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final NodoAST valor;
        public final int linea, columna;

        public Context(ReturnStmt node) {
            this.valor = node.valor;
            this.linea = node.linea;
            this.columna = node.columna;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
