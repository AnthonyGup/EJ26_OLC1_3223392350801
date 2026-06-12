package analisis.ast;

import analisis.visitor.Visitor;
import java.util.List;

public class Programa implements NodoAST {
    private final List<NodoAST> instrucciones;
    private final int linea;
    private final int columna;

    public Programa(List<NodoAST> instrucciones, int linea, int columna) {
        this.instrucciones = instrucciones;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final List<NodoAST> instrucciones;
        public final int linea;
        public final int columna;

        public Context(Programa node) {
            this.instrucciones = node.instrucciones;
            this.linea = node.linea;
            this.columna = node.columna;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
