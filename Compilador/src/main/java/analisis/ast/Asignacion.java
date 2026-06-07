package analisis.ast;

import analisis.visitor.Visitor;

public class Asignacion extends NodoAST {
    public String id;       // nombre de la variable
    public NodoAST expr;    // nuevo valor

    public Asignacion(String id, NodoAST expr, int linea, int columna) {
        super(linea, columna);
        this.id = id;
        this.expr = expr;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
