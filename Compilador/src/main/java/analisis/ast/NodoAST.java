package analisis.ast;

import analisis.visitor.Visitor;

public interface NodoAST {
    <T> T accept(Visitor<T> visitor);
}
