package analisis.visitor.ejecucion.valor;

public record ValorNil(int linea, int columna) implements Valor {
    @Override
    public String obtenerTipoNombre() {
        return "nil";
    }

    @Override
    public String toString() {
        return "nil";
    }
}
