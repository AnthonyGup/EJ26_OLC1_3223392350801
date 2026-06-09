package analisis.ast.exp;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;

public class ExpBinaria implements NodoAST {
    private final NodoAST izquierdo;
    private final String operador;
    private final NodoAST derecho;
    private final int linea;
    private final int columna;

    public ExpBinaria(NodoAST izquierdo, String operador, NodoAST derecho, int linea, int columna) {
        this.izquierdo = izquierdo;
        this.operador = operador;
        this.derecho = derecho;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final NodoAST izquierdo;
        public final String operador;
        public final NodoAST derecho;
        public final int linea;
        public final int columna;

        public Context(ExpBinaria node) {
            this.izquierdo = node.izquierdo;
            this.operador = node.operador;
            this.derecho = node.derecho;
            this.linea = node.linea;
            this.columna = node.columna;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
