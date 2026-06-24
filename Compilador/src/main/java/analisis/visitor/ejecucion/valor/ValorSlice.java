package analisis.visitor.ejecucion.valor;

import java.util.List;
import java.util.stream.Collectors;

public record ValorSlice(List<Valor> elementos, String tipoElemento, int linea, int columna) implements Valor {
    @Override
    public String obtenerTipoNombre() {
        return "[]" + tipoElemento;
    }

    @Override
    public String toString() {
        return "[" + elementos.stream().map(Valor::toString).collect(Collectors.joining(", ")) + "]";
    }
}
