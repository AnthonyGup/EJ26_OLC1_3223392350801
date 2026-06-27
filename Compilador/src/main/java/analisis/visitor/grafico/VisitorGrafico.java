package analisis.visitor.grafico;

import analisis.ast.*;
import analisis.ast.exp.*;
import analisis.ast.stm.*;
import analisis.visitor.Visitor;
import java.util.List;

public class VisitorGrafico implements Visitor<String> {

    private final StringBuilder sb = new StringBuilder();
    private int contador = 0;

    private String nextId() {
        return "n" + (contador++);
    }

    private String nodo(String id, String label) {
        sb.append("  ").append(id).append(" [label=\"").append(escape(label)).append("\"];\n");
        return id;
    }

    private void edge(String from, String to) {
        sb.append("  ").append(from).append(" -> ").append(to).append(";\n");
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    private String visitar(NodoAST nodo) {
        return nodo != null ? nodo.accept(this) : null;
    }

    private void enlazar(String idPadre, String idHijo, String label) {
        if (idHijo != null) {
            edge(idPadre, idHijo);
        }
    }

    public String getDot() {
        return "digraph AST {\n" +
               "  node [shape=box, style=rounded, fontname=\"Arial\"];\n" +
               sb.toString() +
               "}\n";
    }

    @Override
    public String visit(Programa.Context ctx) {
        String id = nextId();
        nodo(id, "Programa (" + ctx.linea + ":" + ctx.columna + ")");
        for (StructDef sd : ctx.structDefs) {
            String h = sd.accept(this);
            if (h != null) edge(id, h);
        }
        for (StructMethodDef smd : ctx.structMethods) {
            String h = smd.accept(this);
            if (h != null) edge(id, h);
        }
        for (FuncDef fd : ctx.funcDefs) {
            String h = fd.accept(this);
            if (h != null) edge(id, h);
        }
        for (NodoAST inst : ctx.instrucciones) {
            String h = inst.accept(this);
            if (h != null) edge(id, h);
        }
        return id;
    }

    @Override
    public String visit(Bloque.Context ctx) {
        String id = nextId();
        nodo(id, "Bloque");
        for (NodoAST inst : ctx.instrucciones) {
            String h = inst.accept(this);
            if (h != null) edge(id, h);
        }
        return id;
    }

    @Override
    public String visit(DeclaracionVar.Context ctx) {
        String id = nextId();
        String label = "Var: " + ctx.id;
        if (ctx.tipo != null) label += " : " + ctx.tipo;
        nodo(id, label);
        if (ctx.expr != null) {
            String h = visitar(ctx.expr);
            enlazar(id, h, "=");
        }
        return id;
    }

    @Override
    public String visit(Asignacion.Context ctx) {
        String id = nextId();
        nodo(id, "= " + ctx.id);
        if (ctx.expr != null) {
            String h = visitar(ctx.expr);
            enlazar(id, h, null);
        }
        return id;
    }

    @Override
    public String visit(AsignacionOp.Context ctx) {
        String id = nextId();
        nodo(id, ctx.id + " " + ctx.operador + "=");
        if (ctx.expr != null) {
            String h = visitar(ctx.expr);
            enlazar(id, h, null);
        }
        return id;
    }

    @Override
    public String visit(If.Context ctx) {
        String id = nextId();
        nodo(id, "If");
        String hCond = visitar(ctx.condicion);
        enlazar(id, hCond, "cond");
        String hIf = visitar(ctx.bloqueIf);
        enlazar(id, hIf, "then");
        if (ctx.bloqueElse != null) {
            String hElse = visitar(ctx.bloqueElse);
            enlazar(id, hElse, "else");
        }
        return id;
    }

    @Override
    public String visit(For.Context ctx) {
        String id = nextId();
        nodo(id, "For");
        if (ctx.init != null) {
            String h = visitar(ctx.init);
            enlazar(id, h, "init");
        }
        if (ctx.condicion != null) {
            String h = visitar(ctx.condicion);
            enlazar(id, h, "cond");
        }
        if (ctx.update != null) {
            String h = visitar(ctx.update);
            enlazar(id, h, "update");
        }
        String hBody = visitar(ctx.bloque);
        enlazar(id, hBody, "body");
        return id;
    }

    @Override
    public String visit(Switch.Context ctx) {
        String id = nextId();
        nodo(id, "Switch");
        String hExp = visitar(ctx.expresion);
        enlazar(id, hExp, "expr");
        for (Switch.Caso caso : ctx.casos) {
            String idCaso = nextId();
            nodo(idCaso, "Caso");
            edge(id, idCaso);
            for (NodoAST exp : caso.expresiones) {
                String h = visitar(exp);
                enlazar(idCaso, h, "match");
            }
            String hBloque = visitar(caso.bloque);
            enlazar(idCaso, hBloque, null);
        }
        if (ctx.bloqueDefault != null) {
            String hDef = visitar(ctx.bloqueDefault);
            enlazar(id, hDef, "default");
        }
        return id;
    }

    @Override
    public String visit(Break.Context ctx) {
        String id = nextId();
        nodo(id, "Break");
        return id;
    }

    @Override
    public String visit(Continue.Context ctx) {
        String id = nextId();
        nodo(id, "Continue");
        return id;
    }

    @Override
    public String visit(LlamadaFuncion.Context ctx) {
        String id = nextId();
        String label = ctx.paquete != null && !ctx.paquete.isEmpty()
                ? ctx.paquete + "." + ctx.funcion + "()"
                : ctx.funcion + "()";
        nodo(id, label);
        for (NodoAST arg : ctx.argumentos) {
            String h = visitar(arg);
            enlazar(id, h, null);
        }
        return id;
    }

    @Override
    public String visit(ExpBinaria.Context ctx) {
        String id = nextId();
        nodo(id, ctx.operador);
        String hIzq = visitar(ctx.izquierdo);
        enlazar(id, hIzq, null);
        String hDer = visitar(ctx.derecho);
        enlazar(id, hDer, null);
        return id;
    }

    @Override
    public String visit(ExpUnaria.Context ctx) {
        String id = nextId();
        nodo(id, ctx.operador);
        String h = visitar(ctx.expr);
        enlazar(id, h, null);
        return id;
    }

    @Override
    public String visit(Literal.Context ctx) {
        String id = nextId();
        String val = ctx.valor != null ? ctx.valor.toString() : "nil";
        nodo(id, val + " (" + ctx.tipo + ")");
        return id;
    }

    @Override
    public String visit(Identificador.Context ctx) {
        String id = nextId();
        nodo(id, ctx.nombre);
        return id;
    }

    @Override
    public String visit(SliceLiteral.Context ctx) {
        String id = nextId();
        nodo(id, "Slice[" + ctx.tipoElemento + "]");
        for (NodoAST elem : ctx.elementos) {
            String h = visitar(elem);
            enlazar(id, h, null);
        }
        return id;
    }

    @Override
    public String visit(Len.Context ctx) {
        String id = nextId();
        nodo(id, "len");
        String h = visitar(ctx.expr);
        enlazar(id, h, null);
        return id;
    }

    @Override
    public String visit(Append.Context ctx) {
        String id = nextId();
        nodo(id, "append");
        String hSlice = visitar(ctx.slice);
        enlazar(id, hSlice, "slice");
        for (NodoAST elem : ctx.elementos) {
            String h = visitar(elem);
            enlazar(id, h, null);
        }
        return id;
    }

    @Override
    public String visit(IndexAccess.Context ctx) {
        String id = nextId();
        nodo(id, "IndexAccess");
        String hBase = visitar(ctx.base);
        enlazar(id, hBase, "base");
        String hIdx = visitar(ctx.indice);
        enlazar(id, hIdx, "idx");
        return id;
    }

    @Override
    public String visit(AssignIndex.Context ctx) {
        String id = nextId();
        nodo(id, "AssignIndex " + ctx.id);
        String hIdx = visitar(ctx.indice);
        enlazar(id, hIdx, "idx");
        String hVal = visitar(ctx.valor);
        enlazar(id, hVal, "=");
        return id;
    }

    @Override
    public String visit(SliceLiteral2D.Context ctx) {
        String id = nextId();
        nodo(id, "Slice2D[" + ctx.tipo + "]");
        for (List<NodoAST> fila : ctx.filas) {
            for (NodoAST elem : fila) {
                String h = visitar(elem);
                enlazar(id, h, null);
            }
        }
        return id;
    }

    @Override
    public String visit(AssignIndex2D.Context ctx) {
        String id = nextId();
        nodo(id, "AssignIndex2D " + ctx.id);
        String hIdx1 = visitar(ctx.indice1);
        enlazar(id, hIdx1, "i");
        String hIdx2 = visitar(ctx.indice2);
        enlazar(id, hIdx2, "j");
        String hVal = visitar(ctx.valor);
        enlazar(id, hVal, "=");
        return id;
    }

    @Override
    public String visit(StructDef.Context ctx) {
        String id = nextId();
        nodo(id, "Struct " + ctx.nombre);
        for (StructDef.Campo campo : ctx.campos) {
            String idCampo = nextId();
            nodo(idCampo, campo.nombre + " : " + campo.tipo);
            edge(id, idCampo);
        }
        return id;
    }

    @Override
    public String visit(StructAccess.Context ctx) {
        String id = nextId();
        nodo(id, ctx.structId + "." + ctx.campo);
        return id;
    }

    @Override
    public String visit(StructAssign.Context ctx) {
        String id = nextId();
        nodo(id, ctx.structId + "." + ctx.campo + " =");
        String h = visitar(ctx.valor);
        enlazar(id, h, null);
        return id;
    }

    @Override
    public String visit(NewStruct.Context ctx) {
        String id = nextId();
        nodo(id, "new " + ctx.nombreTipo);
        for (NewStruct.CampoInit campo : ctx.campos) {
            String idCampo = nextId();
            nodo(idCampo, campo.nombre);
            edge(id, idCampo);
            String hVal = visitar(campo.valor);
            enlazar(idCampo, hVal, null);
        }
        return id;
    }

    @Override
    public String visit(StructMethodDef.Context ctx) {
        String id = nextId();
        nodo(id, "Method (" + ctx.receiverType + ") " + ctx.nombre);
        String idParams = nextId();
        nodo(idParams, "Params");
        edge(id, idParams);
        for (Parametro p : ctx.parametros) {
            String idP = nextId();
            nodo(idP, p.nombre + " : " + p.tipo);
            edge(idParams, idP);
        }
        String hBody = visitar(ctx.body);
        enlazar(id, hBody, "body");
        return id;
    }

    @Override
    public String visit(StructMethodCall.Context ctx) {
        String id = nextId();
        nodo(id, ctx.id + "." + ctx.nombre + "()");
        for (NodoAST arg : ctx.argumentos) {
            String h = visitar(arg);
            enlazar(id, h, null);
        }
        return id;
    }

    @Override
    public String visit(FuncDef.Context ctx) {
        String id = nextId();
        String ret = ctx.tipoRetorno != null ? " -> " + ctx.tipoRetorno : "";
        nodo(id, "Func " + ctx.nombre + ret);
        String idParams = nextId();
        nodo(idParams, "Params");
        edge(id, idParams);
        for (Parametro p : ctx.parametros) {
            String idP = nextId();
            nodo(idP, p.nombre + " : " + p.tipo);
            edge(idParams, idP);
        }
        String hBody = visitar(ctx.body);
        enlazar(id, hBody, "body");
        return id;
    }

    @Override
    public String visit(FuncCall.Context ctx) {
        String id = nextId();
        nodo(id, ctx.nombre + "()");
        for (NodoAST arg : ctx.argumentos) {
            String h = visitar(arg);
            enlazar(id, h, null);
        }
        return id;
    }

    @Override
    public String visit(ReturnStmt.Context ctx) {
        String id = nextId();
        nodo(id, "Return");
        if (ctx.valor != null) {
            String h = visitar(ctx.valor);
            enlazar(id, h, null);
        }
        return id;
    }
}
