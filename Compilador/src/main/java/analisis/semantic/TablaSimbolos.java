package analisis.semantic;

import java.util.*;

public class TablaSimbolos {
    private List<Map<String, Simbolo>> ambitos;

    public TablaSimbolos() {
        ambitos = new ArrayList<>();
        ambitos.add(new HashMap<>()); // ámbito global
    }

    public void nuevoAmbito() {
        ambitos.add(new HashMap<>());
    }

    public void cerrarAmbito() {
        if (ambitos.size() > 1) {
            ambitos.remove(ambitos.size() - 1);
        }
    }

    public boolean insertar(Simbolo s) {
        Map<String, Simbolo> actual = ambitos.get(ambitos.size() - 1);
        if (actual.containsKey(s.id)) {
            return false;
        }
        actual.put(s.id, s);
        return true;
    }

    public Simbolo buscar(String id) {
        for (int i = ambitos.size() - 1; i >= 0; i--) {
            Simbolo s = ambitos.get(i).get(id);
            if (s != null) return s;
        }
        return null;
    }

    public boolean existeEnAmbitoActual(String id) {
        return ambitos.get(ambitos.size() - 1).containsKey(id);
    }

    public List<Simbolo> obtenerTodos() {
        List<Simbolo> todos = new ArrayList<>();
        for (Map<String, Simbolo> ambito : ambitos) {
            todos.addAll(ambito.values());
        }
        return todos;
    }
}
