/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package analisis.ast;

import analisis.visitor.Visitor;
import java.util.List;

/**
 *
 * @author Anthony
 */
public class Bloque extends NodoAST {
    public List<NodoAST> instrucciones;

    public Bloque(List<NodoAST> instrucciones, int linea, int columna) {
        super(linea, columna);
        this.instrucciones = instrucciones;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
