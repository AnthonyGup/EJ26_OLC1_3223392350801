package analisis.semantic;

public enum TipoDato {
    INT(0),
    FLOAT64(0.0),
    STRING(""),
    BOOL(false),
    RUNE(0),
    NIL(null),
    SLICE_INT(null),
    SLICE_FLOAT64(null),
    SLICE_STRING(null),
    SLICE_BOOL(null),
    SLICE_RUNE(null),
    SLICE_SLICE_INT(null),
    SLICE_SLICE_FLOAT64(null),
    SLICE_SLICE_STRING(null),
    SLICE_SLICE_BOOL(null),
    SLICE_SLICE_RUNE(null),
    STRUCT(null),
    SLICE_STRUCT(null);

    private Object valorDefecto;

    TipoDato(Object valorDefecto) {
        this.valorDefecto = valorDefecto;
    }

    public Object getValorDefecto() {
        return valorDefecto;
    }
}
