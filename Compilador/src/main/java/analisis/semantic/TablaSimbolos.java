package analisis.semantic;

import java.util.*;

public class TablaSimbolos {
    private final Deque<Map<String, Simbolo>> pila;

    public TablaSimbolos() {
        pila = new ArrayDeque<>();
        pila.push(new HashMap<>());
    }

    public void nuevoAmbito() {
        pila.push(new HashMap<>());
    }

    public void cerrarAmbito() {
        if (pila.size() > 1) {
            pila.pop();
        }
    }

    public boolean insertar(Simbolo s) {
        Map<String, Simbolo> actual = pila.peek();
        if (actual.containsKey(s.id)) {
            return false;
        }
        actual.put(s.id, s);
        return true;
    }

    public Simbolo buscar(String id) {
        for (Map<String, Simbolo> ambito : pila) {
            Simbolo s = ambito.get(id);
            if (s != null) return s;
        }
        return null;
    }

    public boolean existeEnAmbitoActual(String id) {
        return pila.peek().containsKey(id);
    }

    public int getNivelAmbito() {
        return pila.size() - 1;
    }

    public List<Simbolo> obtenerTodos() {
        List<Simbolo> todos = new ArrayList<>();
        for (Map<String, Simbolo> ambito : pila) {
            todos.addAll(ambito.values());
        }
        return todos;
    }
}
