package analisis.visitor.ejecucion;

import analisis.ast.*;
import analisis.ast.exp.*;
import analisis.ast.stm.*;
import analisis.visitor.Visitor;
import analisis.visitor.ejecucion.valor.*;
import java.util.List;

public class VisitorEjecucion implements Visitor<Valor> {

    public String output = "";

    private final Valor defaultVoid = new ValorVoid(-1, -1);

    private final GestorAmbitos gestor = new GestorAmbitos();

    public Valor Visitar(NodoAST nodo) {
        return nodo.accept(this);
    }

    @Override
    public Valor visit(Programa.Context ctx) {
        for (NodoAST instr : ctx.instrucciones) {
            instr.accept(this);
        }
        return defaultVoid;
    }

    @Override
    public Valor visit(Bloque.Context ctx) {
        gestor.entrarBloque();
        try {
            for (NodoAST instr : ctx.instrucciones) {
                instr.accept(this);
            }
        } finally {
            gestor.salirBloque();
        }
        return defaultVoid;
    }

    @Override
    public Valor visit(DeclaracionVar.Context ctx) {
        if (gestor.existeEnAmbitoActual(ctx.id)) {
            throw new RuntimeException("Linea " + ctx.linea + ": variable '" + ctx.id + "' ya declarada en este ambito");
        }

        Valor valor = Operaciones.obtenerValorDefecto(ctx.tipo, ctx.linea, ctx.columna);

        if (ctx.expr != null) {
            valor = ctx.expr.accept(this);
        }

        gestor.declarar(ctx.id, valor);
        return defaultVoid;
    }

    @Override
    public Valor visit(Asignacion.Context ctx) {
        Valor valor = ctx.expr.accept(this);
        gestor.asignar(ctx.id, valor, ctx.linea);
        return defaultVoid;
    }

    @Override
    public Valor visit(AsignacionOp.Context ctx) {
        Valor actual = gestor.buscar(ctx.id, ctx.linea);
        Valor expr = ctx.expr.accept(this);

        Valor resultado;
        if (ctx.operador.equals("+=")) {
            resultado = Operaciones.sumar(actual, expr, ctx.linea);
        } else {
            resultado = Operaciones.restar(actual, expr, ctx.linea);
        }

        gestor.asignar(ctx.id, resultado, ctx.linea);
        return defaultVoid;
    }

    @Override
    public Valor visit(If.Context ctx) {
        if (ctx.condicion != null) {
            Valor cond = ctx.condicion.accept(this);
            if (cond instanceof ValorBool b && b.valor()) {
                ctx.bloqueIf.accept(this);
            } else if (ctx.bloqueElse != null) {
                ctx.bloqueElse.accept(this);
            }
        } else {
            ctx.bloqueIf.accept(this);
        }
        return defaultVoid;
    }

    @Override
    public Valor visit(For.Context ctx) {
        gestor.entrarBloque();
        if (ctx.init != null) {
            ctx.init.accept(this);
        }

        try {
            while (true) {
                if (ctx.condicion == null) break;
                Valor cond = ctx.condicion.accept(this);
                if (cond instanceof ValorBool b) {
                    if (!b.valor()) {
                        break;
                    }
                } else {
                    throw new RuntimeException("Linea " + ctx.linea + ": condicion del for debe ser bool");
                }

                try {
                    ctx.bloque.accept(this);
                } catch (ContinueException e) {
                }

                if (ctx.update != null) {
                    ctx.update.accept(this);
                }
            }
        } catch (BreakException e) {
        }

        gestor.salirBloque();
        return defaultVoid;
    }

    @Override
    public Valor visit(Switch.Context ctx) {
        Valor valorSwitch = ctx.expresion.accept(this);

        for (Switch.Caso caso : ctx.casos) {
            boolean coincide = false;
            for (NodoAST expresionCaso : caso.expresiones) {
                Valor valorCaso = expresionCaso.accept(this);
                Valor comparacion = Operaciones.comparar(valorSwitch, valorCaso, caso.linea);
                if (comparacion instanceof ValorBool b && b.valor()) {
                    coincide = true;
                    break;
                }
            }

            if (coincide) {
                try {
                    caso.bloque.accept(this);
                } catch (BreakException e) {
                }
                return defaultVoid;
            }
        }

        if (ctx.bloqueDefault != null) {
            try {
                ctx.bloqueDefault.accept(this);
            } catch (BreakException e) {
            }
        }

        return defaultVoid;
    }

    @Override
    public Valor visit(Break.Context ctx) {
        throw new BreakException();
    }

    @Override
    public Valor visit(Continue.Context ctx) {
        throw new ContinueException();
    }

