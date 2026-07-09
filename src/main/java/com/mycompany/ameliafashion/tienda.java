package com.mycompany.ameliafashion;
import java.awt.Color;                     // Para dar color a fondos, textos y bordes de los componentes
import java.sql.Connection;                // Representa la conexión activa con la base de datos MySQL
import java.sql.DriverManager;             // Se encarga de crear la conexión usando la URL, usuario y contraseña
import java.sql.PreparedStatement;         // Ejecuta consultas SQL con parámetros (?) de forma segura, evita inyección SQL
import java.sql.ResultSet;                 // Guarda el resultado de una consulta SELECT (las filas que regresa la BD)
import java.sql.Statement;                 // Ejecuta consultas SQL simples, sin parámetros
import javax.swing.BorderFactory;          // Crea los bordes de las tarjetas de producto (líneas, márgenes internos)
import javax.swing.BoxLayout;              // Acomoda los elementos de la tarjeta uno debajo del otro
import javax.swing.ImageIcon;              // Carga y muestra imágenes (como el logo de la tienda)
import javax.swing.JButton;                // Botones, como "Agregar al carrito", "eliminar" y "pagar"
import javax.swing.JLabel;                 // Textos que se muestran en pantalla (nombre, precio, stock)
import javax.swing.JOptionPane;            // Ventanas emergentes de aviso o confirmación (ej. confirmar el pago)
import javax.swing.JPanel;                 // Contenedor donde se agrupan los elementos de cada tarjeta de producto
import javax.swing.table.DefaultTableModel; // Maneja los datos de la tabla del carrito (agregar, quitar, editar filas)

public class tienda extends javax.swing.JFrame {

    // Datos de conexión en un solo lugar 
    private String nombreUsuario;
    private static final String BD_URL = "jdbc:mysql://localhost:3306/amelia_fashion";
    private static final String BD_USUARIO = "root";
    private static final String BD_PASSWORD = "";

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(tienda.class.getName());

    public tienda() {
        this(null);
    }
    public tienda(String usuario) {
        this.nombreUsuario = usuario;
        initComponents();
        configurarVentana();
        cargarProductos();
    }

 
    private void mostrarUsuario() {
        System.out.println("Usuario recibido: " + nombreUsuario); // Debug
        if (nombreUsuario != null && !nombreUsuario.isEmpty()) {
            labelUsuario.setText("" + nombreUsuario);
        } else {
            labelUsuario.setText("Usuario");
        }
        // Forzar actualización visual
        labelUsuario.repaint();
        labelUsuario.revalidate();
    }
    private void configurarVentana() {
        ImageIcon logo = new ImageIcon(getClass().getResource("/imagenes/logo.jpeg"));
        Jlabel.setIcon(new ImageIcon(logo.getImage().getScaledInstance(139, 42, java.awt.Image.SCALE_SMOOTH)));
        getContentPane().setBackground(new Color(247, 244, 242));

        jTable1.setDefaultEditor(Object.class, null);
        ((DefaultTableModel) jTable1.getModel()).setRowCount(0); 
        actualizarTotal();

        
        jPanelProductos.setLayout(new java.awt.GridLayout(0, 2, 15, 15));
        jScrollPane2.setViewportView(jPanelProductos);

        jeliminar.addActionListener(e -> eliminarDelCarrito());
        btnpagar.addActionListener(e -> pagarCarrito());
        mostrarUsuario();
    }

