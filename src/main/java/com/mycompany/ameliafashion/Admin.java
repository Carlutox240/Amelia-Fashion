/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.ameliafashion;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author criss
 */
public class Admin extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Admin.class.getName());
    private static final String BD_URL = "jdbc:mysql://localhost:3306/amelia_fashion";
    private static final String BD_USUARIO = "root";
    private static final String BD_PASSWORD = "";

    // --- Selección de producto ---
    private static final Color COLOR_BORDE_NORMAL = new Color(225, 225, 225);
    private static final Color COLOR_BORDE_SELECCIONADO = new Color(51, 122, 255);
    private static final Color COLOR_FONDO_SELECCIONADO = new Color(232, 240, 255);

    private JPanel tarjetaSeleccionada = null;
    private String productoSeleccionadoNombre = null;
    private double productoSeleccionadoPrecio = 0;
    private int productoSeleccionadoStock = 0;

    public Admin() {
        initComponents();
        cargarProductos(); // <-- FIX 1: ahora sí se llama al método que carga los productos
    }

    private Connection conectarBD() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(BD_URL, BD_USUARIO, BD_PASSWORD);
    }
    
    private void cargarProductos() {
        jPanelProductos.removeAll();
        tarjetaSeleccionada = null;
        productoSeleccionadoNombre = null;
        String sql = "SELECT nombre, precio, cantidad FROM productos";

        try (Connection con = conectarBD();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                jPanelProductos.add(crearTarjeta(
                        rs.getString("nombre"), rs.getDouble("precio"), rs.getInt("cantidad")));
            }
        } catch (Exception ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los productos.");
        }

        jPanelProductos.revalidate();
        jPanelProductos.repaint();
    }

    /** Crea la tarjeta de un producto. */
    private JPanel crearTarjeta(String nombre, double precio, int stock) {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE_NORMAL, 1, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        tarjeta.add(etiqueta(nombre, 14, Color.BLACK, true));
        tarjeta.add(etiqueta(stock > 0 ? "Disponibles: " + stock : "Sin stock",
                11, stock > 0 ? Color.GRAY : Color.RED, false));
        tarjeta.add(etiqueta(String.format("$%.2f", precio), 15, new Color(0, 153, 0), true));

        // Hace la tarjeta seleccionable con un clic
        tarjeta.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        tarjeta.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                seleccionarTarjeta(tarjeta, nombre, precio, stock);
            }
        });

        return tarjeta;
    }

    /** Marca visualmente la tarjeta elegida y guarda los datos del producto seleccionado. */
    private void seleccionarTarjeta(JPanel tarjeta, String nombre, double precio, int stock) {
        // Quita el resaltado de la tarjeta previamente seleccionada
        if (tarjetaSeleccionada != null) {
            tarjetaSeleccionada.setBackground(Color.WHITE);
            tarjetaSeleccionada.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_BORDE_NORMAL, 1, true),
                    BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        }

        // Si se hace clic sobre la misma tarjeta ya seleccionada, se deselecciona
        if (tarjetaSeleccionada == tarjeta) {
            tarjetaSeleccionada = null;
            productoSeleccionadoNombre = null;
            return;
        }

        // Resalta la nueva tarjeta seleccionada
        tarjeta.setBackground(COLOR_FONDO_SELECCIONADO);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE_SELECCIONADO, 2, true),
                BorderFactory.createEmptyBorder(11, 11, 11, 11)));

        tarjetaSeleccionada = tarjeta;
        productoSeleccionadoNombre = nombre;
        productoSeleccionadoPrecio = precio;
        productoSeleccionadoStock = stock;
    }

    private JLabel etiqueta(String texto, int tamanio, Color color, boolean negrita) {
        JLabel label = new JLabel(texto);
        label.setFont(new java.awt.Font("SansSerif", negrita ? java.awt.Font.BOLD : java.awt.Font.PLAIN, tamanio));
        label.setForeground(color);
        label.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        return label;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     *
     * NOTA IMPORTANTE: Este método normalmente lo regenera el editor de formularios
     * de NetBeans (Design view). Si abres este .java en NetBeans y guardas cambios
     * desde la vista de diseño, es MUY probable que sobreescriba las modificaciones
     * de jPanelProductos / jScrollPane2 que se hicieron aquí a mano.
     *
     * Para que el cambio sea permanente y "sobreviva" al editor visual, lo ideal es
     * hacer los ajustes de layout DESDE el Design view de NetBeans:
     *   1. Selecciona jPanelProductos en el árbol de componentes.
     *   2. En el Layout de jPanelProductos, cámbialo a GridLayout con 0 filas, 1 columna.
     *   3. Elimina jPanel1 del árbol (clic derecho > Delete) y arrastra jPanelProductos
     *      directo dentro de jScrollPane2 como su viewport.
     * Si no usas el Design view y compilas/ejecutas este .java tal cual (por ejemplo
     * con Maven/Gradle o javac), el código de abajo funciona sin problema.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        Salir = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        Jlabel = new javax.swing.JLabel();
        labelUsuario = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanelProductos = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        Salir.setBackground(new java.awt.Color(204, 204, 204));
        Salir.setFont(new java.awt.Font("Poor Richard", 1, 14)); // NOI18N
        Salir.setText("Cerrar Sesión");
        Salir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                SalirMouseClicked(evt);
            }
        });
        Salir.addActionListener(this::SalirActionPerformed);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel1.setText("Admin:");

        Jlabel.setMaximumSize(new java.awt.Dimension(367, 86));
        Jlabel.setMinimumSize(new java.awt.Dimension(367, 86));

        labelUsuario.setText(".");

        // FIX 2: GridLayout(0, 1, ...) = filas ilimitadas, 1 columna -> las tarjetas
        // se apilan verticalmente y el panel crece con el contenido real.
        jPanelProductos.setLayout(new java.awt.GridLayout(0, 1, 8, 8));

        // FIX 3: el scroll ahora envuelve directamente a jPanelProductos, ya no a
        // jPanel1 (que tenía un tamaño fijo por GroupLayout y por eso nunca
        // reflejaba el contenido agregado dinámicamente).
        jScrollPane2.setViewportView(jPanelProductos);

        jButton1.setBackground(new java.awt.Color(102, 255, 102));
        jButton1.setFont(new java.awt.Font("Pristina", 1, 24)); // NOI18N
        jButton1.setForeground(new java.awt.Color(51, 153, 0));
        jButton1.setText("Agregar");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });

        jButton4.setBackground(new java.awt.Color(255, 255, 102));
        jButton4.setFont(new java.awt.Font("Pristina", 1, 24)); // NOI18N
        jButton4.setForeground(new java.awt.Color(153, 153, 0));
        jButton4.setText("Actualizar");

        jButton5.setBackground(new java.awt.Color(255, 102, 102));
        jButton5.setFont(new java.awt.Font("Pristina", 1, 24)); // NOI18N
        jButton5.setForeground(new java.awt.Color(153, 0, 0));
        jButton5.setText("Borrar");
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton5MouseClicked(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(204, 204, 204));
        jButton2.setFont(new java.awt.Font("Poor Richard", 1, 24)); // NOI18N
        jButton2.setText("Registro de ventas");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(Jlabel, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Salir))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGap(14, 14, 14))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15)))
                .addGap(21, 21, 21))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(labelUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(Salir)
                        .addComponent(jLabel1))
                    .addComponent(Jlabel, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 476, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 456, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(109, 109, 109)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );

        pack();
    }// </editor-fold>                        

    private void SalirMouseClicked(java.awt.event.MouseEvent evt) {                                   
        Inicio I=new Inicio();
        I.setVisible(true);
        this.setVisible(false);
    }                                  

    private void SalirActionPerformed(java.awt.event.ActionEvent evt) {                                      
        // TODO add your handling code here:
    }                                     

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {                                      
        // TODO add your handling code here:zzz
        PAgregar PA=new PAgregar();
        PA.setVisible(true);
        this.setVisible(false);
    }                                     

    private void jButton5MouseClicked(java.awt.event.MouseEvent evt) {
        if (productoSeleccionadoNombre == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto primero.");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Seguro que quieres borrar \"" + productoSeleccionadoNombre + "\"?",
                "Confirmar borrado", JOptionPane.YES_NO_OPTION);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM productos WHERE nombre = ?";

        try (Connection con = conectarBD();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, productoSeleccionadoNombre);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Producto borrado correctamente.");
            cargarProductos(); // recarga la lista para que ya no aparezca

        } catch (Exception ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "No se pudo borrar el producto.");
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Admin().setVisible(true));
    }

    // Variables declaration - do not modify                     
    private javax.swing.JLabel Jlabel;
    private javax.swing.JButton Salir;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanelProductos;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel labelUsuario;
    // End of variables declaration                   
}
