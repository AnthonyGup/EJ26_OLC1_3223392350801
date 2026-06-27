package usac.compi1.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import analisis.Lexer;
import analisis.ast.Programa;
import analisis.parser;
import analisis.visitor.VisitorSemantico;
import analisis.visitor.ejecucion.VisitorEjecucion;
import analisis.visitor.grafico.VisitorGrafico;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import java_cup.runtime.Symbol;
import usac.compi1.gui.reports.GoliteError;
import usac.compi1.gui.reports.ReporteDialog;
import usac.compi1.gui.reports.TokenInfo;

public class VentanaPrincipal extends JFrame {

    private final PanelEditor editor;
    private final PanelConsola consola;
    private File archivoActual;
    private Lexer lexer;
    private parser parser;
    private Programa programa;
    private List<String> erroresSemanticos;

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
        nuevo.addActionListener(e -> { editor.establecerTexto(""); archivoActual = null; });
        JMenuItem abrir = new JMenuItem("Abrir");
        abrir.addActionListener(e -> abrir());
        JMenuItem guardar = new JMenuItem("Guardar");
        guardar.addActionListener(e -> guardar());
        JMenuItem salir = new JMenuItem("Salir");
        salir.addActionListener(e -> System.exit(0));
        archivo.add(nuevo);
        archivo.add(abrir);
        archivo.add(guardar);
        archivo.add(new JSeparator());
        archivo.add(salir);
        bar.add(archivo);

        JButton btnEjecutar = crearBotonMenu("Ejecutar");
        btnEjecutar.addActionListener(e -> run());
        bar.add(btnEjecutar);

        JButton btnAST = crearBotonMenu("Ver AST");
        btnAST.addActionListener(e -> mostrarAST());
        bar.add(btnAST);

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

    private void abrir() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Archivos GoLite (*.glt)", "glt"));
        if (archivoActual != null) {
            fc.setCurrentDirectory(archivoActual.getParentFile());
            fc.setSelectedFile(archivoActual);
        }
        int opt = fc.showOpenDialog(this);
        if (opt == JFileChooser.APPROVE_OPTION) {
            archivoActual = fc.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(archivoActual))) {
                StringBuilder sb = new StringBuilder();
                String linea;
                while ((linea = br.readLine()) != null) {
                    sb.append(linea).append("\n");
                }
                editor.establecerTexto(sb.toString());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al abrir el archivo: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void guardar() {
        if (archivoActual == null) {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Archivos GoLite (*.glt)", "glt"));
            int opt = fc.showSaveDialog(this);
            if (opt != JFileChooser.APPROVE_OPTION) return;
            archivoActual = fc.getSelectedFile();
            if (!archivoActual.getName().toLowerCase().endsWith(".glt")) {
                archivoActual = new File(archivoActual.getAbsolutePath() + ".glt");
            }
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivoActual))) {
            bw.write(editor.obtenerTexto());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al guardar el archivo: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
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
            programa = (Programa) resultado.value;

            VisitorSemantico semantico = new VisitorSemantico();
            programa.accept(semantico);
            erroresSemanticos = semantico.getErrores();

            if (!erroresSemanticos.isEmpty()) {
                consola.limpiar();
                for (String err : semantico.getErrores()) {
                    consola.append(err + "\n");
                }
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

    private void mostrarAST() {
        if (programa == null) {
            JOptionPane.showMessageDialog(this, "Ejecuta el codigo primero.");
            return;
        }

        VisitorGrafico grafico = new VisitorGrafico();
        programa.accept(grafico);
        String dot = grafico.getDot();

        try {
            BufferedImage img = Graphviz.fromString(dot)
                    .width(1200)
                    .render(Format.PNG)
                    .toImage();

            JPanel panel = new JPanel() {
                private double escala = 1.0;

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY);
                    int w = (int) (img.getWidth() * escala);
                    int h = (int) (img.getHeight() * escala);
                    g2.drawImage(img, 0, 0, w, h, null);
                }

                {
                    setFocusable(true);
                    addMouseWheelListener(e -> {
                        if (e.isControlDown()) {
                            escala *= e.getWheelRotation() < 0 ? 1.15 : 1 / 1.15;
                            escala = Math.max(0.05, Math.min(20, escala));
                            setPreferredSize(new Dimension(
                                    (int) (img.getWidth() * escala),
                                    (int) (img.getHeight() * escala)));
                            revalidate();
                            repaint();
                            e.consume();
                        }
                    });
                }
            };
            panel.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));

            JScrollPane scroll = new JScrollPane(panel);
            JFrame frame = new JFrame("Arbol AST");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(scroll);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(this);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);

            consola.append("AST generado correctamente.\n");
        } catch (Exception e) {
            consola.append("Error al generar AST: " + e.getMessage() + "\n");
            e.printStackTrace();
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

        for (GoliteError e : lexer.errores) {
            filas.add(new String[]{e.getDescripcion(),
                    String.valueOf(e.getLinea()), String.valueOf(e.getColumna()), e.getTipo()});
        }
        for (GoliteError e : parser.errors) {
            filas.add(new String[]{e.getDescripcion(),
                    String.valueOf(e.getLinea()), String.valueOf(e.getColumna()), e.getTipo()});
        }
        if (erroresSemanticos != null) {
            for (String err : erroresSemanticos) {
                String desc = err;
                String linea = "";
                if (err.startsWith("Linea ")) {
                    int idx = err.indexOf(": ");
                    if (idx > 0) {
                        linea = err.substring(6, idx).trim();
                        desc = err.substring(idx + 2);
                    }
                }
                filas.add(new String[]{desc, linea, "", "Semantico"});
            }
        }

        String[] cols = {"Descripcion", "Linea", "Columna", "Tipo"};
        new ReporteDialog(this, "Reporte de errores", cols, filas);
    }
}
