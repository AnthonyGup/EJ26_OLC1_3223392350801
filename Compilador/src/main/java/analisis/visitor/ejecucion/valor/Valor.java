package analisis.visitor.ejecucion.valor;

public sealed interface Valor permits ValorInt, ValorFloat, ValorBool, ValorString, ValorRune, ValorNil, ValorVoid {
    int linea();
    int columna();
    String obtenerTipoNombre();
}
