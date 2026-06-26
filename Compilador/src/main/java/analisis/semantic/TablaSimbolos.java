package analisis.semantic;

import analisis.ast.stm.StructDef;
import java.util.*;

public class TablaSimbolos {
    private final Deque<Map<String, Simbolo>> pila;
    private final Map<String, List<StructDef.Campo>> structs;

    public TablaSimbolos() {
        pila = new ArrayDeque<>();
        pila.push(new HashMap<>());
        structs = new HashMap<>();
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

    public void definirStruct(String nombre, List<StructDef.Campo> campos) {
        structs.put(nombre, campos);
    }

    public List<StructDef.Campo> buscarStruct(String nombre) {
        return structs.get(nombre);
    }

    public boolean existeStruct(String nombre) {
        return structs.containsKey(nombre);
    }
}
