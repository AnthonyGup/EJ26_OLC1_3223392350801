package analisis.ast;

import analisis.ast.stm.StructDef;
import analisis.visitor.Visitor;
import java.util.List;

public class Programa implements NodoAST {
    private final List<NodoAST> instrucciones;
    private final List<StructDef> structDefs;
    private final int linea;
    private final int columna;

    public Programa(List<NodoAST> instrucciones, int linea, int columna) {
        this.instrucciones = instrucciones;
        this.structDefs = List.of();
        this.linea = linea;
        this.columna = columna;
    }

    public Programa(List<StructDef> structDefs, List<NodoAST> instrucciones, int linea, int columna) {
        this.instrucciones = instrucciones;
        this.structDefs = structDefs;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final List<NodoAST> instrucciones;
        public final List<StructDef> structDefs;
        public final int linea;
        public final int columna;

        public Context(Programa node) {
            this.instrucciones = node.instrucciones;
            this.structDefs = node.structDefs;
            this.linea = node.linea;
            this.columna = node.columna;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
