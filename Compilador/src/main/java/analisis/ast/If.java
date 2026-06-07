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
public class If extends NodoAST  {
    
    public NodoAST condicion; // expresion de la condicion
    public Bloque bloqueIf; // bloque if... wao!
    public NodoAST bloqueElse; // bloque else... waos!

    public If(NodoAST condicion, Bloque bloqueIf, NodoAST bloqueElse, int linea, int columna) {
        super(linea, columna);
        this.bloqueElse = bloqueElse;
        this.bloqueIf = bloqueIf;
        this.condicion = condicion;
    }
    
    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
    
}
