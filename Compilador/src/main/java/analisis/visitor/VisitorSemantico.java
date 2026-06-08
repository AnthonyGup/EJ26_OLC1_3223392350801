/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package analisis.visitor;

import analisis.ast.Asignacion;
import analisis.ast.AsignacionOp;
import analisis.ast.Bloque;
import analisis.ast.Break;
import analisis.ast.Continue;
import analisis.ast.DeclaracionVar;
import analisis.ast.ExpBinaria;
import analisis.ast.ExpUnaria;
import analisis.ast.For;
import analisis.ast.FuncionMain;
import analisis.ast.Identificador;
import analisis.ast.If;
import analisis.ast.Literal;
import analisis.ast.LlamadaFuncion;
import analisis.ast.Programa;
import analisis.semantic.TablaSimbolos;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Anthony
 */
public class VisitorSemantico implements Visitor{
    private TablaSimbolos tabla;
    private List<String> errores;
    private boolean dentroDeFor; //Para validar break/continue
    
    public VisitorSemantico() {
        this.tabla = new TablaSimbolos();
        this.errores = new ArrayList<>();
        this.dentroDeFor  = false;
    }
    
    public List<String> getErrores() {
        return errores;
    }
    
    public TablaSimbolos getTabla() {
        return tabla;
    }
    
    // Cada visit() hace lo siguiente:
    // 1. Visita los hijos (si tiene)
    // 2. Valida reglas semánticas
    // 3. Reporta errores si encuentra

    @Override
    public void visit(Programa nodo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void visit(FuncionMain nodo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void visit(Bloque nodo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void visit(DeclaracionVar nodo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void visit(Asignacion nodo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void visit(AsignacionOp nodo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void visit(If nodo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void visit(For nodo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void visit(Break nodo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void visit(Continue nodo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void visit(LlamadaFuncion nodo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void visit(ExpBinaria nodo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void visit(ExpUnaria nodo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void visit(Literal nodo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void visit(Identificador nodo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
