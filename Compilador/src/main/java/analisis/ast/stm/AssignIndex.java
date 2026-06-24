package analisis.ast.stm;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;

public class AssignIndex implements NodoAST {
    private final String id;
    private final NodoAST indice;
    private final NodoAST valor;
    private final int linea;
    private final int columna;

    public AssignIndex(String id, NodoAST indice, NodoAST valor, int linea, int columna) {
        this.id = id;
        this.indice = indice;
        this.valor = valor;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final String id;
        public final NodoAST indice;
        public final NodoAST valor;
        public final int linea;
        public final int columna;

        public Context(AssignIndex nodo) {
            this.id = nodo.id;
            this.indice = nodo.indice;
            this.valor = nodo.valor;
            this.linea = nodo.linea;
            this.columna = nodo.columna;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
