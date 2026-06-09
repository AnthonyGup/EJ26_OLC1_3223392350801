package analisis.ast.stm;

import analisis.ast.Bloque;
import analisis.ast.NodoAST;
import analisis.visitor.Visitor;

public class For implements NodoAST {
    private final NodoAST init;
    private final NodoAST condicion;
    private final NodoAST update;
    private final Bloque bloque;
    private final int linea;
    private final int columna;

    public For(NodoAST condicion, Bloque bloque, int linea, int columna) {
        this.init = null;
        this.condicion = condicion;
        this.update = null;
        this.bloque = bloque;
        this.linea = linea;
        this.columna = columna;
    }

    public For(NodoAST init, NodoAST condicion, NodoAST update, Bloque bloque, int linea, int columna) {
        this.init = init;
        this.condicion = condicion;
        this.update = update;
        this.bloque = bloque;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final NodoAST init;
        public final NodoAST condicion;
        public final NodoAST update;
        public final Bloque bloque;
        public final int linea;
        public final int columna;

        public Context(For node) {
            this.init = node.init;
            this.condicion = node.condicion;
            this.update = node.update;
            this.bloque = node.bloque;
            this.linea = node.linea;
            this.columna = node.columna;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
