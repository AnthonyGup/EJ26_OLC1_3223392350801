/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package usac.compi1;

import analisis.Lexer;
import analisis.parser;
import java.io.BufferedReader;
import java.io.StringReader;

/**
 *
 * @author Anthony
 */
public class Compilador {

    public static void main(String[] args) {
        try {
            String texto = "imprimir(1+2+3);imprimir(-1+2*2+9/4);";
            Lexer s = new Lexer(new BufferedReader(new StringReader(texto)));
            parser p = new parser(s);
            var resultado = p.parse().value;
            System.out.println(resultado);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