    /** Abre una conexión nueva a MySQL. Se usa en cargarProductos() y pagarCarrito(). */
    private Connection conectarBD() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(BD_URL, BD_USUARIO, BD_PASSWORD);
    }

    // ---------------------- PRODUCTOS ----------------------
    private void cargarProductos() {
        jPanelProductos.removeAll();
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
                BorderFactory.createLineBorder(new Color(225, 225, 225), 1, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        tarjeta.add(etiqueta(nombre, 14, Color.BLACK, true));
        tarjeta.add(etiqueta(stock > 0 ? "Disponibles: " + stock : "Sin stock",
                11, stock > 0 ? Color.GRAY : Color.RED, false));
        tarjeta.add(etiqueta(String.format("$%.2f", precio), 15, new Color(0, 153, 0), true));

        JButton btnAgregar = new JButton("Agregar al carrito");
        btnAgregar.setEnabled(stock > 0);
        btnAgregar.addActionListener(e -> agregarAlCarrito(nombre, precio));
        tarjeta.add(btnAgregar);

        return tarjeta;
    }
    private JLabel etiqueta(String texto, int tamanio, Color color, boolean negrita) {
        JLabel label = new JLabel(texto);
        label.setFont(new java.awt.Font("SansSerif", negrita ? java.awt.Font.BOLD : java.awt.Font.PLAIN, tamanio));
        label.setForeground(color);
        label.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        return label;
    }

    // ---------------------- CARRITO ----------------------

    /** Agrega un producto al carrito. Si ya estaba, solo le suma 1 a la cantidad. */
    private void agregarAlCarrito(String nombre, double precio) {
        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();

        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (nombre.equals(modelo.getValueAt(i, 0))) {
                int cantidad = Integer.parseInt(modelo.getValueAt(i, 1).toString()) + 1;
                modelo.setValueAt(cantidad, i, 1);
                modelo.setValueAt(precio * cantidad, i, 3);
                actualizarTotal();
                return;
            }
        }
        modelo.addRow(new Object[]{nombre, 1, precio, precio});
        actualizarTotal();
    }
    private void eliminarDelCarrito() {
        int fila = jTable1.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto para eliminarlo.");
            return;
        }
        ((DefaultTableModel) jTable1.getModel()).removeRow(fila);
        actualizarTotal();
    }

    private void pagarCarrito() {
        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
        if (modelo.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío.");
            return;
        }

        int confirmar = JOptionPane.showConfirmDialog(this,
                "Total a pagar: " + txtTotal.getText() + "\n¿Confirmar pago?",
                "Confirmar pago", JOptionPane.YES_NO_OPTION);
        if (confirmar != JOptionPane.YES_OPTION) return;

        String sql = "UPDATE productos SET cantidad = cantidad - ? WHERE nombre = ? AND cantidad >= ?";
        try (Connection con = conectarBD()) {
            for (int i = 0; i < modelo.getRowCount(); i++) {
                int cantidad = Integer.parseInt(modelo.getValueAt(i, 1).toString());
                try (PreparedStatement st = con.prepareStatement(sql)) {
                    st.setInt(1, cantidad);
                    st.setString(2, modelo.getValueAt(i, 0).toString());
                    st.setInt(3, cantidad);
                    st.executeUpdate();
                }
            }
            JOptionPane.showMessageDialog(this, "¡Pago realizado con éxito!");
            modelo.setRowCount(0);
            actualizarTotal();
            cargarProductos(); // refresca el stock que se ve en las tarjetas
        } catch (Exception ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Error al procesar el pago.");
        }
    }
    private void actualizarTotal() {
        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
        double total = 0;
        for (int i = 0; i < modelo.getRowCount(); i++) {
            try {
                total += Double.parseDouble(modelo.getValueAt(i, 3).toString());
            } catch (Exception ignored) {
                // fila vacía o con dato raro, se ignora
            }
        }
        txtTotal.setText(String.format("$%.2f", total));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Jlabel = new javax.swing.JLabel();
        labelUsuario = new javax.swing.JLabel();
        Salir = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        txtTotal = new javax.swing.JLabel();
        jeliminar = new javax.swing.JButton();
        btnpagar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jPanelProductos = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        Jlabel.setMaximumSize(new java.awt.Dimension(367, 86));
        Jlabel.setMinimumSize(new java.awt.Dimension(367, 86));

        labelUsuario.setText(".");

        Salir.setText("Cerrar Sesión");
        Salir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                SalirMouseClicked(evt);
            }
        });
        Salir.addActionListener(this::SalirActionPerformed);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel2.setFont(new java.awt.Font("Pristina", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 153, 0));
        jLabel2.setText("Carrito");
        jLabel2.setName(""); // NOI18N

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "producto", "cant.", "precio", "Total"
            }
        ));
        jTable1.addPropertyChangeListener(this::jTable1PropertyChange);
        jScrollPane1.setViewportView(jTable1);

        jLabel3.setFont(new java.awt.Font("Pristina", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 153, 0));
        jLabel3.setText("Total:");

        jeliminar.setText("eliminar");

        btnpagar.setText("pagar");

        jPanelProductos.setLayout(new java.awt.GridLayout(1, 0));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jPanelProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(449, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jPanelProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(269, Short.MAX_VALUE))
        );

        jScrollPane2.setViewportView(jPanel1);

        jLabel1.setText("Usuario:");

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
                                .addContainerGap()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jeliminar, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
                                        .addComponent(btnpagar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel2)
                                .addGap(91, 91, 91)))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 476, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnpagar)
                        .addGap(18, 18, 18)
                        .addComponent(jeliminar))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 456, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void SalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SalirActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SalirActionPerformed

    private void SalirMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SalirMouseClicked
      Inicio I=new Inicio();
        I.setVisible(true);
       this.setVisible(false);
    }//GEN-LAST:event_SalirMouseClicked

    private void jTable1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTable1PropertyChange
    
    }//GEN-LAST:event_jTable1PropertyChange

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
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
        java.awt.EventQueue.invokeLater(() -> new tienda().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Jlabel;
    private javax.swing.JButton Salir;
    private javax.swing.JButton btnpagar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelProductos;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTable jTable1;
    private javax.swing.JButton jeliminar;
    private javax.swing.JLabel labelUsuario;
    private javax.swing.JLabel txtTotal;
    // End of variables declaration//GEN-END:variables
}
