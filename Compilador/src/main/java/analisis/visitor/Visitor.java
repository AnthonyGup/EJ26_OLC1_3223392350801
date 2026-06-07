/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package analisis.visitor;

import analisis.ast.*;

/**
 *
 * @author Anthony
 */
public interface Visitor {
    
    void visit(Programa nodo);
    void visit(FuncionMain nodo);
    void visit(Bloque nodo);
    void visit(DeclaracionVar nodo);
    void visit(Asignacion nodo);
    void visit(AsignacionOp nodo);
    void visit(If nodo);
    void visit(For nodo);
    void visit(Break nodo);
    void visit(Continue nodo);
    void visit(LlamadaFuncion nodo);
    void visit(ExpBinaria nodo);
    void visit(ExpUnaria nodo);
    void visit(Literal nodo);
    void visit(Identificador nodo);
    
}
