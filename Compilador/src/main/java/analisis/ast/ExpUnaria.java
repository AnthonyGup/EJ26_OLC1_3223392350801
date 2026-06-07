package analisis.ast;

import analisis.visitor.Visitor;

public class ExpUnaria extends NodoAST {
    public String operador; // "-" o "!"
    public NodoAST expr;

    public ExpUnaria(String operador, NodoAST expr, int linea, int columna) {
        super(linea, columna);
        this.operador = operador;
        this.expr = expr;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
