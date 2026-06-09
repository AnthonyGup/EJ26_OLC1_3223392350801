package analisis.visitor.ejecucion;

import analisis.visitor.ejecucion.valor.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class GestorAmbitos {

    private final Deque<Map<String, Valor>> ambitos = new ArrayDeque<>();

    public GestorAmbitos() {
        ambitos.push(new HashMap<>());
    }

    public void entrarBloque() {
        ambitos.push(new HashMap<>());
    }

    public void salirBloque() {
        ambitos.pop();
    }

    public void declarar(String id, Valor valor) {
        ambitos.peek().put(id, valor);
    }

    public boolean existeEnAmbitoActual(String id) {
        return ambitos.peek().containsKey(id);
    }

    public boolean existeEnAlgunAmbito(String id) {
        for (Map<String, Valor> ambito : ambitos) {
            if (ambito.containsKey(id)) {
                return true;
            }
        }
        return false;
    }

    public Valor buscar(String id, int linea) {
        for (Map<String, Valor> ambito : ambitos) {
            Valor v = ambito.get(id);
            if (v != null) {
                return v;
            }
        }
        throw new RuntimeException("Linea " + linea + ": variable '" + id + "' no declarada");
    }

    public void asignar(String id, Valor valor, int linea) {
        for (Map<String, Valor> ambito : ambitos) {
            if (ambito.containsKey(id)) {
                ambito.put(id, valor);
                return;
            }
        }
        throw new RuntimeException("Linea " + linea + ": variable '" + id + "' no declarada");
    }
}
