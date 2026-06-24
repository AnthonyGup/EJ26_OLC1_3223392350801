package analisis.ast.exp;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;

public class IndexAccess implements NodoAST {
    private final NodoAST base;
    private final NodoAST indice;
    private final int linea;
    private final int columna;

    public IndexAccess(NodoAST base, NodoAST indice, int linea, int columna) {
        this.base = base;
        this.indice = indice;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final NodoAST base;
        public final NodoAST indice;
        public final int linea;
        public final int columna;

        public Context(IndexAccess nodo) {
            this.base = nodo.base;
            this.indice = nodo.indice;
            this.linea = nodo.linea;
            this.columna = nodo.columna;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
