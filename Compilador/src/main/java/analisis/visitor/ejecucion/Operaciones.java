package analisis.visitor.ejecucion;

import analisis.visitor.ejecucion.valor.*;

public final class Operaciones {

    private Operaciones() {
    }

    public static Valor obtenerValorDefecto(String tipo, int linea, int columna) {
        if (tipo == null) {
            return new ValorNil(linea, columna);
        }
        return switch (tipo) {
            case "int" ->
                new ValorInt(0, linea, columna);
            case "float64" ->
                new ValorFloat(0.0, linea, columna);
            case "string" ->
                new ValorString("", linea, columna);
            case "bool" ->
                new ValorBool(false, linea, columna);
            case "rune" ->
                new ValorRune(0, linea, columna);
            default ->
                new ValorNil(linea, columna);
        };
    }

    public static Valor sumar(Valor a, Valor b, int linea) {
        if (a instanceof ValorString sa && b instanceof ValorString sb) {
            return new ValorString(sa.valor() + sb.valor(), linea, linea);
        }
        return switch (a) {
            case ValorInt i when b instanceof ValorInt r ->
                new ValorInt(i.valor() + r.valor(), linea, linea);
            case ValorInt i when b instanceof ValorFloat r ->
                new ValorFloat(i.valor() + r.valor(), linea, linea);
            case ValorFloat i when b instanceof ValorInt r ->
                new ValorFloat(i.valor() + r.valor(), linea, linea);
            case ValorFloat i when b instanceof ValorFloat r ->
                new ValorFloat(i.valor() + r.valor(), linea, linea);
            default ->
                throw new RuntimeException("Linea " + linea + ": operador + no valido entre " + a.obtenerTipoNombre() + " y " + b.obtenerTipoNombre());
        };
    }

    public static Valor restar(Valor a, Valor b, int linea) {
        return switch (a) {
            case ValorInt i when b instanceof ValorInt r ->
                new ValorInt(i.valor() - r.valor(), linea, linea);
            case ValorInt i when b instanceof ValorFloat r ->
                new ValorFloat(i.valor() - r.valor(), linea, linea);
            case ValorFloat i when b instanceof ValorInt r ->
                new ValorFloat(i.valor() - r.valor(), linea, linea);
            case ValorFloat i when b instanceof ValorFloat r ->
                new ValorFloat(i.valor() - r.valor(), linea, linea);
            default ->
                throw new RuntimeException("Linea " + linea + ": operador - no valido entre " + a.obtenerTipoNombre() + " y " + b.obtenerTipoNombre());
        };
    }

    public static Valor multiplicar(Valor a, Valor b, int linea) {
        return switch (a) {
            case ValorInt i when b instanceof ValorInt r ->
                new ValorInt(i.valor() * r.valor(), linea, linea);
            case ValorInt i when b instanceof ValorFloat r ->
                new ValorFloat(i.valor() * r.valor(), linea, linea);
            case ValorFloat i when b instanceof ValorInt r ->
                new ValorFloat(i.valor() * r.valor(), linea, linea);
            case ValorFloat i when b instanceof ValorFloat r ->
                new ValorFloat(i.valor() * r.valor(), linea, linea);
            default ->
                throw new RuntimeException("Linea " + linea + ": operador * no valido entre " + a.obtenerTipoNombre() + " y " + b.obtenerTipoNombre());
        };
    }

    public static Valor dividir(Valor a, Valor b, int linea) {
        return switch (a) {
            case ValorInt i when b instanceof ValorInt r -> {
                if (r.valor() == 0) {
                    throw new RuntimeException("Linea " + linea + ": division por cero");
                }
                yield new ValorInt(i.valor() / r.valor(), linea, linea);
            }
            case ValorInt i when b instanceof ValorFloat r -> {
                if (r.valor() == 0.0) {
                    throw new RuntimeException("Linea " + linea + ": division por cero");
                }
                yield new ValorFloat(i.valor() / r.valor(), linea, linea);
            }
            case ValorFloat i when b instanceof ValorInt r -> {
                if (r.valor() == 0) {
                    throw new RuntimeException("Linea " + linea + ": division por cero");
                }
                yield new ValorFloat(i.valor() / r.valor(), linea, linea);
            }
            case ValorFloat i when b instanceof ValorFloat r -> {
                if (r.valor() == 0.0) {
                    throw new RuntimeException("Linea " + linea + ": division por cero");
                }
                yield new ValorFloat(i.valor() / r.valor(), linea, linea);
            }
            default ->
                throw new RuntimeException("Linea " + linea + ": operador / no valido entre " + a.obtenerTipoNombre() + " y " + b.obtenerTipoNombre());
        };
    }

