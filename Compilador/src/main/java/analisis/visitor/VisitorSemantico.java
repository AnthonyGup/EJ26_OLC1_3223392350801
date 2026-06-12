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
import analisis.ast.stm.Switch;
import analisis.semantic.Simbolo;
import analisis.semantic.TablaSimbolos;
import analisis.semantic.TipoDato;
import java.util.ArrayList;
import java.util.List;

public class VisitorSemantico implements Visitor<Void> {

    private TablaSimbolos tabla;
    private List<String> errores;
    private boolean dentroDeFor;
    private boolean dentroDeSwitch;
    private TipoDato tipoResultado; // Clave: guarda el tipo de la ultima expresion

    public VisitorSemantico() {
        this.tabla = new TablaSimbolos();
        this.errores = new ArrayList<>();
        this.dentroDeFor = false;
        this.dentroDeSwitch = false;
    }

    public List<String> getErrores() {
        return errores;
    }

    public TablaSimbolos getTabla() {
        return tabla;
    }

    private void error(int linea, String msg) {
        errores.add("Linea " + linea + ": " + msg);
    }

    private boolean esNumerico(TipoDato t) {
        return t == TipoDato.INT || t == TipoDato.FLOAT64;
    }

    // INT + FLOAT64 a FLOAT64...
    private TipoDato promover(TipoDato a, TipoDato b) {
        if (a == TipoDato.FLOAT64 || b == TipoDato.FLOAT64) {
            return TipoDato.FLOAT64;
        }
        return TipoDato.INT;
    }

