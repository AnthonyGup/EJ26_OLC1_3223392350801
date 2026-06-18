package analisis.ast.stm;

import analisis.ast.NodoAST;
import analisis.visitor.Visitor;
import java.util.List;

public class LlamadaFuncion implements NodoAST {
    private final String paquete;
    private final String funcion;
    private final List<NodoAST> argumentos;
    private final int linea;
    private final int columna;

    public LlamadaFuncion(String paquete, String funcion, List<NodoAST> argumentos, int linea, int columna) {
        this.paquete = paquete;
        this.funcion = funcion;
        this.argumentos = argumentos;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final String paquete;
        public final String funcion;
        public final List<NodoAST> argumentos;
        public final int linea;
        public final int columna;

        public Context(LlamadaFuncion node) {
            this.paquete = node.paquete;
            this.funcion = node.funcion;
            this.argumentos = node.argumentos;
            this.linea = node.linea;
            this.columna = node.columna;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
