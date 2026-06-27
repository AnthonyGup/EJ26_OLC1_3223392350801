package analisis.ast.stm;

import analisis.ast.NodoAST;
import analisis.ast.Parametro;
import analisis.visitor.Visitor;
import java.util.List;

public class StructMethodDef implements NodoAST {
    private final String receiverVar;
    private final String receiverType;
    private final String nombre;
    private final List<Parametro> parametros;
    private final NodoAST body;
    private final int linea, columna;

    public StructMethodDef(String receiverVar, String receiverType, String nombre, List<Parametro> parametros, NodoAST body, int linea, int columna) {
        this.receiverVar = receiverVar;
        this.receiverType = receiverType;
        this.nombre = nombre;
        this.parametros = parametros;
        this.body = body;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final String receiverVar;
        public final String receiverType;
        public final String nombre;
        public final List<Parametro> parametros;
        public final NodoAST body;
        public final int linea, columna;

        public Context(StructMethodDef node) {
            this.receiverVar = node.receiverVar;
            this.receiverType = node.receiverType;
            this.nombre = node.nombre;
            this.parametros = node.parametros;
            this.body = node.body;
            this.linea = node.linea;
            this.columna = node.columna;
        }
    }

    public String getNombre() { return nombre; }
    public String getReceiverType() { return receiverType; }
    public String getReceiverVar() { return receiverVar; }
    public List<Parametro> getParametros() { return parametros; }
    public NodoAST getBody() { return body; }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