    @Override
    public Void visit(Programa.Context ctx) {
        for (NodoAST instr : ctx.instrucciones) {
            instr.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(Bloque.Context ctx) {
        tabla.nuevoAmbito();
        for (NodoAST instr : ctx.instrucciones) {
            instr.accept(this);
        }
        tabla.cerrarAmbito();
        return null;
    }

    @Override
    public Void visit(DeclaracionVar.Context ctx) {
        // Verificar que no se redeclare en el mismo ambito
        if (tabla.existeEnAmbitoActual(ctx.id)) {
            error(ctx.linea, "La variable '" + ctx.id + "' ya fue declarada  en este ambito");
            return null;
        }

        TipoDato tipoDeclarado = null;
        Object valorInicial = null;

        // Determinar  el tipo  
        if (ctx.tipo != null) {
            tipoDeclarado = TipoDato.valueOf(ctx.tipo.toUpperCase());
            valorInicial = tipoDeclarado.getValorDefecto();
        }
        //Siel ctx.tipo es null, es := a el tipo se ifiere   de la expreesion

        //  Validar einferir tipo dee  laexpresion
        if (ctx.expr != null) {
            ctx.expr.accept(this);
            TipoDato tipoExpr = this.tipoResultado;

            if (tipoDeclarado == null) {
                tipoDeclarado = tipoExpr;
                valorInicial = null; // se  asigna  despues  en ejecucion
            } else {
                // Valida compatibilidad entre   tipo de dato  y  expresion
                if (!sonCompatibles(tipoDeclarado, tipoExpr)) {
                    error(ctx.linea, "No se puede asignar un valor de tipo " + tipoExpr + " a variable de tipo " + tipoDeclarado);
                    return null;
                }
            }
        }

        String ambito = (tabla.getNivelAmbito() == 0) ? "global" : "local";
        Simbolo s = new Simbolo(ctx.id, tipoDeclarado, valorInicial, ctx.linea, ctx.columna, ambito);

        tabla.insertar(s);

        return null;
    }

    @Override
    public Void visit(Asignacion.Context ctx) {
        // Verifica que la variable exista
        Simbolo s = tabla.buscar(ctx.id);
        if (s == null) {
            error(ctx.linea, "La variable '" + ctx.id + "' no ha sido declarada");
            return null;
        }

        // Validar tipo de la expresión
        ctx.expr.accept(this);
        TipoDato tipoExpr = this.tipoResultado;

        if (!sonCompatibles(s.tipo, tipoExpr)) {
            error(ctx.linea, "No se puede asignar " + tipoExpr + " a variable de tipo " + s.tipo);
        }

        return null;
    }

    @Override
    public Void visit(AsignacionOp.Context ctx) {
        Simbolo s = tabla.buscar(ctx.id);
        if (s == null) {
            error(ctx.linea, "La variable '" + ctx.id + "' no ha sido declarada");
            return null;
        }

        // += y -= solo para numéricos y string (solo += para string)
        ctx.expr.accept(this);
        TipoDato tipoExpr = this.tipoResultado;

        if (ctx.operador.equals("+=") && s.tipo == TipoDato.STRING && tipoExpr == TipoDato.STRING) {
            return null; // string concatenation OK
        }

        if (!esNumerico(s.tipo) || !esNumerico(tipoExpr)) {
            error(ctx.linea, "Operador '" + ctx.operador + "' no válido para tipos " + s.tipo + " y " + tipoExpr);
        }

        return null;

    }

    @Override
    public Void visit(If.Context ctx) {
        // Validar que la condición sea bool
        ctx.condicion.accept(this);
        if (this.tipoResultado != TipoDato.BOOL) {
            error(ctx.linea, "La condición del if debe ser de tipo bool, no " + this.tipoResultado);
        }

        // Visitar bloques
        ctx.bloqueIf.accept(this);
        if (ctx.bloqueElse != null) {
            ctx.bloqueElse.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(For.Context ctx) {
        boolean antes = dentroDeFor;
        dentroDeFor = true;

        // for init; cond; update
        if (ctx.init != null) {
            ctx.init.accept(this);
        }

        // Validar condición (si existe)
        if (ctx.condicion != null) {
            ctx.condicion.accept(this);
            if (this.tipoResultado != TipoDato.BOOL) {
                error(ctx.linea, "La condición del for debe ser de tipo bool");
            }
        }

        if (ctx.update != null) {
            ctx.update.accept(this);
        }

        ctx.bloque.accept(this);
        dentroDeFor = antes;

        return null;
    }

    @Override
    public Void visit(Switch.Context ctx) {
        ctx.expresion.accept(this);
        TipoDato tipoSwitch = this.tipoResultado;

        boolean antesSwitch = dentroDeSwitch;
        dentroDeSwitch = true;
        try {
            for (Switch.Caso caso : ctx.casos) {
                for (NodoAST expresionCaso : caso.expresiones) {
                    expresionCaso.accept(this);
                    if (tipoSwitch != null && this.tipoResultado != null && !sonCompatibles(tipoSwitch, this.tipoResultado)) {
                        error(caso.linea, "La expresion del case debe ser compatible con el tipo del switch: " + tipoSwitch + " vs " + this.tipoResultado);
                    }
                }
                caso.bloque.accept(this);
            }

            if (ctx.bloqueDefault != null) {
                ctx.bloqueDefault.accept(this);
            }
        } finally {
            dentroDeSwitch = antesSwitch;
        }

        return null;
    }

    @Override
    public Void visit(Break.Context ctx) {
        if (!dentroDeFor && !dentroDeSwitch) {
            error(ctx.linea, "Sentencia break fuera de un ciclo for o switch");
        }
        return null;
    }

    @Override
    public Void visit(Continue.Context ctx) {
        if (!dentroDeFor) {
            error(ctx.linea, "Sentencia continue fuera de un ciclo for");
        }
        return null;
    }

    @Override
    public Void visit(LlamadaFuncion.Context ctx) {
        switch (ctx.paquete) {
            case "fmt":
                if (ctx.funcion.equals("Println")) {
                    for (NodoAST arg : ctx.argumentos) {
                        arg.accept(this);
                    }
                    tipoResultado = TipoDato.NIL;
                }
                break;
            case "strconv":
                // strconv.Atoi y ParseFloat reciben un string, devuelven INT o FLOAT64
                if (ctx.argumentos.size() > 0) {
                    ctx.argumentos.get(0).accept(this);
                    if (this.tipoResultado != TipoDato.STRING) {
                        error(ctx.linea, ctx.funcion + " espera un string como argumento");
                    }
                }
                tipoResultado = ctx.funcion.equals("Atoi") ? TipoDato.INT : TipoDato.FLOAT64;
                break;
            case "reflect":
                if (ctx.argumentos.size() > 0) {
                    ctx.argumentos.get(0).accept(this);
                }
                // reflect.TypeOf().String() devuelve un String
                tipoResultado = TipoDato.STRING;
                break;
        }
        return null;
    }

    @Override
    public Void visit(ExpBinaria.Context ctx) {
        ctx.izquierdo.accept(this);
        TipoDato izq = this.tipoResultado;
        ctx.derecho.accept(this);
        TipoDato der = this.tipoResultado;

        if (izq == null || der == null) {
            tipoResultado = null;
            return null;
        }

        // nil check
        if (izq == TipoDato.NIL || der == TipoDato.NIL) {
            error(ctx.linea, "No se puede realizar la operación con nil");
            tipoResultado = null;
            return null;
        }

        switch (ctx.operador) {
            case "+":
                if (izq == TipoDato.STRING && der == TipoDato.STRING) {
                    tipoResultado = TipoDato.STRING; // concatenación
                } else if (esNumerico(izq) && esNumerico(der)) {
                    tipoResultado = promover(izq, der);
                } else {
                    error(ctx.linea, "Operador + no válido entre " + izq + " y " + der);
                    tipoResultado = null;
                }
                break;
            case "-":
            case "*":
            case "/":
                if (esNumerico(izq) && esNumerico(der)) {
                    tipoResultado = promover(izq, der);
                } else {
                    error(ctx.linea, "Operador " + ctx.operador + " no válido entre " + izq + " y " + der);
                    tipoResultado = null;
                }
                break;
            case "%":
                if (izq == TipoDato.INT && der == TipoDato.INT) {
                    tipoResultado = TipoDato.INT;
                } else {
                    error(ctx.linea, "Módulo solo válido para int");
                    tipoResultado = null;
                }
                break;
            case "==":
            case "!=":
                if (izq == der || (esNumerico(izq) && esNumerico(der))) {
                    tipoResultado = TipoDato.BOOL;
                } else {
                    error(ctx.linea, "Comparación " + ctx.operador + " no válida entre " + izq + " y " + der);
                    tipoResultado = null;
                }
                break;
            case ">":
            case ">=":
            case "<":
            case "<=":
                if (esNumerico(izq) && esNumerico(der)) {
                    tipoResultado = TipoDato.BOOL;
                } else {
                    error(ctx.linea, "Comparación " + ctx.operador + " no válida entre " + izq + " y " + der);
                    tipoResultado = null;
                }
                break;
            case "&&":
            case "||":
                if (izq == TipoDato.BOOL && der == TipoDato.BOOL) {
                    tipoResultado = TipoDato.BOOL;
                } else {
                    error(ctx.linea, "Operador lógico solo válido para bool");
                    tipoResultado = null;
                }
                break;
        }

        return null;
    }

    @Override
    public Void visit(ExpUnaria.Context ctx) {
        ctx.expr.accept(this);

        if (ctx.operador.equals("-")) {
            if (!esNumerico(this.tipoResultado)) {
                error(ctx.linea, "Negación unaria solo válida para tipos numéricos");
                tipoResultado = null;
            }
        } else if (ctx.operador.equals("!")) {
            if (this.tipoResultado != TipoDato.BOOL) {
                error(ctx.linea, "Operador ! solo válido para bool");
                tipoResultado = null;
            }
        }

        return null;

    }

    @Override
    public Void visit(Literal.Context ctx) {
        switch (ctx.tipo) {
            case "int":
                tipoResultado = TipoDato.INT;
                break;
            case "float64":
                tipoResultado = TipoDato.FLOAT64;
                break;
            case "string":
                tipoResultado = TipoDato.STRING;
                break;
            case "bool":
                tipoResultado = TipoDato.BOOL;
                break;
            case "rune":
                tipoResultado = TipoDato.RUNE;
                break;
            case "nil":
                tipoResultado = TipoDato.NIL;
                break;
        }
        return null;
    }

    @Override
    public Void visit(Identificador.Context ctx) {
        Simbolo s = tabla.buscar(ctx.nombre);
        if (s == null) {
            error(ctx.linea, "Variable '" + ctx.nombre + "' no declarada");
            tipoResultado = null;
        } else {
            tipoResultado = s.tipo;
        }
        return null;
    }
    
    private boolean sonCompatibles(TipoDato declarado, TipoDato expr) {
        if (declarado == expr) {
            return true;
        }
        // int -> float64 implicit conversion
        if (declarado == TipoDato.FLOAT64 && expr == TipoDato.INT) {
            return true;
        }
        return false;
    }

}
