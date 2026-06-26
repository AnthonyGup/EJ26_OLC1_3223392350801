package analisis;

import analisis.ast.Programa;
import analisis.visitor.VisitorSemantico;
import analisis.visitor.ejecucion.VisitorEjecucion;
import java.io.StringReader;

public class TestStructs {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testSimpleFunc();
        testSimpleStructDef();
        testStructDefAndLiteral();
        testStructAccess();
        testStructAssign();
        testStructPrintln();
        testIntSlice();
        testStructSliceBasic();
        testStructSliceAccess();
        testStructTwoVars();
        testMissingField();
        testWrongType();
        report();
    }

    static void test(String name, String code, boolean shouldSucceed, String expectedOutput) {
        try {
            Lexer lexer = new Lexer(new StringReader(code));
            parser parser = new parser(lexer);
            java_cup.runtime.Symbol resultado = parser.parse();

            if (!(resultado.value instanceof Programa)) {
                if (shouldSucceed) {
                    System.out.println("FAIL: " + name + " - parser errors: " + parser.errors);
                    failed++;
                } else {
                    System.out.println("PASS: " + name + " (error esperado)");
                    passed++;
                }
                return;
            }

            Programa programa = (Programa) resultado.value;

            VisitorSemantico semantico = new VisitorSemantico();
            programa.accept(semantico);

            if (!semantico.getErrores().isEmpty()) {
                if (shouldSucceed) {
                    System.out.println("FAIL: " + name + " - errores semanticos: " + semantico.getErrores());
                    failed++;
                } else {
                    System.out.println("PASS: " + name + " (error esperado)");
                    passed++;
                }
                return;
            }

            if (!shouldSucceed) {
                System.out.println("FAIL: " + name + " - se esperaba error pero no hubo");
                failed++;
                return;
            }

            VisitorEjecucion ejecucion = new VisitorEjecucion();
            programa.accept(ejecucion);

            if (expectedOutput != null && !ejecucion.output.equals(expectedOutput)) {
                System.out.println("FAIL: " + name + " - se esperaba: '" + expectedOutput.replace("\n", "\\n") + "' pero se obtuvo: '" + ejecucion.output.replace("\n", "\\n") + "'");
                failed++;
            } else {
                System.out.println("PASS: " + name);
                passed++;
            }
        } catch (Exception e) {
            if (shouldSucceed) {
                System.out.println("FAIL: " + name + " - excepcion: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                e.printStackTrace(System.out);
                failed++;
            } else {
                System.out.println("PASS: " + name + " (error esperado)");
                passed++;
            }
        }
    }

    static void testSimpleFunc() {
        test("simple func main() {}", 
            "func main() {\n}\n", true, "");
    }

    static void testSimpleStructDef() {
        test("struct def only", 
            "struct Persona {\n    string nombre;\n    int edad;\n}\nfunc main() {\n}\n", true, "");
    }

    static void testStructDefAndLiteral() {
        String code = "struct Persona {\n" +
                      "    string nombre;\n" +
                      "    int edad;\n" +
                      "}\n" +
                      "func main() {\n" +
                      "    Persona p = { nombre: \"Juan\", edad: 25 }\n" +
                      "}\n";
        test("struct def + literal", code, true, "");
    }

    static void testStructAccess() {
        String code = "struct Persona {\n" +
                      "    string nombre;\n" +
                      "    int edad;\n" +
                      "}\n" +
                      "func main() {\n" +
                      "    Persona p = { nombre: \"Juan\", edad: 25 }\n" +
                      "    fmt.Println(p.nombre)\n" +
                      "}\n";
        test("struct field access", code, true, "Juan\n");
    }

    static void testStructAssign() {
        String code = "struct Persona {\n" +
                      "    string nombre;\n" +
                      "    int edad;\n" +
                      "}\n" +
                      "func main() {\n" +
                      "    Persona p = { nombre: \"Juan\", edad: 25 }\n" +
                      "    p.edad = 30\n" +
                      "    fmt.Println(p.edad)\n" +
                      "}\n";
        test("struct field assign", code, true, "30\n");
    }

    static void testStructPrintln() {
        String code = "struct Persona {\n" +
                      "    string nombre;\n" +
                      "    int edad;\n" +
                      "}\n" +
                      "func main() {\n" +
                      "    Persona p = { nombre: \"Juan\", edad: 25 }\n" +
                      "    fmt.Println(p)\n" +
                      "}\n";
        test("struct println (full)", code, true, "PERSONA{nombre: Juan, edad: 25}\n");
    }

    static void testIntSlice() {
        String code = "func main() {\n" +
                      "    []int lista = []int{ 10, 20 }\n" +
                      "    fmt.Println(len(lista))\n" +
                      "}\n";
        test("int slice basic", code, true, "2\n");
    }

    static void testStructSliceBasic() {
        String code = "struct Persona {\n" +
                      "    string nombre;\n" +
                      "    int edad;\n" +
                      "}\n" +
                      "func main() {\n" +
                      "    []Persona lista = []Persona{ Persona { nombre: \"Ana\", edad: 20 }, Persona { nombre: \"Luis\", edad: 30 } }\n" +
                      "    fmt.Println(len(lista))\n" +
                      "}\n";
        test("struct slice basic", code, true, "2\n");
    }

    static void testStructSliceAccess() {
        String code = "struct Persona {\n" +
                      "    string nombre;\n" +
                      "    int edad;\n" +
                      "}\n" +
                      "func main() {\n" +
                      "    []Persona lista = []Persona{ Persona { nombre: \"Ana\", edad: 20 }, Persona { nombre: \"Luis\", edad: 30 } }\n" +
                      "    Persona p = lista[0]\n" +
                      "    fmt.Println(p.nombre)\n" +
                      "    p = lista[1]\n" +
                      "    fmt.Println(p.edad)\n" +
                      "}\n";
        test("struct slice element access", code, true, "Ana\n30\n");
    }

    static void testStructTwoVars() {
        String code = "struct Punto {\n" +
                      "    int x;\n" +
                      "    int y;\n" +
                      "}\n" +
                      "func main() {\n" +
                      "    Punto a = { x: 1, y: 2 }\n" +
                      "    Punto b = { x: 3, y: 4 }\n" +
                      "    fmt.Println(a.x)\n" +
                      "    fmt.Println(b.y)\n" +
                      "}\n";
        test("two struct vars", code, true, "1\n4\n");
    }

    static void testMissingField() {
        String code = "struct Persona {\n" +
                      "    string nombre;\n" +
                      "    int edad;\n" +
                      "}\n" +
                      "func main() {\n" +
                      "    Persona p = { nombre: \"Juan\" }\n" +
                      "}\n";
        test("missing field error", code, false, null);
    }

    static void testWrongType() {
        String code = "struct Persona {\n" +
                      "    string nombre;\n" +
                      "    int edad;\n" +
                      "}\n" +
                      "func main() {\n" +
                      "    Persona p = { nombre: 123, edad: 25 }\n" +
                      "}\n";
        test("wrong field type error", code, false, null);
    }

    static void report() {
        System.out.println("\n=== RESULTADOS ===");
        System.out.println("Pasaron: " + passed);
        System.out.println("Fallaron: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }
}
