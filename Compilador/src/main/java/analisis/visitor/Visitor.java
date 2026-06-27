package analisis.visitor;

import analisis.ast.*;
import analisis.ast.exp.*;
import analisis.ast.stm.*;

public interface Visitor<T> {
    T visit(Programa.Context ctx);
    T visit(Bloque.Context ctx);
    T visit(DeclaracionVar.Context ctx);
    T visit(Asignacion.Context ctx);
    T visit(AsignacionOp.Context ctx);
    T visit(If.Context ctx);
    T visit(For.Context ctx);
    T visit(Switch.Context ctx);
    T visit(Break.Context ctx);
    T visit(Continue.Context ctx);
    T visit(LlamadaFuncion.Context ctx);
    T visit(ExpBinaria.Context ctx);
    T visit(ExpUnaria.Context ctx);
    T visit(Literal.Context ctx);
    T visit(Identificador.Context ctx);
    T visit(SliceLiteral.Context ctx);
    T visit(Len.Context ctx);
    T visit(Append.Context ctx);
    T visit(IndexAccess.Context ctx);
    T visit(AssignIndex.Context ctx);
    T visit(SliceLiteral2D.Context ctx);
    T visit(AssignIndex2D.Context ctx);
    T visit(StructDef.Context ctx);
    T visit(StructAccess.Context ctx);
    T visit(StructAssign.Context ctx);
    T visit(NewStruct.Context ctx);
    T visit(StructMethodDef.Context ctx);
    T visit(StructMethodCall.Context ctx);
    T visit(FuncDef.Context ctx);
    T visit(FuncCall.Context ctx);
    T visit(ReturnStmt.Context ctx);
}
