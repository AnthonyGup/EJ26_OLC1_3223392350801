package analisis.ast.stm;

import analisis.ast.Bloque;
import analisis.ast.NodoAST;
import analisis.visitor.Visitor;

public class If implements NodoAST {
    private final NodoAST condicion;
    private final Bloque bloqueIf;
    private final NodoAST bloqueElse;
    private final int linea;
    private final int columna;

    public If(NodoAST condicion, Bloque bloqueIf, NodoAST bloqueElse, int linea, int columna) {
        this.condicion = condicion;
        this.bloqueIf = bloqueIf;
        this.bloqueElse = bloqueElse;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final NodoAST condicion;
        public final Bloque bloqueIf;
        public final NodoAST bloqueElse;
        public final int linea;
        public final int columna;

        public Context(If node) {
            this.condicion = node.condicion;
            this.bloqueIf = node.bloqueIf;
            this.bloqueElse = node.bloqueElse;
            this.linea = node.linea;
            this.columna = node.columna;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
