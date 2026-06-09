package analisis.visitor.ejecucion.valor;

public record ValorInt(int valor, int linea, int columna) implements Valor {
    @Override
    public String obtenerTipoNombre() {
        return "int";
    }

    @Override
    public String toString() {
        return String.valueOf(valor);
    }
}
