package analisis.ast.stm;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;
import java.util.List;

public class StructDef implements NodoAST {
    private final String nombre;
    private final List<Campo> campos;
    private final int linea, columna;

    public StructDef(String nombre, List<Campo> campos, int linea, int columna) {
        this.nombre = nombre;
        this.campos = campos;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Campo {
        public final String nombre;
        public final String tipo;

        public Campo(String nombre, String tipo) {
            this.nombre = nombre;
            this.tipo = tipo;
        }
    }

    public static class Context {
        public final String nombre;
        public final List<Campo> campos;
        public final int linea, columna;

        public Context(StructDef node) {
            this.nombre = node.nombre;
            this.campos = node.campos;
            this.linea = node.linea;
            this.columna = node.columna;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
