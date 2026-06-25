package analisis.ast.stm;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;

public class AssignIndex2D implements NodoAST {
    private final String id;
    private final NodoAST indice1, indice2, valor;
    private final int linea, columna;

    public AssignIndex2D(String id, NodoAST indice1, NodoAST indice2, NodoAST valor, int linea, int columna) {
        this.id = id;
        this.indice1 = indice1;
        this.indice2 = indice2;
        this.valor = valor;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final String id;
        public final NodoAST indice1, indice2, valor;
        public final int linea, columna;

        public Context(AssignIndex2D node) {
            this.id = node.id;
            this.indice1 = node.indice1;
            this.indice2 = node.indice2;
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
