package analisis.ast;

import analisis.visitor.Visitor;
import java.util.List;

public class LlamadaFuncion extends NodoAST {
    public String paquete;           // "fmt", "strconv", "reflect"
    public String funcion;           // "Println", "Atoi", "ParseFloat", "TypeOf"
    public boolean llamarString;     // true si se aplica .String() (reflect.TypeOf().String())
    public List<NodoAST> argumentos;

    // Constructor normal: fmt.Println(...), strconv.Atoi(...), strconv.ParseFloat(...)
    public LlamadaFuncion(String paquete, String funcion, List<NodoAST> argumentos, int linea, int columna) {
        super(linea, columna);
        this.paquete = paquete;
        this.funcion = funcion;
        this.argumentos = argumentos;
        this.llamarString = false;
    }

    // Constructor para reflect.TypeOf().String()
    public LlamadaFuncion(String paquete, String funcion, List<NodoAST> argumentos, boolean llamarString, int linea, int columna) {
        super(linea, columna);
        this.paquete = paquete;
        this.funcion = funcion;
        this.argumentos = argumentos;
        this.llamarString = llamarString;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
