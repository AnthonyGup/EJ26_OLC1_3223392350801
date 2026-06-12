package analisis.visitor.ejecucion;

import analisis.visitor.ejecucion.valor.*;
import java.util.HashMap;
import java.util.Map;

public class GestorAmbitos {

    private final Map<String, Valor> ambito;

    public GestorAmbitos() {
        ambito = new HashMap<>();
    }

    public void declarar(String id, Valor valor) {
        ambito.put(id, valor);
    }

    public boolean existeEnAmbitoActual(String id) {
        return ambito.containsKey(id);
    }

    public Valor buscar(String id, int linea) {
        Valor v = ambito.get(id);
        if (v != null) {
            return v;
        }
        throw new RuntimeException("Linea " + linea + ": variable '" + id + "' no declarada");
    }

    public void asignar(String id, Valor valor, int linea) {
        if (ambito.containsKey(id)) {
            ambito.put(id, valor);
        } else {
            throw new RuntimeException("Linea " + linea + ": variable '" + id + "' no declarada");
        }
    }
}
