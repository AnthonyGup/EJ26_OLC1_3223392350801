package analisis.ast.stm;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;

public class Break implements NodoAST {
    private final int linea;
    private final int columna;

    public Break(int linea, int columna) {
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final int linea;
        public final int columna;

        public Context(Break node) {
            this.linea = node.linea;
            this.columna = node.columna;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
