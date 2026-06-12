package analisis.ast.stm;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;
import java.util.List;

public class Switch implements NodoAST {
    private final NodoAST expresion;
    private final List<Caso> casos;
    private final NodoAST bloqueDefault;
    private final int linea;
    private final int columna;

    public Switch(NodoAST expresion, List<Caso> casos, NodoAST bloqueDefault, int linea, int columna) {
        this.expresion = expresion;
        this.casos = casos;
        this.bloqueDefault = bloqueDefault;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Caso {
        public final List<NodoAST> expresiones;
        public final NodoAST bloque;
        public final int linea;
        public final int columna;

        public Caso(List<NodoAST> expresiones, NodoAST bloque, int linea, int columna) {
            this.expresiones = expresiones;
            this.bloque = bloque;
            this.linea = linea;
            this.columna = columna;
        }
    }

    public static class Context {
        public final NodoAST expresion;
        public final List<Caso> casos;
        public final NodoAST bloqueDefault;
        public final int linea;
        public final int columna;

        public Context(Switch node) {
            this.expresion = node.expresion;
            this.casos = node.casos;
            this.bloqueDefault = node.bloqueDefault;
            this.linea = node.linea;
            this.columna = node.columna;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}