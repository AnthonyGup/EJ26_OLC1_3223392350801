package analisis.ast.exp;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;

public class Literal implements NodoAST {
    private final Object valor;
    private final String tipo;
    private final int linea;
    private final int columna;

    public Literal(Object valor, String tipo, int linea, int columna) {
        this.valor = valor;
        this.tipo = tipo;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final Object valor;
        public final String tipo;
        public final int linea;
        public final int columna;

        public Context(Literal node) {
            this.valor = node.valor;
            this.tipo = node.tipo;
            this.linea = node.linea;
            this.columna = node.columna;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
