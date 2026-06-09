package analisis.visitor.ejecucion.valor;

public record ValorFloat(double valor, int linea, int columna) implements Valor {
    @Override
    public String obtenerTipoNombre() {
        return "float64";
    }

    @Override
    public String toString() {
        if (valor == Math.floor(valor)) {
            return String.format("%.1f", valor);
        }
        return String.valueOf(valor);
    }
}
