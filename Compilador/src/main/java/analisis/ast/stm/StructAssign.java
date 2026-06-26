package analisis.ast.stm;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;

public class StructAssign implements NodoAST {
    private final String structId;
    private final String campo;
    private final NodoAST valor;
    private final int linea, columna;

    public StructAssign(String structId, String campo, NodoAST valor, int linea, int columna) {
        this.structId = structId;
        this.campo = campo;
        this.valor = valor;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final String structId;
        public final String campo;
        public final NodoAST valor;
        public final int linea, columna;

        public Context(StructAssign node) {
            this.structId = node.structId;
            this.campo = node.campo;
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
