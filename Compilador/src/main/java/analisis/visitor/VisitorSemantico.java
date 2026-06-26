package analisis.visitor;

import analisis.ast.*;
import analisis.ast.exp.ExpBinaria;
import analisis.ast.exp.ExpUnaria;
import analisis.ast.exp.Identificador;
import analisis.ast.exp.Literal;
import analisis.ast.exp.SliceLiteral;
import analisis.ast.exp.Len;
import analisis.ast.exp.Append;
import analisis.ast.exp.IndexAccess;
import analisis.ast.exp.SliceLiteral2D;
import analisis.ast.exp.StructAccess;
import analisis.ast.exp.NewStruct;
import analisis.ast.stm.AssignIndex;
import analisis.ast.stm.AssignIndex2D;
import analisis.ast.stm.Asignacion;
import analisis.ast.stm.AsignacionOp;
import analisis.ast.stm.Break;
import analisis.ast.stm.Continue;
import analisis.ast.stm.DeclaracionVar;
import analisis.ast.stm.For;
import analisis.ast.stm.If;
import analisis.ast.stm.LlamadaFuncion;
import analisis.ast.stm.StructAssign;
import analisis.ast.stm.StructDef;
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
    private String tipoStructResultado; // Guarda el nombre del struct cuando tipoResultado es STRUCT

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
        // Registrar structs primero (ambito global)
        for (StructDef sd : ctx.structDefs) {
            sd.accept(this);
        }
        for (NodoAST instr : ctx.instrucciones) {
            instr.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(Bloque.Context ctx) {
        tabla.nuevoAmbito();
        try {
            for (NodoAST instr : ctx.instrucciones) {
                instr.accept(this);
            }
        } finally {
            tabla.cerrarAmbito();
        }
        return null;
    }

    @Override
    public Void visit(DeclaracionVar.Context ctx) {
        if (tabla.existeEnAmbitoActual(ctx.id)) {
            error(ctx.linea, "La variable '" + ctx.id + "' ya fue declarada en este ambito");
            return null;
        }

        TipoDato tipoDeclarado = null;
        String structType = null;

        if (ctx.tipo != null) {
            String tipoStr = ctx.tipo.toUpperCase();
            if (tipoStr.startsWith("STRUCT_")) {
                tipoDeclarado = TipoDato.STRUCT;
                structType = ctx.tipo.substring(7);
            } else if (tipoStr.startsWith("SLICE_STRUCT_")) {
                tipoDeclarado = TipoDato.SLICE_STRUCT;
                structType = ctx.tipo.substring(13);
            } else {
                tipoDeclarado = TipoDato.valueOf(tipoStr);
            }
        }

        if (ctx.expr != null) {
            ctx.expr.accept(this);
            TipoDato tipoExpr = this.tipoResultado;

            if (tipoDeclarado == null) {
                tipoDeclarado = tipoExpr;
                if (tipoDeclarado == TipoDato.STRUCT) {
                    structType = this.tipoStructResultado;
                }
            } else if (!sonCompatibles(tipoDeclarado, tipoExpr, structType, this.tipoStructResultado)) {
                error(ctx.linea, "No se puede asignar un valor de tipo " + tipoExpr + " a variable de tipo " + tipoDeclarado);
                return null;
            }
        }

        String ambito = (tabla.getNivelAmbito() == 0) ? "global" : "local";
        Simbolo s = new Simbolo(ctx.id, tipoDeclarado, structType, ctx.linea, ctx.columna, ambito);
        tabla.insertar(s);
        return null;
    }

    @Override
    public Void visit(Asignacion.Context ctx) {
        Simbolo s = tabla.buscar(ctx.id);
        if (s == null) {
            error(ctx.linea, "La variable '" + ctx.id + "' no ha sido declarada");
            return null;
        }

        ctx.expr.accept(this);
        TipoDato tipoExpr = this.tipoResultado;

        if (!sonCompatibles(s.tipo, tipoExpr, s.structType, this.tipoStructResultado)) {
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
        if (ctx.condicion != null) {
            ctx.condicion.accept(this);
            if (this.tipoResultado != TipoDato.BOOL) {
                error(ctx.linea, "La condición del if debe ser de tipo bool, no " + this.tipoResultado);
            }
        }

        ctx.bloqueIf.accept(this);
        if (ctx.bloqueElse != null) {
            ctx.bloqueElse.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(For.Context ctx) {
        tabla.nuevoAmbito();
        boolean antes = dentroDeFor;
        dentroDeFor = true;

        try {
            if (ctx.init != null) {
                ctx.init.accept(this);
            }

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
        } finally {
            dentroDeFor = antes;
            tabla.cerrarAmbito();
        }

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
                // reflect.TypeOf devuelve un String
                tipoResultado = TipoDato.STRING;
                break;
            case "slices":
                if (ctx.funcion.equals("Index")) {
                    if (ctx.argumentos.size() == 2) {
                        ctx.argumentos.get(0).accept(this);
                        TipoDato tipoSlice = this.tipoResultado;
                        ctx.argumentos.get(1).accept(this);
                        TipoDato tipoValor = this.tipoResultado;

                        if (tipoSlice != null && !esTipoSlice(tipoSlice)) {
                            error(ctx.linea, "slices.Index: el primer argumento debe ser un slice");
                        } else if (tipoSlice != null && tipoValor != null) {
                            String elemType = tipoElementoDeSlice(tipoSlice);
                            TipoDato elemTipo = TipoDato.valueOf(elemType.toUpperCase());
                            if (!sonCompatibles(elemTipo, tipoValor)) {
                                error(ctx.linea, "slices.Index: el valor no es compatible con el tipo del slice (" + elemType + ")");
                            }
                        }
                    }
                    tipoResultado = TipoDato.INT;
                }
                break;
            case "strings":
                if (ctx.funcion.equals("Join")) {
                    if (ctx.argumentos.size() == 2) {
                        ctx.argumentos.get(0).accept(this);
                        TipoDato tipoSlice = this.tipoResultado;
                        ctx.argumentos.get(1).accept(this);
                        TipoDato tipoSep = this.tipoResultado;

                        if (tipoSlice != null && tipoSlice != TipoDato.SLICE_STRING) {
                            error(ctx.linea, "strings.Join: el primer argumento debe ser un slice de string");
                        }
                        if (tipoSep != null && tipoSep != TipoDato.STRING) {
                            error(ctx.linea, "strings.Join: el separador debe ser un string");
                        }
                    }
                    tipoResultado = TipoDato.STRING;
                }
                break;
        }
        return null;
    }

    private boolean esTipoSlice(TipoDato t) {
        return t.name().startsWith("SLICE_");
    }

    private String tipoElementoDeSlice(TipoDato t) {
        String name = t.name();
        if (name.startsWith("SLICE_")) {
            return name.substring(6);
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
    public Void visit(SliceLiteral.Context ctx) {
        String tipoElem = ctx.tipoElemento;
        String tipoElemUpper = tipoElem.toUpperCase();
        TipoDato tipoSlice;

        if (tipoElemUpper.startsWith("STRUCT_")) {
            tipoSlice = TipoDato.SLICE_STRUCT;
            this.tipoStructResultado = tipoElemUpper.substring(7);
        } else if ("STRUCT".equals(tipoElemUpper)) {
            tipoSlice = TipoDato.SLICE_STRUCT;
        } else if ("SLICE_STRUCT".equals(tipoElemUpper)) {
            tipoSlice = TipoDato.SLICE_STRUCT;
        } else {
            tipoSlice = TipoDato.valueOf("SLICE_" + tipoElemUpper);
        }

        for (NodoAST elem : ctx.elementos) {
            elem.accept(this);
            if (this.tipoResultado == null) continue;
            TipoDato tipoEsperado;
            String structEsperado = null;
            if (tipoElemUpper.startsWith("STRUCT_")) {
                tipoEsperado = TipoDato.STRUCT;
                structEsperado = tipoElemUpper.substring(7);
            } else {
                tipoEsperado = TipoDato.valueOf(tipoElemUpper);
            }
            if (!sonCompatibles(tipoEsperado, this.tipoResultado, structEsperado, this.tipoStructResultado)) {
                error(ctx.linea, "Elemento de tipo " + this.tipoResultado + " no compatible con slice de " + tipoElem);
            }
        }

        tipoResultado = tipoSlice;
        return null;
    }

    @Override
    public Void visit(SliceLiteral2D.Context ctx) {
        String tipoBase = ctx.tipo;
        String tipoBaseUpper = tipoBase.toUpperCase();

        if (tipoBaseUpper.startsWith("STRUCT_")) {
            error(ctx.linea, "Matrices 2D de structs no soportadas");
            tipoResultado = null;
            return null;
        }

        TipoDato tipoElem = TipoDato.valueOf(tipoBaseUpper);

        for (List<NodoAST> fila : ctx.filas) {
            for (NodoAST elem : fila) {
                elem.accept(this);
                if (this.tipoResultado != null && !sonCompatibles(tipoElem, this.tipoResultado)) {
                    error(ctx.linea, "Elemento de tipo " + this.tipoResultado + " no compatible con matriz de " + tipoBase);
                }
            }
        }

        tipoResultado = TipoDato.valueOf("SLICE_SLICE_" + tipoBaseUpper);
        return null;
    }

    @Override
    public Void visit(Len.Context ctx) {
        ctx.expr.accept(this);
        if (this.tipoResultado != null && !esTipoSlice(this.tipoResultado)) {
            error(ctx.linea, "len solo es válido para slices");
            tipoResultado = null;
        } else if (this.tipoResultado != null) {
            tipoResultado = TipoDato.INT;
        }
        return null;
    }

    @Override
    public Void visit(Append.Context ctx) {
        ctx.slice.accept(this);
        TipoDato tipoSlice = this.tipoResultado;

        if (tipoSlice != null) {
            if (!esTipoSlice(tipoSlice)) {
                error(ctx.linea, "append: el primer argumento debe ser un slice");
                tipoResultado = null;
                return null;
            }
            String elemType = tipoElementoDeSlice(tipoSlice);
            TipoDato elemTipo = TipoDato.valueOf(elemType.toUpperCase());

            for (NodoAST elem : ctx.elementos) {
                elem.accept(this);
                if (this.tipoResultado == null) continue;
                if (!sonCompatibles(elemTipo, this.tipoResultado)) {
                    error(ctx.linea, "append: elemento de tipo " + this.tipoResultado + " no compatible con slice de " + elemType);
                }
            }
        }

        tipoResultado = tipoSlice;
        return null;
    }

    @Override
    public Void visit(IndexAccess.Context ctx) {
        ctx.base.accept(this);
        TipoDato tipoBase = this.tipoResultado;
        String tipoStructBase = this.tipoStructResultado;

        ctx.indice.accept(this);
        TipoDato tipoIndice = this.tipoResultado;

        if (tipoBase == null || tipoIndice == null) {
            return null;
        }

        if (!esTipoSlice(tipoBase)) {
            error(ctx.linea, "El acceso por indice solo es valido para slices");
            tipoResultado = null;
            return null;
        }

        if (tipoIndice != TipoDato.INT) {
            error(ctx.linea, "El indice debe ser de tipo int");
            tipoResultado = null;
            return null;
        }

        String nombreElem = tipoElementoDeSlice(tipoBase);
        String nombreElemUpper = nombreElem.toUpperCase();
        if (nombreElemUpper.startsWith("STRUCT_")) {
            tipoResultado = TipoDato.STRUCT;
            this.tipoStructResultado = nombreElemUpper.substring(7);
        } else if ("STRUCT".equals(nombreElemUpper)) {
            tipoResultado = TipoDato.STRUCT;
            this.tipoStructResultado = tipoStructBase;
        } else if ("SLICE_STRUCT".equals(nombreElemUpper)) {
            tipoResultado = TipoDato.SLICE_STRUCT;
            this.tipoStructResultado = tipoStructBase;
        } else {
            tipoResultado = TipoDato.valueOf(nombreElemUpper);
        }
        return null;
    }

    @Override
    public Void visit(AssignIndex.Context ctx) {
        Simbolo s = tabla.buscar(ctx.id);
        if (s == null) {
            error(ctx.linea, "Variable '" + ctx.id + "' no declarada");
            tipoResultado = null;
            return null;
        }
        TipoDato tipoBase = s.tipo;

        ctx.indice.accept(this);
        TipoDato tipoIndice = this.tipoResultado;

        ctx.valor.accept(this);
        TipoDato tipoValor = this.tipoResultado;

        if (tipoBase == null || tipoIndice == null || tipoValor == null) {
            return null;
        }

        if (!esTipoSlice(tipoBase)) {
            error(ctx.linea, "La asignacion por indice solo es valida para slices");
            return null;
        }

        if (tipoIndice != TipoDato.INT) {
            error(ctx.linea, "El indice debe ser de tipo int");
            return null;
        }

        String nombreElem = tipoElementoDeSlice(tipoBase);
        String nombreElemUpper = nombreElem.toUpperCase();
        TipoDato tipoElem;
        String structTypeElem = null;
        if (nombreElemUpper.startsWith("STRUCT_")) {
            tipoElem = TipoDato.STRUCT;
            structTypeElem = nombreElemUpper.substring(7);
        } else if ("STRUCT".equals(nombreElemUpper)) {
            tipoElem = TipoDato.STRUCT;
            structTypeElem = s.structType;
        } else {
            tipoElem = TipoDato.valueOf(nombreElemUpper);
        }
        if (!sonCompatibles(tipoElem, tipoValor, structTypeElem, this.tipoStructResultado)) {
            error(ctx.linea, "No se puede asignar un valor de tipo " + tipoValor + " a un elemento de tipo " + nombreElem);
        }

        return null;
    }

    @Override
    public Void visit(AssignIndex2D.Context ctx) {
        Simbolo s = tabla.buscar(ctx.id);
        if (s == null) {
            error(ctx.linea, "Variable '" + ctx.id + "' no declarada");
            tipoResultado = null;
            return null;
        }
        TipoDato tipoBase = s.tipo;

        if (tipoBase == null || !esTipoSlice(tipoBase) || !tipoBase.name().startsWith("SLICE_SLICE_")) {
            error(ctx.linea, "Acceso multidimensional solo valido para matrices ([][]T)");
            return null;
        }

        ctx.indice1.accept(this);
        if (this.tipoResultado != TipoDato.INT) {
            error(ctx.linea, "El indice debe ser de tipo int");
            return null;
        }

        ctx.indice2.accept(this);
        if (this.tipoResultado != TipoDato.INT) {
            error(ctx.linea, "El indice debe ser de tipo int");
            return null;
        }

        ctx.valor.accept(this);
        TipoDato tipoValor = this.tipoResultado;

        String innerTypeName = tipoElementoDeSlice(tipoBase);
        TipoDato innerTipo = TipoDato.valueOf(innerTypeName);
        String elemTypeName = tipoElementoDeSlice(innerTipo);
        TipoDato elemTipo = TipoDato.valueOf(elemTypeName);

        if (!sonCompatibles(elemTipo, tipoValor)) {
            error(ctx.linea, "No se puede asignar un valor de tipo " + tipoValor + " a un elemento de tipo " + elemTypeName.toLowerCase());
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
    
    @Override
    public Void visit(StructDef.Context ctx) {
        // Validar que el struct solo se declare en ambito global
        if (tabla.getNivelAmbito() > 0) {
            error(ctx.linea, "Los structs solo pueden declararse en ambito global");
            return null;
        }
        // Validar que no exista otro struct con el mismo nombre
        if (tabla.existeStruct(ctx.nombre)) {
            error(ctx.linea, "El struct '" + ctx.nombre + "' ya fue declarado");
            return null;
        }
        // Validar campos
        for (StructDef.Campo campo : ctx.campos) {
            String tipoStr = campo.tipo.toUpperCase();
            if (tipoStr.startsWith("STRUCT_") || tipoStr.startsWith("SLICE_STRUCT_")) {
                String structName = tipoStr.startsWith("STRUCT_") ? campo.tipo.substring(7) : "";
                if (structName.isEmpty() && tipoStr.startsWith("SLICE_STRUCT_")) {
                    structName = campo.tipo.substring(13);
                }
                if (!structName.isEmpty() && !tabla.existeStruct(structName)) {
                    error(ctx.linea, "El struct '" + structName + "' no ha sido declarado");
                }
            } else if (!tipoStr.startsWith("SLICE_")) {
                try {
                    TipoDato.valueOf(tipoStr);
                } catch (IllegalArgumentException e) {
                    error(ctx.linea, "Tipo '" + campo.tipo + "' no valido para el campo '" + campo.nombre + "'");
                }
            }
        }
        tabla.definirStruct(ctx.nombre, ctx.campos);
        return null;
    }

    @Override
    public Void visit(StructAccess.Context ctx) {
        Simbolo s = tabla.buscar(ctx.structId);
        if (s == null) {
            error(ctx.linea, "Variable '" + ctx.structId + "' no declarada");
            tipoResultado = null;
            return null;
        }
        if (s.tipo != TipoDato.STRUCT) {
            error(ctx.linea, "La variable '" + ctx.structId + "' no es un struct");
            tipoResultado = null;
            return null;
        }
        List<StructDef.Campo> campos = tabla.buscarStruct(s.structType);
        if (campos == null) {
            error(ctx.linea, "El tipo struct '" + s.structType + "' no esta definido");
            tipoResultado = null;
            return null;
        }
        boolean encontrado = false;
        for (StructDef.Campo c : campos) {
            if (c.nombre.equals(ctx.campo)) {
                String tipoStr = c.tipo.toUpperCase();
                if (tipoStr.startsWith("STRUCT_")) {
                    tipoResultado = TipoDato.STRUCT;
                } else if (tipoStr.startsWith("SLICE_STRUCT_")) {
                    tipoResultado = TipoDato.SLICE_STRUCT;
                } else if (tipoStr.startsWith("SLICE_")) {
                    try {
                        tipoResultado = TipoDato.valueOf(tipoStr);
                    } catch (IllegalArgumentException e) {
                        tipoResultado = null;
                    }
                } else {
                    try {
                        tipoResultado = TipoDato.valueOf(tipoStr);
                    } catch (IllegalArgumentException e) {
                        tipoResultado = null;
                    }
                }
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            error(ctx.linea, "El struct '" + s.structType + "' no tiene un campo llamado '" + ctx.campo + "'");
            tipoResultado = null;
        }
        return null;
    }

    @Override
    public Void visit(StructAssign.Context ctx) {
        Simbolo s = tabla.buscar(ctx.structId);
        if (s == null) {
            error(ctx.linea, "Variable '" + ctx.structId + "' no declarada");
            return null;
        }
        if (s.tipo != TipoDato.STRUCT) {
            error(ctx.linea, "La variable '" + ctx.structId + "' no es un struct");
            return null;
        }
        List<StructDef.Campo> campos = tabla.buscarStruct(s.structType);
        if (campos == null) {
            error(ctx.linea, "El tipo struct '" + s.structType + "' no esta definido");
            return null;
        }
        boolean encontrado = false;
        String tipoCampoStr = null;
        for (StructDef.Campo c : campos) {
            if (c.nombre.equals(ctx.campo)) {
                tipoCampoStr = c.tipo.toUpperCase();
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            error(ctx.linea, "El struct '" + s.structType + "' no tiene un campo llamado '" + ctx.campo + "'");
            return null;
        }
        ctx.valor.accept(this);
        TipoDato tipoValor = this.tipoResultado;
        if (tipoValor != null) {
            if (tipoCampoStr.startsWith("STRUCT_") || tipoCampoStr.startsWith("SLICE_STRUCT_")) {
                if (tipoValor != TipoDato.STRUCT && tipoValor != TipoDato.SLICE_STRUCT) {
                    error(ctx.linea, "No se puede asignar " + tipoValor + " al campo '" + ctx.campo + "' de tipo struct");
                }
            } else {
                try {
                    TipoDato tipoCampo = TipoDato.valueOf(tipoCampoStr);
                    if (!sonCompatibles(tipoCampo, tipoValor)) {
                        error(ctx.linea, "No se puede asignar " + tipoValor + " al campo '" + ctx.campo + "' de tipo " + tipoCampoStr.toLowerCase());
                    }
                } catch (IllegalArgumentException e) {
                    error(ctx.linea, "Tipo de campo no reconocido: " + tipoCampoStr);
                }
            }
        }
        return null;
    }

    @Override
    public Void visit(NewStruct.Context ctx) {
        // Verificar que el struct existe
        String nombreTipo = ctx.nombreTipo.toUpperCase();
        if (!tabla.existeStruct(nombreTipo)) {
            error(ctx.linea, "El struct '" + nombreTipo + "' no ha sido declarado");
            tipoResultado = null;
            return null;
        }
        List<StructDef.Campo> definicion = tabla.buscarStruct(nombreTipo);
        // Verificar que todos los campos requeridos estan presentes y los tipos coinciden
        for (StructDef.Campo campo : definicion) {
            boolean encontrado = false;
            for (NewStruct.CampoInit init : ctx.campos) {
                if (init.nombre.equals(campo.nombre)) {
                    init.valor.accept(this);
                    // Validar tipo
                    String tipoEsperadoStr = campo.tipo.toUpperCase();
                    if (tipoEsperadoStr.startsWith("STRUCT_") || tipoEsperadoStr.startsWith("SLICE_STRUCT_")) {
                        if (this.tipoResultado != TipoDato.STRUCT && this.tipoResultado != TipoDato.SLICE_STRUCT) {
                            error(ctx.linea, "El campo '" + campo.nombre + "' espera un struct");
                        }
                    } else {
                        try {
                            TipoDato tipoEsperado = TipoDato.valueOf(tipoEsperadoStr);
                            if (!sonCompatibles(tipoEsperado, this.tipoResultado)) {
                                error(ctx.linea, "El campo '" + campo.nombre + "' espera " + tipoEsperadoStr.toLowerCase() + " pero se recibio " + this.tipoResultado);
                            }
                        } catch (IllegalArgumentException e) {
                            error(ctx.linea, "Tipo no reconocido para campo '" + campo.nombre + "'");
                        }
                    }
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) {
                error(ctx.linea, "Falta el campo '" + campo.nombre + "' en la inicializacion del struct '" + nombreTipo + "'");
            }
        }
        // Verificar que no haya campos extra
        for (NewStruct.CampoInit init : ctx.campos) {
            boolean existe = false;
            for (StructDef.Campo campo : definicion) {
                if (init.nombre.equals(campo.nombre)) {
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                error(ctx.linea, "El struct '" + nombreTipo + "' no tiene un campo llamado '" + init.nombre + "'");
            }
        }
        tipoResultado = TipoDato.STRUCT;
        this.tipoStructResultado = nombreTipo;
        return null;
    }

    private boolean sonCompatibles(TipoDato declarado, TipoDato expr) {
        return sonCompatibles(declarado, expr, null, null);
    }

    private boolean sonCompatibles(TipoDato declarado, TipoDato expr, String structDecl, String structExpr) {
        if (declarado == TipoDato.STRUCT || declarado == TipoDato.SLICE_STRUCT) {
            if (declarado != expr) return false;
            // Mismo struct type
            if (structDecl == null || structExpr == null) return false;
            return structDecl.equals(structExpr);
        }
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
