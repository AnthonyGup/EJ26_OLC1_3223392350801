package usac.compi1.gui.reports;

public class TokenInfo {

    private final String lexema;
    private final String tipo;
    private final int linea;
    private final int columna;

    public TokenInfo(String lexema, String tipo, int linea, int columna) {
        this.lexema = lexema;
        this.tipo = tipo;
        this.linea = linea;
        this.columna = columna;
    }

    public String getLexema() {
        return lexema;
    }

    public String getTipo() {
        return tipo;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }
}
