package usac.compi1.gui.reports;

public class GoliteError {

    private final String tipo;
    private final String descripcion;
    private final int linea;
    private final int columna;

    public GoliteError(String tipo, String descripcion, int linea, int columna) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.linea = linea;
        this.columna = columna;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    @Override
    public String toString() {
        return "Error " + tipo + ": " + descripcion + " en la linea " + linea + ", columna " + columna;
    }
}
