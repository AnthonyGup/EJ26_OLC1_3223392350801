package analisis.visitor;

import analisis.ast.*;
import analisis.ast.exp.*;
import analisis.ast.stm.*;
import analisis.semantic.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VisitorSemantico implements Visitor<Void> {

    private TablaSimbolos tabla;
    private List<String> errores;
    private ValidacionSemantica validador;
    private boolean dentroDeFor;
    private boolean dentroDeSwitch;
    private TipoDato tipoResultado;
    private String tipoStructResultado;

    public VisitorSemantico() {
        this.tabla = new TablaSimbolos();
        this.errores = new ArrayList<>();
        this.validador = new ValidacionSemantica(tabla, errores);
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

    @Override
    public Void visit(Programa.Context ctx) {
        for (StructDef sd : ctx.structDefs) {
            sd.accept(this);
        }
        for (StructMethodDef md : ctx.structMethods) {
            md.accept(this);
        }
        for (FuncDef fd : ctx.funcDefs) {
            fd.accept(this);
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
            TipoUtils.TipoParseResult pr = TipoUtils.parseTipo(ctx.tipo);
            tipoDeclarado = pr.tipo;
            structType = pr.structType;
        }

        if (ctx.expr != null) {
            ctx.expr.accept(this);
            TipoDato tipoExpr = this.tipoResultado;

            if (tipoDeclarado == null) {
                tipoDeclarado = tipoExpr;
                if (tipoDeclarado == TipoDato.STRUCT) {
                    structType = this.tipoStructResultado;
                }
            } else if (!TipoUtils.sonCompatibles(tipoDeclarado, tipoExpr, structType, this.tipoStructResultado)) {
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
        Simbolo s = validador.checkVariableExists(ctx.id, ctx.linea);
        if (s == null) return null;

        ctx.expr.accept(this);
        TipoDato tipoExpr = this.tipoResultado;

        if (!TipoUtils.sonCompatibles(s.tipo, tipoExpr, s.structType, this.tipoStructResultado)) {
            error(ctx.linea, "No se puede asignar " + tipoExpr + " a variable de tipo " + s.tipo);
        }

        return null;
    }

    @Override
    public Void visit(AsignacionOp.Context ctx) {
        Simbolo s = validador.checkVariableExists(ctx.id, ctx.linea);
        if (s == null) return null;

        ctx.expr.accept(this);
        TipoDato tipoExpr = this.tipoResultado;

        if (ctx.operador.equals("+=") && s.tipo == TipoDato.STRING && tipoExpr == TipoDato.STRING) {
            return null;
        }

        if (!TipoUtils.esNumerico(s.tipo) || !TipoUtils.esNumerico(tipoExpr)) {
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
                    if (tipoSwitch != null && this.tipoResultado != null && !TipoUtils.sonCompatibles(tipoSwitch, this.tipoResultado)) {
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
                tipoResultado = TipoDato.STRING;
                break;
            case "slices":
                if (ctx.funcion.equals("Index")) {
                    if (ctx.argumentos.size() == 2) {
                        ctx.argumentos.get(0).accept(this);
                        TipoDato tipoSlice = this.tipoResultado;
                        ctx.argumentos.get(1).accept(this);
                        TipoDato tipoValor = this.tipoResultado;

                        if (tipoSlice != null && !TipoUtils.esTipoSlice(tipoSlice)) {
                            error(ctx.linea, "slices.Index: el primer argumento debe ser un slice");
                        } else if (tipoSlice != null && tipoValor != null) {
                            String elemType = TipoUtils.tipoElementoDeSlice(tipoSlice);
                            TipoDato elemTipo = TipoDato.valueOf(elemType.toUpperCase());
                            if (!TipoUtils.sonCompatibles(elemTipo, tipoValor)) {
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

        if (izq == TipoDato.NIL || der == TipoDato.NIL) {
            error(ctx.linea, "No se puede realizar la operación con nil");
            tipoResultado = null;
            return null;
        }

        switch (ctx.operador) {
            case "+":
                if (izq == TipoDato.STRING && der == TipoDato.STRING) {
                    tipoResultado = TipoDato.STRING;
                } else if (TipoUtils.esNumerico(izq) && TipoUtils.esNumerico(der)) {
                    tipoResultado = TipoUtils.promover(izq, der);
                } else {
                    error(ctx.linea, "Operador + no válido entre " + izq + " y " + der);
                    tipoResultado = null;
                }
                break;
            case "-":
            case "*":
            case "/":
                if (TipoUtils.esNumerico(izq) && TipoUtils.esNumerico(der)) {
                    tipoResultado = TipoUtils.promover(izq, der);
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
                if (izq == der || (TipoUtils.esNumerico(izq) && TipoUtils.esNumerico(der))) {
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
                if (TipoUtils.esNumerico(izq) && TipoUtils.esNumerico(der)) {
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
            if (!TipoUtils.esNumerico(this.tipoResultado)) {
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
            TipoUtils.TipoParseResult pr = TipoUtils.parseTipo(tipoElem);
            TipoDato tipoEsperado = pr.tipo;
            String structEsperado = pr.structType;
            if (!TipoUtils.sonCompatibles(tipoEsperado, this.tipoResultado, structEsperado, this.tipoStructResultado)) {
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
                if (this.tipoResultado != null && !TipoUtils.sonCompatibles(tipoElem, this.tipoResultado)) {
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
        if (this.tipoResultado != null && !TipoUtils.esTipoSlice(this.tipoResultado)) {
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
            if (!TipoUtils.esTipoSlice(tipoSlice)) {
                error(ctx.linea, "append: el primer argumento debe ser un slice");
                tipoResultado = null;
                return null;
            }
            String elemType = TipoUtils.tipoElementoDeSlice(tipoSlice);
            TipoDato elemTipo = TipoDato.valueOf(elemType.toUpperCase());

            for (NodoAST elem : ctx.elementos) {
                elem.accept(this);
                if (this.tipoResultado == null) continue;
                if (!TipoUtils.sonCompatibles(elemTipo, this.tipoResultado)) {
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

        if (!TipoUtils.esTipoSlice(tipoBase)) {
            error(ctx.linea, "El acceso por indice solo es valido para slices");
            tipoResultado = null;
            return null;
        }

        if (!validador.checkIndexIsInt(tipoIndice, ctx.linea)) {
            tipoResultado = null;
            return null;
        }

        String nombreElem = TipoUtils.tipoElementoDeSlice(tipoBase);
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
        Simbolo s = validador.checkVariableExists(ctx.id, ctx.linea);
        if (s == null) {
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

        if (!TipoUtils.esTipoSlice(tipoBase)) {
            error(ctx.linea, "La asignacion por indice solo es valida para slices");
            return null;
        }

        if (!validador.checkIndexIsInt(tipoIndice, ctx.linea)) return null;

        String nombreElem = TipoUtils.tipoElementoDeSlice(tipoBase);
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
        if (!TipoUtils.sonCompatibles(tipoElem, tipoValor, structTypeElem, this.tipoStructResultado)) {
            error(ctx.linea, "No se puede asignar un valor de tipo " + tipoValor + " a un elemento de tipo " + nombreElem);
        }

        return null;
    }

    @Override
    public Void visit(AssignIndex2D.Context ctx) {
        Simbolo s = validador.checkVariableExists(ctx.id, ctx.linea);
        if (s == null) {
            tipoResultado = null;
            return null;
        }
        TipoDato tipoBase = s.tipo;

        if (tipoBase == null || !TipoUtils.esTipoSlice(tipoBase) || !tipoBase.name().startsWith("SLICE_SLICE_")) {
            error(ctx.linea, "Acceso multidimensional solo valido para matrices ([][]T)");
            return null;
        }

        ctx.indice1.accept(this);
        if (!validador.checkIndexIsInt(this.tipoResultado, ctx.linea)) return null;

        ctx.indice2.accept(this);
        if (!validador.checkIndexIsInt(this.tipoResultado, ctx.linea)) return null;

        ctx.valor.accept(this);
        TipoDato tipoValor = this.tipoResultado;

        String innerTypeName = TipoUtils.tipoElementoDeSlice(tipoBase);
        TipoDato innerTipo = TipoDato.valueOf(innerTypeName);
        String elemTypeName = TipoUtils.tipoElementoDeSlice(innerTipo);
        TipoDato elemTipo = TipoDato.valueOf(elemTypeName);

        if (!TipoUtils.sonCompatibles(elemTipo, tipoValor)) {
            error(ctx.linea, "No se puede asignar un valor de tipo " + tipoValor + " a un elemento de tipo " + elemTypeName.toLowerCase());
        }

        return null;
    }

    @Override
    public Void visit(Identificador.Context ctx) {
        Simbolo s = validador.checkVariableExists(ctx.nombre, ctx.linea);
        if (s == null) {
            tipoResultado = null;
        } else {
            tipoResultado = s.tipo;
        }
        return null;
    }
    
    @Override
    public Void visit(StructDef.Context ctx) {
        if (!validador.checkGlobalScope(ctx.linea, "Los structs solo pueden declararse en ambito global")) return null;
        if (tabla.existeStruct(ctx.nombre)) {
            error(ctx.linea, "El struct '" + ctx.nombre + "' ya fue declarado");
            return null;
        }
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
    public Void visit(StructMethodDef.Context ctx) {
        if (!validador.checkGlobalScope(ctx.linea, "Las funciones de struct solo pueden declararse en el ambito global")) return null;
        String typeName = ctx.receiverType.toUpperCase();
        if (!validador.checkStructExists(typeName, ctx.receiverType, ctx.linea)) return null;
        Set<String> paramNames = new HashSet<>();
        for (Parametro p : ctx.parametros) {
            if (!paramNames.add(p.nombre.toUpperCase())) {
                error(ctx.linea, "El parametro '" + p.nombre + "' esta duplicado en el metodo '" + ctx.nombre + "'");
            }
            if (p.nombre.equalsIgnoreCase(ctx.receiverVar)) {
                error(ctx.linea, "El parametro '" + p.nombre + "' no puede tener el mismo nombre que el receptor del metodo");
            }
        }
        List<StructDef.Campo> campos = tabla.buscarStruct(typeName);
        for (StructDef.Campo campo : campos) {
            if (campo.nombre.equalsIgnoreCase(ctx.nombre)) {
                error(ctx.linea, "El metodo '" + ctx.nombre + "' no puede tener el mismo nombre que un campo del struct '" + typeName + "'");
                return null;
            }
        }
        StructMethodDef existente = tabla.buscarMetodo(typeName, ctx.nombre);
        if (existente != null) {
            error(ctx.linea, "El metodo '" + ctx.nombre + "' ya existe para el struct '" + typeName + "'");
            return null;
        }
        tabla.definirMetodo(typeName, new StructMethodDef(ctx.receiverVar, ctx.receiverType, ctx.nombre, ctx.parametros, ctx.body, ctx.linea, ctx.columna));
        return null;
    }

    @Override
    public Void visit(StructMethodCall.Context ctx) {
        Simbolo s = validador.checkVariableExists(ctx.id, ctx.linea);
        if (s == null) {
            tipoResultado = null;
            return null;
        }
        if (!validador.checkIsStruct(s, ctx.linea, ctx.id, ", no tiene metodos")) {
            tipoResultado = null;
            return null;
        }
        String structName = s.structType;
        StructMethodDef metodo = tabla.buscarMetodo(structName, ctx.nombre);
        if (metodo == null) {
            error(ctx.linea, "El struct '" + structName + "' no tiene un metodo llamado '" + ctx.nombre + "'");
            tipoResultado = null;
            return null;
        }
        if (ctx.argumentos.size() != metodo.getParametros().size()) {
            error(ctx.linea, "El metodo '" + ctx.nombre + "' espera " + metodo.getParametros().size() + " argumentos pero se recibieron " + ctx.argumentos.size());
            tipoResultado = null;
            return null;
        }
        TipoDato[] argTypes = new TipoDato[ctx.argumentos.size()];
        for (int i = 0; i < ctx.argumentos.size(); i++) {
            ctx.argumentos.get(i).accept(this);
            argTypes[i] = this.tipoResultado;
        }
        validador.validateParamTypes(argTypes, metodo.getParametros(), "del metodo", ctx.nombre, ctx.linea);
        tipoResultado = null;
        return null;
    }

    @Override
    public Void visit(StructAccess.Context ctx) {
        Simbolo s = validador.checkVariableExists(ctx.structId, ctx.linea);
        if (s == null) {
            tipoResultado = null;
            return null;
        }
        if (!validador.checkIsStruct(s, ctx.linea, ctx.structId, "")) {
            tipoResultado = null;
            return null;
        }
        StructDef.Campo campo = validador.lookupStructField(s.structType, ctx.campo, ctx.linea);
        if (campo == null) {
            tipoResultado = null;
        } else {
            TipoUtils.TipoParseResult pr = TipoUtils.parseTipo(campo.tipo);
            tipoResultado = pr.tipo;
        }
        return null;
    }

    @Override
    public Void visit(StructAssign.Context ctx) {
        Simbolo s = validador.checkVariableExists(ctx.structId, ctx.linea);
        if (s == null) return null;
        if (!validador.checkIsStruct(s, ctx.linea, ctx.structId, "")) return null;
        StructDef.Campo campo = validador.lookupStructField(s.structType, ctx.campo, ctx.linea);
        if (campo == null) return null;
        String tipoCampoStr = campo.tipo.toUpperCase();
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
                    if (!TipoUtils.sonCompatibles(tipoCampo, tipoValor)) {
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
    public Void visit(FuncDef.Context ctx) {
        if (!validador.checkGlobalScope(ctx.linea, "Las funciones solo pueden declararse en el ambito global")) return null;
        if (tabla.existeFuncion(ctx.nombre)) {
            error(ctx.linea, "La funcion '" + ctx.nombre + "' ya existe");
            return null;
        }
        Set<String> paramNames = new HashSet<>();
        for (Parametro p : ctx.parametros) {
            if (!paramNames.add(p.nombre.toUpperCase())) {
                error(ctx.linea, "El parametro '" + p.nombre + "' esta duplicado en la funcion '" + ctx.nombre + "'");
            }
        }
        tabla.definirFuncion(ctx.nombre, new FuncDef(ctx.nombre, ctx.parametros, ctx.tipoRetorno, ctx.body, ctx.linea, ctx.columna));
        return null;
    }

    @Override
    public Void visit(FuncCall.Context ctx) {
        FuncDef func = tabla.buscarFuncion(ctx.nombre);
        if (func == null) {
            error(ctx.linea, "La funcion '" + ctx.nombre + "' no ha sido declarada");
            tipoResultado = null;
            return null;
        }
        if (ctx.argumentos.size() != func.getParametros().size()) {
            error(ctx.linea, "La funcion '" + ctx.nombre + "' espera " + func.getParametros().size() + " argumentos pero se recibieron " + ctx.argumentos.size());
            tipoResultado = null;
            return null;
        }
        TipoDato[] argTypes = new TipoDato[ctx.argumentos.size()];
        for (int i = 0; i < ctx.argumentos.size(); i++) {
            ctx.argumentos.get(i).accept(this);
            argTypes[i] = this.tipoResultado;
        }
        validador.validateParamTypes(argTypes, func.getParametros(), "de la funcion", ctx.nombre, ctx.linea);
        if (func.getTipoRetorno() != null) {
            TipoUtils.TipoParseResult pr = TipoUtils.parseTipo(func.getTipoRetorno());
            tipoResultado = pr.tipo;
            tipoStructResultado = pr.structType;
        } else {
            tipoResultado = null;
        }
        return null;
    }

    @Override
    public Void visit(ReturnStmt.Context ctx) {
        if (ctx.valor != null) {
            ctx.valor.accept(this);
        } else {
            tipoResultado = null;
        }
        return null;
    }

    @Override
    public Void visit(NewStruct.Context ctx) {
        String nombreTipo = ctx.nombreTipo.toUpperCase();
        if (!validador.checkStructExists(nombreTipo, ctx.linea)) {
            tipoResultado = null;
            return null;
        }
        List<StructDef.Campo> definicion = tabla.buscarStruct(nombreTipo);
        for (StructDef.Campo campo : definicion) {
            boolean encontrado = false;
            for (NewStruct.CampoInit init : ctx.campos) {
                if (init.nombre.equals(campo.nombre)) {
                    init.valor.accept(this);
                    String tipoEsperadoStr = campo.tipo.toUpperCase();
                    if (tipoEsperadoStr.startsWith("STRUCT_") || tipoEsperadoStr.startsWith("SLICE_STRUCT_")) {
                        if (this.tipoResultado != TipoDato.STRUCT && this.tipoResultado != TipoDato.SLICE_STRUCT) {
                            error(ctx.linea, "El campo '" + campo.nombre + "' espera un struct");
                        }
                    } else {
                        TipoUtils.TipoParseResult pr = TipoUtils.parseTipo(campo.tipo);
                        if (pr.tipo != null && !TipoUtils.sonCompatibles(pr.tipo, this.tipoResultado)) {
                            error(ctx.linea, "El campo '" + campo.nombre + "' espera " + tipoEsperadoStr.toLowerCase() + " pero se recibio " + this.tipoResultado);
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

}