    @Override
    public Valor visit(LlamadaFuncion.Context ctx) {
        switch (ctx.paquete) {
            case "fmt":
                if (ctx.funcion.equals("Println")) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < ctx.argumentos.size(); i++) {
                        if (i > 0) {
                            sb.append(" ");
                        }
                        sb.append(ctx.argumentos.get(i).accept(this).toString());
                    }
                    output += sb.toString() + "\n";
                }
                return defaultVoid;

            case "strconv":
                if (ctx.argumentos.isEmpty()) {
                    throw new RuntimeException("Linea " + ctx.linea + ": " + ctx.funcion + " espera un argumento");
                }
                Valor arg = ctx.argumentos.get(0).accept(this);
                if (!(arg instanceof ValorString vs)) {
                    throw new RuntimeException("Linea " + ctx.linea + ": " + ctx.funcion + " espera un string");
                }
                if (ctx.funcion.equals("Atoi")) {
                    try {
                        int val = Integer.parseInt(vs.valor());
                        return new ValorInt(val, ctx.linea, ctx.columna);
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Linea " + ctx.linea + ": strconv.Atoi: '" + vs.valor() + "' no es un entero valido");
                    }
                } else {
                    try {
                        double val = Double.parseDouble(vs.valor());
                        return new ValorFloat(val, ctx.linea, ctx.columna);
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Linea " + ctx.linea + ": strconv.ParseFloat: '" + vs.valor() + "' no es un decimal valido");
                    }
                }

            case "reflect":
                if (ctx.argumentos.isEmpty()) {
                    throw new RuntimeException("Linea " + ctx.linea + ": reflect.TypeOf espera un argumento");
                }
                Valor expr = ctx.argumentos.get(0).accept(this);
                return new ValorString(expr.obtenerTipoNombre(), ctx.linea, ctx.columna);

            case "slices":
                if (ctx.funcion.equals("Index")) {
                    if (ctx.argumentos.size() < 2) {
                        throw new RuntimeException("Linea " + ctx.linea + ": slices.Index espera 2 argumentos");
                    }
                    Valor slice = ctx.argumentos.get(0).accept(this);
                    Valor buscado = ctx.argumentos.get(1).accept(this);

                    if (!(slice instanceof ValorSlice vs)) {
                        throw new RuntimeException("Linea " + ctx.linea + ": slices.Index: el primer argumento debe ser un slice");
                    }

                    java.util.List<Valor> elementos = vs.elementos();
                    for (int i = 0; i < elementos.size(); i++) {
                        Valor comp = Operaciones.comparar(elementos.get(i), buscado, ctx.linea);
                        if (comp instanceof ValorBool b && b.valor()) {
                            return new ValorInt(i, ctx.linea, ctx.columna);
                        }
                    }
                    return new ValorInt(-1, ctx.linea, ctx.columna);
                }
                return defaultVoid;

            case "strings":
                if (ctx.funcion.equals("Join")) {
                    if (ctx.argumentos.size() < 2) {
                        throw new RuntimeException("Linea " + ctx.linea + ": strings.Join espera 2 argumentos");
                    }
                    Valor sliceVal = ctx.argumentos.get(0).accept(this);
                    Valor sepVal = ctx.argumentos.get(1).accept(this);

                    if (!(sliceVal instanceof ValorSlice vs)) {
                        throw new RuntimeException("Linea " + ctx.linea + ": strings.Join: el primer argumento debe ser un slice");
                    }
                    if (!(sepVal instanceof ValorString sep)) {
                        throw new RuntimeException("Linea " + ctx.linea + ": strings.Join: el separador debe ser un string");
                    }

                    StringBuilder sb = new StringBuilder();
                    java.util.List<Valor> elementos = vs.elementos();
                    for (int i = 0; i < elementos.size(); i++) {
                        if (i > 0) sb.append(sep.valor());
                        Valor e = elementos.get(i);
                        if (e instanceof ValorString vs2) {
                            sb.append(vs2.valor());
                        } else {
                            throw new RuntimeException("Linea " + ctx.linea + ": strings.Join: elemento del slice no es string");
                        }
                    }
                    return new ValorString(sb.toString(), ctx.linea, ctx.columna);
                }
                return defaultVoid;

