package analisis.ast.exp;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;
import java.util.List;

public class SliceLiteral implements NodoAST {
    private final String tipoElemento;
    private final List<NodoAST> elementos;
    private final int linea;
    private final int columna;

    public SliceLiteral(String tipoElemento, List<NodoAST> elementos, int linea, int columna) {
        this.tipoElemento = tipoElemento;
        this.elementos = elementos;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final String tipoElemento;
        public final List<NodoAST> elementos;
        public final int linea;
        public final int columna;

        public Context(SliceLiteral node) {
            this.tipoElemento = node.tipoElemento;
            this.elementos = node.elementos;
            this.linea = node.linea;
            this.columna = node.columna;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
