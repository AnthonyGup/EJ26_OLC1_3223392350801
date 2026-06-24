package analisis.ast.exp;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;
import java.util.List;

public class Append implements NodoAST {
    private final NodoAST slice;
    private final List<NodoAST> elementos;
    private final int linea;
    private final int columna;

    public Append(NodoAST slice, List<NodoAST> elementos, int linea, int columna) {
        this.slice = slice;
        this.elementos = elementos;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final NodoAST slice;
        public final List<NodoAST> elementos;
        public final int linea;
        public final int columna;

        public Context(Append node) {
            this.slice = node.slice;
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
