package usac.compi1.gui;

import analisis.Lexer;
import analisis.parser;
import analisis.ast.Programa;
import analisis.visitor.VisitorSemantico;
import analisis.visitor.ejecucion.VisitorEjecucion;
import usac.compi1.gui.reports.GoliteError;
import usac.compi1.gui.reports.ReporteDialog;
import usac.compi1.gui.reports.TokenInfo;
import java_cup.runtime.Symbol;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.StringReader;
import java.util.List;
import java.util.ArrayList;

public class VentanaPrincipal extends JFrame {

    private final PanelEditor editor;
    private final PanelConsola consola;
    private Lexer lexer;
    private parser parser;

    public VentanaPrincipal() {
        super("Golite");
        editor = new PanelEditor();
        consola = new PanelConsola();

        iniciarComponentes();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 400));
        setSize(1200, 675);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void iniciarComponentes() {
        setJMenuBar(crearMenuBar());

        JPanel mainPanel = new JPanel(new BorderLayout());
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editor, consola);
        split.setResizeWeight(0.67);
        split.setContinuousLayout(true);
        split.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(split, BorderLayout.CENTER);

        mainPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                split.setDividerLocation((int) (mainPanel.getHeight() * 0.67));
                mainPanel.removeComponentListener(this);
            }
        });

        add(mainPanel);
    }

    private JMenuBar crearMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu archivo = new JMenu("Archivo");
        JMenuItem nuevo = new JMenuItem("Nuevo");
        nuevo.addActionListener(e -> editor.establecerTexto(""));
        JMenuItem salir = new JMenuItem("Salir");
        salir.addActionListener(e -> System.exit(0));
        archivo.add(nuevo);
        archivo.add(new JSeparator());
        archivo.add(salir);
        bar.add(archivo);

        JButton btnEjecutar = crearBotonMenu("Ejecutar");
        btnEjecutar.addActionListener(e -> run());
        bar.add(btnEjecutar);

        JMenu reportes = new JMenu("Reportes");
        JMenuItem rptTokens = new JMenuItem("Reporte de tokens");
        rptTokens.addActionListener(e -> mostrarTokens());
        JMenuItem rptErrores = new JMenuItem("Reporte de errores");
        rptErrores.addActionListener(e -> errores());
        reportes.add(rptTokens);
        reportes.add(rptErrores);
        bar.add(reportes);

        JButton btnLimpiar = crearBotonMenu("Limpiar consola");
        btnLimpiar.addActionListener(e -> consola.limpiar());
        bar.add(btnLimpiar);

        JMenu ayuda = new JMenu("Ayuda");
        JMenuItem acercaDe = new JMenuItem("Acerca de");
        acercaDe.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "GoLite Compiler - Fase 1\nLaboratorio de Organizacion de Lenguajes y Compiladores 1",
                "Acerca de", JOptionPane.INFORMATION_MESSAGE));
        ayuda.add(acercaDe);
        bar.add(ayuda);

        return bar;
    }

    private JButton crearBotonMenu(String texto) {
        JButton btn = new JButton(texto);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                btn.setBackground(Color.LIGHT_GRAY);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                btn.setBackground(new JButton().getBackground());
            }
        });
        return btn;
    }

    private void run() {
        String codigo = editor.obtenerTexto();
        if (codigo.isBlank()) {
            consola.limpiar();
            consola.append("No hay codigo para compilar.\n");
            return;
        }

        try {
            lexer = new Lexer(new StringReader(codigo));
            parser = new parser(lexer);
            Symbol resultado = parser.parse();
            Programa programa = (Programa) resultado.value;

            VisitorSemantico semantico = new VisitorSemantico();
            programa.accept(semantico);

            if (!semantico.getErrores().isEmpty()) {
                consola.limpiar();
                consola.append("Errores semanticos encontrados. Revisa el Reporte de errores.\n");
                return;
            }

            VisitorEjecucion ejecucion = new VisitorEjecucion();
            programa.accept(ejecucion);

            String salida = ejecucion.output;
            consola.limpiar();
            consola.append(salida);

        } catch (Exception e) {
            consola.limpiar();
            String msg = e.getMessage();

            if (msg == null || msg.isBlank()) {
                msg = "Error interno: " + e.getClass().getSimpleName();
            } else if (msg.contains("Couldn't repair")) {
                msg = "Error de sintaxis: no se pudo recuperar el analisis";
            } else if (msg.contains("/ by zero") || msg.contains("Divide by zero")) {
                msg = "Error: division por cero";
            }

            if (parser != null && !parser.errors.isEmpty()) {
                consola.append("Errores sintacticos encontrados. Revisa el Reporte de errores.\n");
            } else {
                consola.append("Error: " + msg + "\n");
            }
        }
    }

    private void mostrarTokens() {
        if (lexer == null) {
            JOptionPane.showMessageDialog(this, "Ejecuta el codigo primero.");
            return;
        }
        List<TokenInfo> tokens = lexer.tokens;
        List<String[]> filas = new ArrayList<>();
        int i = 1;
        for (TokenInfo t : tokens) {
            filas.add(new String[]{String.valueOf(i), t.getLexema(), t.getTipo(),
                    String.valueOf(t.getLinea()), String.valueOf(t.getColumna())});
            i++;
        }
        String[] cols = {"No.", "Lexema", "Tipo", "Linea", "Columna"};
        new ReporteDialog(this, "Reporte de tokens", cols, filas);
    }

    private void errores() {
        if (lexer == null || parser == null) {
            JOptionPane.showMessageDialog(this, "Ejecuta el codigo primero.");
            return;
        }

        List<String[]> filas = new ArrayList<>();
        int i = 1;

        for (GoliteError e : lexer.errores) {
            filas.add(new String[]{String.valueOf(i), e.getDescripcion(),
                    String.valueOf(e.getLinea()), String.valueOf(e.getColumna()), e.getTipo()});
            i++;
        }
        for (GoliteError e : parser.errors) {
            filas.add(new String[]{String.valueOf(i), e.getDescripcion(),
                    String.valueOf(e.getLinea()), String.valueOf(e.getColumna()), e.getTipo()});
            i++;
        }

        String[] cols = {"No.", "Descripcion", "Linea", "Columna", "Tipo"};
        new ReporteDialog(this, "Reporte de errores", cols, filas);
    }
}
