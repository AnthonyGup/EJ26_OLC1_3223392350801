package analisis.ast.stm;

import analisis.ast.NodoAST;
import analisis.ast.Parametro;
import analisis.visitor.Visitor;
import java.util.List;

public class FuncDef implements NodoAST {
    private final String nombre;
    private final List<Parametro> parametros;
    private final String tipoRetorno;
    private final NodoAST body;
    private final int linea, columna;

    public FuncDef(String nombre, List<Parametro> parametros, String tipoRetorno, NodoAST body, int linea, int columna) {
        this.nombre = nombre;
        this.parametros = parametros;
        this.tipoRetorno = tipoRetorno;
        this.body = body;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final String nombre;
        public final List<Parametro> parametros;
        public final String tipoRetorno;
        public final NodoAST body;
        public final int linea, columna;

        public Context(FuncDef node) {
            this.nombre = node.nombre;
            this.parametros = node.parametros;
            this.tipoRetorno = node.tipoRetorno;
            this.body = node.body;
            this.linea = node.linea;
            this.columna = node.columna;
        }
    }

    public String getNombre() { return nombre; }
    public List<Parametro> getParametros() { return parametros; }
    public String getTipoRetorno() { return tipoRetorno; }
    public NodoAST getBody() { return body; }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
