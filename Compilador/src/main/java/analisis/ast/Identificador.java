package analisis.ast;

import analisis.visitor.Visitor;

public class Identificador extends NodoAST {
    public String nombre;

    public Identificador(String nombre, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
