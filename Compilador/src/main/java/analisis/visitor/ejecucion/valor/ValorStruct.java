package analisis.visitor.ejecucion.valor;

import java.util.Map;
import java.util.stream.Collectors;

public record ValorStruct(Map<String, Valor> campos, String nombreTipo, int linea, int columna) implements Valor {
    @Override
    public String obtenerTipoNombre() {
        return nombreTipo;
    }

    @Override
    public String toString() {
        String contenido = campos.entrySet().stream()
            .map(e -> e.getKey() + ": " + e.getValue().toString())
            .collect(Collectors.joining(", "));
        return nombreTipo + "{" + contenido + "}";
    }
}