            default:
                throw new RuntimeException("Linea " + ctx.linea + ": paquete desconocido '" + ctx.paquete + "'");
        }
    }

    @Override
    public Valor visit(ExpBinaria.Context ctx) {
        Valor izq = ctx.izquierdo.accept(this);
        Valor der = ctx.derecho.accept(this);

        switch (ctx.operador) {
            case "+":
                return Operaciones.sumar(izq, der, ctx.linea);
            case "-":
                return Operaciones.restar(izq, der, ctx.linea);
            case "*":
                return Operaciones.multiplicar(izq, der, ctx.linea);
            case "/":
                return Operaciones.dividir(izq, der, ctx.linea);
            case "%":
                return Operaciones.modulo(izq, der, ctx.linea);
            case "==":
                return Operaciones.comparar(izq, der, ctx.linea);
            case "!=": {
                Valor r = Operaciones.comparar(izq, der, ctx.linea);
                if (r instanceof ValorBool b) {
                    return new ValorBool(!b.valor(), ctx.linea, ctx.columna);
                }
                throw new RuntimeException("Linea " + ctx.linea + ": error interno en !=");
            }
            case ">":
                return Operaciones.compararOrd(izq, der, ">", ctx.linea);
            case ">=":
                return Operaciones.compararOrd(izq, der, ">=", ctx.linea);
            case "<":
                return Operaciones.compararOrd(izq, der, "<", ctx.linea);
            case "<=":
                return Operaciones.compararOrd(izq, der, "<=", ctx.linea);
            case "&&":
                return Operaciones.logicAnd(izq, der, ctx.linea);
            case "||":
                return Operaciones.logicOr(izq, der, ctx.linea);
            default:
                throw new RuntimeException("Linea " + ctx.linea + ": operador desconocido '" + ctx.operador + "'");
        }
    }

    @Override
    public Valor visit(ExpUnaria.Context ctx) {
        Valor val = ctx.expr.accept(this);

        if (ctx.operador.equals("-")) {
            return switch (val) {
                case ValorInt v ->
                    new ValorInt(-v.valor(), ctx.linea, ctx.columna);
                case ValorFloat v ->
                    new ValorFloat(-v.valor(), ctx.linea, ctx.columna);
                default ->
                    throw new RuntimeException("Linea " + ctx.linea + ": negacion unaria no valida para " + val.obtenerTipoNombre());
            };
        } else {
            if (val instanceof ValorBool b) {
                return new ValorBool(!b.valor(), ctx.linea, ctx.columna);
            }
            throw new RuntimeException("Linea " + ctx.linea + ": operador ! no valido para " + val.obtenerTipoNombre());
        }
    }

    @Override
    public Valor visit(Literal.Context ctx) {
        if (ctx.valor == null) {
            return new ValorNil(ctx.linea, ctx.columna);
        }

        String str = ctx.valor.toString();
        return switch (ctx.tipo) {
            case "int" ->
                new ValorInt(Integer.parseInt(str), ctx.linea, ctx.columna);
            case "float64" ->
                new ValorFloat(Double.parseDouble(str), ctx.linea, ctx.columna);
            case "string" ->
                new ValorString(str, ctx.linea, ctx.columna);
            case "bool" ->
                new ValorBool(Boolean.parseBoolean(str), ctx.linea, ctx.columna);
            case "rune" -> {
                String s = str;
                if (s.startsWith("'") && s.endsWith("'") && s.length() >= 2) {
                    s = s.substring(1, s.length() - 1);
                }
                yield new ValorRune(s.charAt(0), ctx.linea, ctx.columna);
            }
            case "nil" ->
                new ValorNil(ctx.linea, ctx.columna);
            default ->
                throw new RuntimeException("Linea " + ctx.linea + ": tipo literal desconocido '" + ctx.tipo + "'");
        };
    }

    @Override
    public Valor visit(SliceLiteral.Context ctx) {
        List<NodoAST> exprs = ctx.elementos;
        List<Valor> valores = new java.util.ArrayList<>();
        for (NodoAST e : exprs) {
            valores.add(e.accept(this));
        }
        return new ValorSlice(valores, ctx.tipoElemento, ctx.linea, ctx.columna);
    }

    @Override
    public Valor visit(SliceLiteral2D.Context ctx) {
        List<Valor> outerElems = new java.util.ArrayList<>();
        for (List<NodoAST> fila : ctx.filas) {
            List<Valor> innerElems = new java.util.ArrayList<>();
            for (NodoAST expr : fila) {
                innerElems.add(expr.accept(this));
            }
            outerElems.add(new ValorSlice(innerElems, ctx.tipo, ctx.linea, ctx.columna));
        }
        return new ValorSlice(outerElems, "[]" + ctx.tipo, ctx.linea, ctx.columna);
    }

    @Override
    public Valor visit(Len.Context ctx) {
        Valor val = ctx.expr.accept(this);
        if (val instanceof ValorSlice vs) {
            return new ValorInt(vs.elementos().size(), ctx.linea, ctx.columna);
        }
        throw new RuntimeException("Linea " + ctx.linea + ": len solo es válido para slices");
    }

    @Override
    public Valor visit(Append.Context ctx) {
        Valor sliceVal = ctx.slice.accept(this);
        if (!(sliceVal instanceof ValorSlice vs)) {
            throw new RuntimeException("Linea " + ctx.linea + ": append: el primer argumento debe ser un slice");
        }

        java.util.List<Valor> nuevos = new java.util.ArrayList<>(vs.elementos());
        for (NodoAST elem : ctx.elementos) {
            nuevos.add(elem.accept(this));
        }

        return new ValorSlice(nuevos, vs.tipoElemento(), ctx.linea, ctx.columna);
    }

    @Override
    public Valor visit(IndexAccess.Context ctx) {
        Valor baseVal = ctx.base.accept(this);
        Valor idxVal = ctx.indice.accept(this);

        if (!(baseVal instanceof ValorSlice vs)) {
            throw new RuntimeException("Linea " + ctx.linea + ": el acceso por indice solo es valido para slices");
        }
        if (!(idxVal instanceof ValorInt vi)) {
            throw new RuntimeException("Linea " + ctx.linea + ": el indice debe ser un entero");
        }

        int idx = vi.valor();
        java.util.List<Valor> elems = vs.elementos();

        if (idx < 0 || idx >= elems.size()) {
            throw new RuntimeException("Linea " + ctx.linea + ": indice " + idx + " fuera de rango (tamaño " + elems.size() + ")");
        }

        return elems.get(idx);
    }

    @Override
    public Valor visit(AssignIndex.Context ctx) {
        Valor baseVal = gestor.buscar(ctx.id, ctx.linea);
        Valor idxVal = ctx.indice.accept(this);
        Valor valVal = ctx.valor.accept(this);

        if (!(baseVal instanceof ValorSlice vs)) {
            throw new RuntimeException("Linea " + ctx.linea + ": la asignacion por indice solo es valida para slices");
        }
        if (!(idxVal instanceof ValorInt vi)) {
            throw new RuntimeException("Linea " + ctx.linea + ": el indice debe ser un entero");
        }

        int idx = vi.valor();
        java.util.List<Valor> elems = vs.elementos();

        if (idx < 0 || idx >= elems.size()) {
            throw new RuntimeException("Linea " + ctx.linea + ": indice " + idx + " fuera de rango (tamaño " + elems.size() + ")");
        }

        java.util.List<Valor> nuevos = new java.util.ArrayList<>(elems);
        nuevos.set(idx, valVal);
        ValorSlice nuevoSlice = new ValorSlice(nuevos, vs.tipoElemento(), ctx.linea, ctx.columna);

        gestor.asignar(ctx.id, nuevoSlice, ctx.linea);
        return defaultVoid;
    }

    @Override
    public Valor visit(AssignIndex2D.Context ctx) {
        Valor baseVal = gestor.buscar(ctx.id, ctx.linea);
        Valor idx1Val = ctx.indice1.accept(this);
        Valor idx2Val = ctx.indice2.accept(this);
        Valor valVal = ctx.valor.accept(this);

        if (!(baseVal instanceof ValorSlice vs)) {
            throw new RuntimeException("Linea " + ctx.linea + ": la asignacion multidimensional solo es valida para matrices");
        }
        if (!(idx1Val instanceof ValorInt vi1)) {
            throw new RuntimeException("Linea " + ctx.linea + ": el indice debe ser un entero");
        }
        if (!(idx2Val instanceof ValorInt vi2)) {
            throw new RuntimeException("Linea " + ctx.linea + ": el indice debe ser un entero");
        }

        int i1 = vi1.valor(), i2 = vi2.valor();
        if (i1 < 0 || i1 >= vs.elementos().size()) {
            throw new RuntimeException("Linea " + ctx.linea + ": indice " + i1 + " fuera de rango");
        }

        ValorSlice inner = (ValorSlice) vs.elementos().get(i1);
        if (i2 < 0 || i2 >= inner.elementos().size()) {
            throw new RuntimeException("Linea " + ctx.linea + ": indice " + i2 + " fuera de rango");
        }

        java.util.List<Valor> outerElems = new java.util.ArrayList<>(vs.elementos());
        java.util.List<Valor> innerElems = new java.util.ArrayList<>(inner.elementos());
        innerElems.set(i2, valVal);
        outerElems.set(i1, new ValorSlice(innerElems, inner.tipoElemento(), ctx.linea, ctx.columna));
        gestor.asignar(ctx.id, new ValorSlice(outerElems, vs.tipoElemento(), ctx.linea, ctx.columna), ctx.linea);
        return defaultVoid;
    }

    @Override
    public Valor visit(Identificador.Context ctx) {
        return gestor.buscar(ctx.nombre, ctx.linea);
    }
}
