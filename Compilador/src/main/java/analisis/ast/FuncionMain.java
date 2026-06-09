package analisis.ast;

import analisis.visitor.Visitor;

public class FuncionMain implements NodoAST {
    private final Bloque bloque;
    private final int linea;
    private final int columna;

    public FuncionMain(Bloque bloque, int linea, int columna) {
        this.bloque = bloque;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final Bloque bloque;
        public final int linea;
        public final int columna;

        public Context(FuncionMain node) {
            this.bloque = node.bloque;
            this.linea = node.linea;
            this.columna = node.columna;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
