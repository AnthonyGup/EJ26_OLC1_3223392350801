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
public class ExpBinaria extends NodoAST {
    
    public NodoAST izquierdo;
    public String operador;       // "+", "-", "*", "/", "%"
    public NodoAST derecho;

    public ExpBinaria(NodoAST izq, String op, NodoAST der, int l, int c) {
        super(l, c);
        this.izquierdo = izq;
        this.operador = op;
        this.derecho = der;
    }

    @Override 
    public void accept(Visitor v) { 
        v.visit(this); 
    }
}
