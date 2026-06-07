/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package analisis.ast;

import analisis.visitor.Visitor;

/**
 *
 * @author Anthony
 */
public class For extends NodoAST {
    // for condicion {}
     public NodoAST init;       // null en  for simple
     public NodoAST condicion;  // expresion bool
     public NodoAST update;     // null en for simple
     public Bloque bloque;

    public For(NodoAST condicion, Bloque bloque, int linea, int columna) {
        super(linea, columna);
        this.init = null;
        this.condicion = condicion;
        this.update = null;
        this.bloque = bloque;
    }
    
    // Constructor para: for init; cond; update { }
    public For(NodoAST init, NodoAST cond, NodoAST update, Bloque bloque, int l, int c) {
        super(l, c);
        this.init = init;
        this.condicion = cond;
        this.update = update;
        this.bloque = bloque;
    }

    
    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
     
     
}
