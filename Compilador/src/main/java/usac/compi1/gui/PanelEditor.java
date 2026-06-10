package usac.compi1.gui;

import javax.swing.*;
import javax.swing.event.CaretListener;
import java.awt.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public class PanelEditor extends JPanel {

    private final RSyntaxTextArea area;
    private final JLabel statusBar;

    public PanelEditor() {
        setLayout(new BorderLayout());

        area = new RSyntaxTextArea(25, 60);
        area.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GO);
        area.setCodeFoldingEnabled(true);
        area.setAntiAliasingEnabled(true);
        area.setAutoIndentEnabled(true);
        area.setTabSize(4);
        area.setCaretPosition(0);

        RTextScrollPane scroll = new RTextScrollPane(area);
        add(scroll, BorderLayout.CENTER);

        statusBar = new JLabel("Lin: 1, Col: 1");
        statusBar.setHorizontalAlignment(SwingConstants.RIGHT);
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        add(statusBar, BorderLayout.SOUTH);

        area.addCaretListener((CaretListener) e -> {
            int linea = area.getCaretLineNumber() + 1;
            int columna = area.getCaretOffsetFromLineStart() + 1;
            statusBar.setText("Lin: " + linea + ", Col: " + columna);
        });
    }

    public String obtenerTexto() {
        return area.getText();
    }

    public void establecerTexto(String texto) {
        area.setText(texto);
        area.setCaretPosition(0);
    }

    public void limpiar() {
        area.setText("");
    }
}
