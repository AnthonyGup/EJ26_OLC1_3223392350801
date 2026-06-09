package analisis.visitor;

import analisis.ast.*;
import analisis.ast.exp.ExpBinaria;
import analisis.ast.exp.ExpUnaria;
import analisis.ast.exp.Identificador;
import analisis.ast.exp.Literal;
import analisis.ast.stm.Asignacion;
import analisis.ast.stm.AsignacionOp;
import analisis.ast.stm.Break;
import analisis.ast.stm.Continue;
import analisis.ast.stm.DeclaracionVar;
import analisis.ast.stm.For;
import analisis.ast.stm.If;
import analisis.ast.stm.LlamadaFuncion;
import analisis.semantic.TablaSimbolos;
import java.util.ArrayList;
import java.util.List;

public class VisitorSemantico implements Visitor<Void> {
    private TablaSimbolos tabla;
    private List<String> errores;
    private boolean dentroDeFor;

    public VisitorSemantico() {
        this.tabla = new TablaSimbolos();
        this.errores = new ArrayList<>();
        this.dentroDeFor = false;
    }

    public List<String> getErrores() {
        return errores;
    }

    public TablaSimbolos getTabla() {
        return tabla;
    }

    @Override
    public Void visit(Programa.Context ctx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Void visit(FuncionMain.Context ctx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Void visit(Bloque.Context ctx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Void visit(DeclaracionVar.Context ctx) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Void visit(Asignacion.Context ctx) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Void visit(AsignacionOp.Context ctx) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Void visit(If.Context ctx) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Void visit(For.Context ctx) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Void visit(Break.Context ctx) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Void visit(Continue.Context ctx) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Void visit(LlamadaFuncion.Context ctx) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Void visit(ExpBinaria.Context ctx) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Void visit(ExpUnaria.Context ctx) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Void visit(Literal.Context ctx) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Void visit(Identificador.Context ctx) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
