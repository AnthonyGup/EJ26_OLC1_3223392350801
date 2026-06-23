package usac.compi1.gui.reports;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class ReporteDialog extends JDialog {

    public ReporteDialog(JFrame parent, String titulo, String[] columnas, List<String[]> filas) {
        super(parent, titulo, true);
        this.setResizable(true);
        setSize(800, 400);
        setLocationRelativeTo(parent);

        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        for (String[] fila : filas) {
            modelo.addRow(fila);
        }

        JTable tabla = new JTable(modelo);
        tabla.setFont(new Font("Monospaced", Font.PLAIN, 13));
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        tabla.setRowHeight(22);
        tabla.setFillsViewportHeight(true);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        for (int c = 0; c < tabla.getColumnCount(); c++) {
            String nombre = columnas[c];
            var col = tabla.getColumnModel().getColumn(c);
            switch (nombre) {
                case "No." -> { col.setPreferredWidth(35); col.setMaxWidth(45); }
                case "Linea", "Columna" -> { col.setPreferredWidth(55); col.setMaxWidth(70); }
                case "Tipo" -> { col.setPreferredWidth(80); col.setMaxWidth(100); }
            }
        }

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private String tip;

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                tip = (value != null) ? value.toString() : null;
                return super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
            }

            @Override
            public String getToolTipText(MouseEvent event) {
                return tip;
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scroll);

        setVisible(true);
    }
}
