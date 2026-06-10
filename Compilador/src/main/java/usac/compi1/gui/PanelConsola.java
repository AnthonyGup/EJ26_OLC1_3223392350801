package usac.compi1.gui;

import javax.swing.*;
import java.awt.*;

public class PanelConsola extends JScrollPane {

    private final JTextArea area;

    public PanelConsola() {
        area = new JTextArea(10, 60);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBackground(new Color(40, 40, 40));
        area.setForeground(new Color(200, 200, 200));
        area.setCaretColor(new Color(200, 200, 200));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        area.setText("CONSOLA  -  LABORATORIO DE ORGANIZACION DE LENGUAJES Y COMPILADORES 1\n\n");
        getViewport().setView(area);
    }

    public void append(String texto) {
        SwingUtilities.invokeLater(() -> {
            area.append(texto);
            area.setCaretPosition(area.getDocument().getLength());
        });
    }

    public void limpiar() {
        area.setText("CONSOLA  -  LABORATORIO DE ORGANIZACION DE LENGUAJES Y COMPILADORES 1\n\n");
    }
}
