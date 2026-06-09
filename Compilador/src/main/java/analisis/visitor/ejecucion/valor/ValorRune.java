package analisis.visitor.ejecucion.valor;

public record ValorRune(int valor, int linea, int columna) implements Valor {
    @Override
    public String obtenerTipoNombre() {
        return "rune";
    }

    @Override
    public String toString() {
        return String.valueOf((char) valor);
    }
}
