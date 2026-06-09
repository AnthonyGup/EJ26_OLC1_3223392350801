package analisis.visitor.ejecucion.valor;

public record ValorString(String valor, int linea, int columna) implements Valor {
    @Override
    public String obtenerTipoNombre() {
        return "string";
    }

    @Override
    public String toString() {
        return valor;
    }
}
