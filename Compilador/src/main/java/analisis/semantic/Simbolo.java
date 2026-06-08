package analisis.semantic;

public class Simbolo {
    public String id;
    public TipoDato tipo;
    public Object valor;
    public int linea;
    public int columna;
    public String ambito;

    public Simbolo(String id, TipoDato tipo, Object valor, int linea, int columna, String ambito) {
        this.id = id;
        this.tipo = tipo;
        this.valor = valor;
        this.linea = linea;
        this.columna = columna;
        this.ambito = ambito;
    }

    public Simbolo(String id, TipoDato tipo, int linea, int columna, String ambito) {
        this(id, tipo, null, linea, columna, ambito);
    }
}