    public static Valor modulo(Valor a, Valor b, int linea) {
        if (a instanceof ValorInt i && b instanceof ValorInt r) {
            if (r.valor() == 0) {
                throw new RuntimeException("Linea " + linea + ": modulo por cero");
            }
            return new ValorInt(i.valor() % r.valor(), linea, linea);
        }
        throw new RuntimeException("Linea " + linea + ": operador % solo valido para int");
    }

    public static Valor comparar(Valor a, Valor b, int linea) {
        if (a instanceof ValorInt i && b instanceof ValorInt r) {
            return new ValorBool(i.valor() == r.valor(), linea, linea);
        }
        if (a instanceof ValorFloat i && b instanceof ValorFloat r) {
            return new ValorBool(i.valor() == r.valor(), linea, linea);
        }
        if (a instanceof ValorFloat i && b instanceof ValorInt r) {
            return new ValorBool(i.valor() == (double) r.valor(), linea, linea);
        }
        if (a instanceof ValorInt i && b instanceof ValorFloat r) {
            return new ValorBool((double) i.valor() == r.valor(), linea, linea);
        }
        if (a instanceof ValorBool i && b instanceof ValorBool r) {
            return new ValorBool(i.valor() == r.valor(), linea, linea);
        }
        if (a instanceof ValorString i && b instanceof ValorString r) {
            return new ValorBool(i.valor().equals(r.valor()), linea, linea);
        }
        if (a instanceof ValorRune i && b instanceof ValorRune r) {
            return new ValorBool(i.valor() == r.valor(), linea, linea);
        }
        if (a instanceof ValorNil && b instanceof ValorNil) {
            return new ValorBool(true, linea, linea);
        }
        if (a instanceof ValorNil || b instanceof ValorNil) {
            return new ValorBool(false, linea, linea);
        }
        throw new RuntimeException("Linea " + linea + ": comparacion == no valida entre " + a.obtenerTipoNombre() + " y " + b.obtenerTipoNombre());
    }

    public static Valor compararOrd(Valor a, Valor b, String op, int linea) {
        double ai = 0, bi = 0;
        if (a instanceof ValorInt i) {
            ai = i.valor();
        } else if (a instanceof ValorFloat f) {
            ai = f.valor();
        } else {
            throw new RuntimeException("Linea " + linea + ": operador " + op + " no valido para " + a.obtenerTipoNombre());
        }

        if (b instanceof ValorInt i) {
            bi = i.valor();
        } else if (b instanceof ValorFloat f) {
            bi = f.valor();
        } else {
            throw new RuntimeException("Linea " + linea + ": operador " + op + " no valido para " + b.obtenerTipoNombre());
        }

        boolean r = switch (op) {
            case ">" ->
                ai > bi;
            case ">=" ->
                ai >= bi;
            case "<" ->
                ai < bi;
            case "<=" ->
                ai <= bi;
            default ->
                throw new RuntimeException("Linea " + linea + ": operador desconocido " + op);
        };
        return new ValorBool(r, linea, linea);
    }

    public static Valor logicAnd(Valor a, Valor b, int linea) {
        if (a instanceof ValorBool ia && b instanceof ValorBool ib) {
            return new ValorBool(ia.valor() && ib.valor(), linea, linea);
        }
        throw new RuntimeException("Linea " + linea + ": operador && solo valido para bool");
    }

    public static Valor logicOr(Valor a, Valor b, int linea) {
        if (a instanceof ValorBool ia && b instanceof ValorBool ib) {
            return new ValorBool(ia.valor() || ib.valor(), linea, linea);
        }
        throw new RuntimeException("Linea " + linea + ": operador || solo valido para bool");
    }
}
