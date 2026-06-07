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
public class FuncionMain extends NodoAST {

    public Bloque bloque;
    
    public FuncionMain(Bloque bloque, int linea, int columna) {
        super(linea, columna);
        this.bloque = bloque;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
    
}
