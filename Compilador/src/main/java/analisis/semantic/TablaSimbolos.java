package analisis.semantic;

import analisis.ast.Parametro;
import analisis.ast.stm.FuncDef;
import analisis.ast.stm.StructDef;
import analisis.ast.stm.StructMethodDef;
import java.util.*;

public class TablaSimbolos {
    private final Deque<Map<String, Simbolo>> pila;
    private final Map<String, List<StructDef.Campo>> structs;
    private final Map<String, Map<String, StructMethodDef>> structMethods;
    private final Map<String, FuncDef> funciones;

    public TablaSimbolos() {
        pila = new ArrayDeque<>();
        pila.push(new HashMap<>());
        structs = new HashMap<>();
        structMethods = new HashMap<>();
        funciones = new HashMap<>();
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

    public void definirMetodo(String structName, StructMethodDef metodo) {
        structMethods.computeIfAbsent(structName.toUpperCase(), k -> new HashMap<>()).put(metodo.getNombre().toUpperCase(), metodo);
    }

    public StructMethodDef buscarMetodo(String structName, String methodName) {
        Map<String, StructMethodDef> metodos = structMethods.get(structName.toUpperCase());
        if (metodos == null) return null;
        return metodos.get(methodName.toUpperCase());
    }

    public void definirFuncion(String nombre, FuncDef func) {
        funciones.put(nombre.toUpperCase(), func);
    }

    public FuncDef buscarFuncion(String nombre) {
        return funciones.get(nombre.toUpperCase());
    }

    public boolean existeFuncion(String nombre) {
        return funciones.containsKey(nombre.toUpperCase());
    }
}
