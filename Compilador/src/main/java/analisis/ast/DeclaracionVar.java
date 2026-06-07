package analisis.ast;

import analisis.visitor.Visitor;

public class DeclaracionVar extends NodoAST {
    public String id;       // nombre de la variable
    public String tipo;     // "int", "float64", "string", "bool", "rune" (null si es :=)
    public NodoAST expr;    // expresion de inicializacion (null si no tiene)

    // Constructor para: var x int = expr
    public DeclaracionVar(String id, String tipo, NodoAST expr, int linea, int columna) {
        super(linea, columna);
        this.id = id;
        this.tipo = tipo;
        this.expr = expr;
    }

    // Constructor para: var x int (sin inicializacion)
    public DeclaracionVar(String id, String tipo, int linea, int columna) {
        super(linea, columna);
        this.id = id;
        this.tipo = tipo;
        this.expr = null;
    }

    // Constructor para: x := expr (inferencia de tipo, tipo se determina en semantico)
    public DeclaracionVar(String id, NodoAST expr, int linea, int columna) {
        super(linea, columna);
        this.id = id;
        this.tipo = null;
        this.expr = expr;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
