package analisis.ast;

import analisis.ast.stm.FuncDef;
import analisis.ast.stm.StructDef;
import analisis.ast.stm.StructMethodDef;
import analisis.visitor.Visitor;
import java.util.List;

public class Programa implements NodoAST {
    private final List<NodoAST> instrucciones;
    private final List<StructDef> structDefs;
    private final List<StructMethodDef> structMethods;
    private final List<FuncDef> funcDefs;
    private final int linea;
    private final int columna;

    public Programa(List<NodoAST> instrucciones, int linea, int columna) {
        this.instrucciones = instrucciones;
        this.structDefs = List.of();
        this.structMethods = List.of();
        this.funcDefs = List.of();
        this.linea = linea;
        this.columna = columna;
    }

    public Programa(List<StructDef> structDefs, List<NodoAST> instrucciones, int linea, int columna) {
        this.instrucciones = instrucciones;
        this.structDefs = structDefs;
        this.structMethods = List.of();
        this.funcDefs = List.of();
        this.linea = linea;
        this.columna = columna;
    }

    public Programa(List<StructDef> structDefs, List<StructMethodDef> structMethods, List<NodoAST> instrucciones, int linea, int columna) {
        this.instrucciones = instrucciones;
        this.structDefs = structDefs;
        this.structMethods = structMethods;
        this.funcDefs = List.of();
        this.linea = linea;
        this.columna = columna;
    }

    public Programa(List<StructDef> structDefs, List<StructMethodDef> structMethods, List<FuncDef> funcDefs, List<NodoAST> instrucciones, int linea, int columna) {
        this.instrucciones = instrucciones;
        this.structDefs = structDefs;
        this.structMethods = structMethods;
        this.funcDefs = funcDefs;
        this.linea = linea;
        this.columna = columna;
    }

    public static class Context {
        public final List<NodoAST> instrucciones;
        public final List<StructDef> structDefs;
        public final List<StructMethodDef> structMethods;
        public final List<FuncDef> funcDefs;
        public final int linea;
        public final int columna;

        public Context(Programa node) {
            this.instrucciones = node.instrucciones;
            this.structDefs = node.structDefs;
            this.structMethods = node.structMethods;
            this.funcDefs = node.funcDefs;
            this.linea = node.linea;
            this.columna = node.columna;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}
