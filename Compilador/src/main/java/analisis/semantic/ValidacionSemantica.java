package analisis.semantic;

import analisis.ast.Parametro;
import analisis.ast.stm.StructDef;
import java.util.List;

public class ValidacionSemantica {

    private TablaSimbolos tabla;
    private List<String> errores;

    public ValidacionSemantica(TablaSimbolos tabla, List<String> errores) {
        this.tabla = tabla;
        this.errores = errores;
    }

    private void error(int linea, String msg) {
        errores.add("Linea " + linea + ": " + msg);
    }

    public boolean checkGlobalScope(int linea, String msg) {
        if (tabla.getNivelAmbito() > 0) {
            error(linea, msg);
            return false;
        }
        return true;
    }

    public boolean checkStructExists(String lookupName, String displayName, int linea) {
        if (!tabla.existeStruct(lookupName)) {
            error(linea, "El struct '" + displayName + "' no ha sido declarado");
            return false;
        }
        return true;
    }

    public boolean checkStructExists(String name, int linea) {
        return checkStructExists(name, name, linea);
    }

    public Simbolo checkVariableExists(String id, int linea) {
        Simbolo s = tabla.buscar(id);
        if (s == null) {
            error(linea, "La variable '" + id + "' no ha sido declarada");
        }
        return s;
    }

    public boolean checkIsStruct(Simbolo s, int linea, String id, String extra) {
        if (s.tipo != TipoDato.STRUCT) {
            error(linea, "La variable '" + id + "' no es un struct" + extra);
            return false;
        }
        return true;
    }

    public boolean checkIndexIsInt(TipoDato tipoIndice, int linea) {
        if (tipoIndice != TipoDato.INT) {
            error(linea, "El indice debe ser de tipo int");
            return false;
        }
        return true;
    }

    public StructDef.Campo lookupStructField(String structType, String fieldName, int linea) {
        List<StructDef.Campo> campos = tabla.buscarStruct(structType);
        if (campos == null) {
            error(linea, "El tipo struct '" + structType + "' no esta definido");
            return null;
        }
        for (StructDef.Campo c : campos) {
            if (c.nombre.equals(fieldName)) {
                return c;
            }
        }
        error(linea, "El struct '" + structType + "' no tiene un campo llamado '" + fieldName + "'");
        return null;
    }

    public void validateParamTypes(TipoDato[] argTypes, List<Parametro> params, String kind, String name, int linea) {
        for (int i = 0; i < argTypes.length; i++) {
            TipoDato tipoArg = argTypes[i];
            if (tipoArg == null) continue;
            String tipoParamStr = params.get(i).tipo.toUpperCase();
            if (tipoParamStr.startsWith("STRUCT_") || tipoParamStr.startsWith("SLICE_STRUCT_")) {
                if (tipoArg != TipoDato.STRUCT && tipoArg != TipoDato.SLICE_STRUCT) {
                    error(linea, "El parametro " + (i + 1) + " " + kind + " '" + name + "' espera un struct");
                }
            } else {
                try {
                    TipoDato tipoParam = TipoDato.valueOf(tipoParamStr);
                    if (!TipoUtils.sonCompatibles(tipoParam, tipoArg, null, null)) {
                        error(linea, "El parametro " + (i + 1) + " " + kind + " '" + name + "' espera " + tipoParamStr.toLowerCase() + " pero se recibio " + tipoArg);
                    }
                } catch (IllegalArgumentException e) {
                    error(linea, "Tipo de parametro no reconocido: " + tipoParamStr);
                }
            }
        }
    }
}
