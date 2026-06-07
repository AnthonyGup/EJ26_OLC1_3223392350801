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
public class Literal extends NodoAST {
    
    public Object valor; // Integer, Double, String, Character,  Boolean, null
    public String tipo; // "int", "float64", "string", "bool", "rune", "nil"

    public Literal(Object valor, String tipo, int linea, int columna) {
        super(linea, columna);
        this.valor = valor;
        this.tipo = tipo;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
    
    
}
