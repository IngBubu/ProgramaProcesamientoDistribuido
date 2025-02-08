import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class MainGUI extends JFrame {
    private JTable table;
    private JTable verificationTable;
    private JButton loadAllButton;
    private JButton verifyButton;
    private JButton incrementPriceButton;

    public MainGUI() {
        setTitle("Actualización Distribuida de Precios");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior (tabla principal con proporción 30%)
        JPanel topPanel = new JPanel(new BorderLayout());
        loadAllButton = new JButton("Cargar Todo");
        table = new JTable();

        topPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        topPanel.add(loadAllButton, BorderLayout.NORTH);

        // Asignar altura proporcional al panel superior
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setPreferredSize(new Dimension(800, 420)); // 30% de la altura total
        topContainer.add(topPanel);

        add(topContainer, BorderLayout.NORTH);

        // Panel inferior (tabla de verificación con proporción 70%)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        verifyButton = new JButton("Verificar Producto");
        incrementPriceButton = new JButton("Actualizar Precio"); // Cambiar el texto para mayor claridad
        verificationTable = new JTable();

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(verifyButton);
        buttonPanel.add(incrementPriceButton);

        bottomPanel.add(new JScrollPane(verificationTable), BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Asignar altura proporcional al panel inferior
        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setPreferredSize(new Dimension(800, 180)); // 70% de la altura total
        bottomContainer.add(bottomPanel);

        add(bottomContainer, BorderLayout.CENTER);

        // Acciones de los botones
        loadAllButton.addActionListener(e -> new LoadDataWorker().execute());
        verifyButton.addActionListener(e -> new VerifyProductWorker().execute());
        incrementPriceButton.addActionListener(e -> new UpdatePriceWorker().execute()); // Actualización con ID del producto
    }

    // Ventana flotante con barra de progreso
    private JDialog createProgressDialog(String message) {
        JDialog progressDialog = new JDialog(this, "Procesando", true);
        progressDialog.setLayout(new BorderLayout());
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(this);

        JLabel label = new JLabel(message, JLabel.CENTER);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        progressDialog.add(label, BorderLayout.NORTH);
        progressDialog.add(progressBar, BorderLayout.CENTER);

        return progressDialog;
    }

    // Clase para cargar todos los datos con ventana flotante
    private class LoadDataWorker extends SwingWorker<Void, Void> {
        private JDialog progressDialog;

        @Override
        protected void done() {
            progressDialog.dispose();
        }

        @Override
        protected Void doInBackground() throws Exception {
            progressDialog = createProgressDialog("Cargando datos...");
            SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));

            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "SELECT TSD.FOLIO, TSD.IDPRODUCTO, TSD.UNIDADES, TSD.PRECIO, TSD.TOTAL FROM TICKETSD TSD " +
                        "JOIN TICKETSH TSH ON TSD.FOLIO = TSH.FOLIO";

                try (PreparedStatement stmt = connection.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {
                    DefaultTableModel model = new DefaultTableModel();
                    table.setModel(model);

                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        model.addColumn(metaData.getColumnName(i));
                    }

                    while (rs.next()) {
                        Object[] row = new Object[columnCount];
                        for (int i = 1; i <= columnCount; i++) {
                            row[i - 1] = rs.getObject(i);
                        }
                        model.addRow(row);
                    }
                }
            }
            return null;
        }
    }

    // Clase para verificar un producto con ventana flotante
    private class VerifyProductWorker extends SwingWorker<Void, Void> {
        private JDialog progressDialog;

        @Override
        protected void done() {
            progressDialog.dispose();
        }

        @Override
        protected Void doInBackground() throws Exception {
            String productId = JOptionPane.showInputDialog("Ingrese el ID del Producto a verificar:");
            if (productId == null || productId.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Debe ingresar un ID válido.", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            progressDialog = createProgressDialog("Verificando producto...");
            SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));

            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "SELECT TSD.FOLIO, TSD.IDPRODUCTO, TSD.UNIDADES, TSD.PRECIO, TSD.TOTAL FROM TICKETSD TSD " +
                        "JOIN TICKETSH TSH ON TSD.FOLIO = TSH.FOLIO WHERE TSD.IDPRODUCTO = ?";

                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setString(1, productId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        DefaultTableModel model = new DefaultTableModel();
                        verificationTable.setModel(model);

                        ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();
                        for (int i = 1; i <= columnCount; i++) {
                            model.addColumn(metaData.getColumnName(i));
                        }

                        while (rs.next()) {
                            Object[] row = new Object[columnCount];
                            for (int i = 1; i <= columnCount; i++) {
                                row[i - 1] = rs.getObject(i);
                            }
                            model.addRow(row);
                        }
                    }
                }
            }
            return null;
        }
    }

    private class UpdatePriceWorker extends SwingWorker<Void, Void> {
        private JDialog progressDialog;
    
        @Override
        protected void done() {
            progressDialog.dispose();
        }
    
        @Override
        protected Void doInBackground() throws Exception {
            String productId = JOptionPane.showInputDialog("Ingrese el ID del Producto a actualizar:");
            if (productId == null || productId.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Debe ingresar un ID válido.", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
    
            progressDialog = createProgressDialog("Actualizando precio...");
            SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));
    
            try (Connection connection = DatabaseConnection.getConnection()) {
                String procedureCall = "{CALL ActualizarPrecios(?, ?)}"; // Llamar al procedimiento con dos parámetros
    
                try (CallableStatement stmt = connection.prepareCall(procedureCall)) {
                    stmt.setString(1, "IDPRODUCTO"); // Indica que se está usando el ID del producto
                    stmt.setInt(2, Integer.parseInt(productId)); // Convierte el ID ingresado a entero
                    stmt.execute();
                    JOptionPane.showMessageDialog(null, "Precio actualizado correctamente para el producto " + productId, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error al actualizar el precio: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }
    }
    


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainGUI gui = new MainGUI();
            gui.setVisible(true);
        });
    }
}

class DatabaseConnection {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=Empresa;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASSWORD = "123456789";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
