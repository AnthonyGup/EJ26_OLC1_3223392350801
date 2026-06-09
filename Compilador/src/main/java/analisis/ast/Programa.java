package analisis.ast;

import analisis.visitor.Visitor;

public class Programa implements NodoAST {
    private final FuncionMain main;
    private final int linea;
    private final int columna;

    public Programa(FuncionMain main, int linea, int columna) {
        this.main = main;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final FuncionMain main;
        public final int linea;
        public final int columna;

        public Context(Programa node) {
            this.main = node.main;
            this.linea = node.linea;
            this.columna = node.columna;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
