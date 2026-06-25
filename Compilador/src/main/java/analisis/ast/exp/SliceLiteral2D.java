package analisis.ast.exp;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;
import java.util.List;

public class SliceLiteral2D implements NodoAST {
    private final String tipo;
    private final List<List<NodoAST>> filas;
    private final int linea, columna;

    public SliceLiteral2D(String tipo, List<List<NodoAST>> filas, int linea, int columna) {
        this.tipo = tipo;
        this.filas = filas;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final String tipo;
        public final List<List<NodoAST>> filas;
        public final int linea, columna;

        public Context(SliceLiteral2D node) {
            this.tipo = node.tipo;
            this.filas = node.filas;
            this.linea = node.linea;
            this.columna = node.columna;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
