package analisis;

import usac.compi1.gui.VentanaPrincipal;
import javax.swing.SwingUtilities;

public class Compilador {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VentanaPrincipal();
        });
    }
}
