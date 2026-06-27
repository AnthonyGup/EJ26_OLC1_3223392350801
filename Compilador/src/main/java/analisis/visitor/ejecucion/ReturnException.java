package analisis.visitor.ejecucion;

import analisis.visitor.ejecucion.valor.Valor;

class ReturnException extends RuntimeException {
    public final Valor valor;
    public ReturnException(Valor valor) {
        this.valor = valor;
    }
}
