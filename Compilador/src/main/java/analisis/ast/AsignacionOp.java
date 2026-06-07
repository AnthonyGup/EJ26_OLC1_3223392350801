package analisis.ast;

import analisis.visitor.Visitor;

public class AsignacionOp extends NodoAST {
    public String id;        // nombre de la variable
    public String operador;  // "+=" o "-="
    public NodoAST expr;     // expresion a sumar/restar

    public AsignacionOp(String id, String operador, NodoAST expr, int linea, int columna) {
        super(linea, columna);
        this.id = id;
        this.operador = operador;
        this.expr = expr;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
