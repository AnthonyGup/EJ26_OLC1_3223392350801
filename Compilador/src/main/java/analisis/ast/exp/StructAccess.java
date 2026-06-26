package analisis.ast.exp;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;

public class StructAccess implements NodoAST {
    private final String structId;
    private final String campo;
    private final int linea, columna;

    public StructAccess(String structId, String campo, int linea, int columna) {
        this.structId = structId;
        this.campo = campo;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final String structId;
        public final String campo;
        public final int linea, columna;

        public Context(StructAccess node) {
            this.structId = node.structId;
            this.campo = node.campo;
            this.linea = node.linea;
            this.columna = node.columna;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
