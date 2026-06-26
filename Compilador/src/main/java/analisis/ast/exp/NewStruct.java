package analisis.ast.exp;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;
import java.util.List;

public class NewStruct implements NodoAST {
    private final String nombreTipo;
    private final List<CampoInit> campos;
    private final int linea, columna;

    public NewStruct(String nombreTipo, List<CampoInit> campos, int linea, int columna) {
        this.nombreTipo = nombreTipo;
        this.campos = campos;
        this.linea = linea;
        this.columna = columna;
    }

    public static class CampoInit {
        public final String nombre;
        public final NodoAST valor;

        public CampoInit(String nombre, NodoAST valor) {
            this.nombre = nombre;
            this.valor = valor;
        }
    }

    public static class Context {
        public final String nombreTipo;
        public final List<CampoInit> campos;
        public final int linea, columna;

        public Context(NewStruct node) {
            this.nombreTipo = node.nombreTipo;
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
