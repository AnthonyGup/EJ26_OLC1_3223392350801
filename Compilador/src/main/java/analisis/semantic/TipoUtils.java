package analisis.semantic;

public class TipoUtils {

    public static boolean esTipoSlice(TipoDato t) {
        return t.name().startsWith("SLICE_");
    }

    public static String tipoElementoDeSlice(TipoDato t) {
        String name = t.name();
        if (name.startsWith("SLICE_")) {
            return name.substring(6);
        }
        return null;
    }

    public static boolean esNumerico(TipoDato t) {
        return t == TipoDato.INT || t == TipoDato.FLOAT64;
    }

    public static TipoDato promover(TipoDato a, TipoDato b) {
        if (a == TipoDato.FLOAT64 || b == TipoDato.FLOAT64) {
            return TipoDato.FLOAT64;
        }
        return TipoDato.INT;
    }

    public static boolean sonCompatibles(TipoDato declarado, TipoDato expr) {
        return sonCompatibles(declarado, expr, null, null);
    }

    public static boolean sonCompatibles(TipoDato declarado, TipoDato expr, String structDecl, String structExpr) {
        if (declarado == TipoDato.STRUCT || declarado == TipoDato.SLICE_STRUCT) {
            if (declarado != expr) return false;
            if (structDecl == null || structExpr == null) return false;
            return structDecl.equals(structExpr);
        }
        if (declarado == expr) return true;
        if (declarado == TipoDato.FLOAT64 && expr == TipoDato.INT) return true;
        return false;
    }

    public static class TipoParseResult {
        public TipoDato tipo;
        public String structType;
    }

    public static TipoParseResult parseTipo(String rawTipo) {
        String upper = rawTipo.toUpperCase();
        TipoParseResult r = new TipoParseResult();
        if (upper.startsWith("STRUCT_")) {
            r.tipo = TipoDato.STRUCT;
            r.structType = rawTipo.substring(7);
        } else if (upper.startsWith("SLICE_STRUCT_")) {
            r.tipo = TipoDato.SLICE_STRUCT;
            r.structType = rawTipo.substring(13);
        } else {
            try {
                r.tipo = TipoDato.valueOf(upper);
            } catch (IllegalArgumentException e) {
                r.tipo = null;
            }
        }
        return r;
    }
}
