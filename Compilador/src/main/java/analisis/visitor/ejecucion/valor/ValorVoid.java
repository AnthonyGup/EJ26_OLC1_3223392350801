package analisis.visitor.ejecucion.valor;

public record ValorVoid(int linea, int columna) implements Valor {
    @Override
    public String obtenerTipoNombre() {
        return "void";
    }

    @Override
    public String toString() {
        return "void";
    }
}
