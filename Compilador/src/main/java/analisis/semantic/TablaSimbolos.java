package analisis.semantic;

import java.util.*;

public class TablaSimbolos {
    private final Map<String, Simbolo> ambito;

    public TablaSimbolos() {
        ambito = new HashMap<>();
    }

    public boolean insertar(Simbolo s) {
        if (ambito.containsKey(s.id)) {
            return false;
        }
        ambito.put(s.id, s);
        return true;
    }

    public Simbolo buscar(String id) {
        return ambito.get(id);
    }

    public boolean existeEnAmbitoActual(String id) {
        return ambito.containsKey(id);
    }

    public List<Simbolo> obtenerTodos() {
        return new ArrayList<>(ambito.values());
    }
}
