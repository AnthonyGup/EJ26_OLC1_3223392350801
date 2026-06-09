package analisis.visitor.ejecucion.valor;

public record ValorBool(boolean valor, int linea, int columna) implements Valor {
    @Override
    public String obtenerTipoNombre() {
        return "bool";
    }

    @Override
    public String toString() {
        return String.valueOf(valor);
    }
}
