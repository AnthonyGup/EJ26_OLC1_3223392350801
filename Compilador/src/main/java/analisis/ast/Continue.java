package analisis.ast;

import analisis.visitor.Visitor;

public class Continue extends NodoAST {

    public Continue(int linea, int columna) {
        super(linea, columna);
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
