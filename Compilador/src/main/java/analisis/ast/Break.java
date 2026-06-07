package analisis.ast;

import analisis.visitor.Visitor;

public class Break extends NodoAST {

    public Break(int linea, int columna) {
        super(linea, columna);
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
