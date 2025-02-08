import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class MainGUI extends JFrame {
    private JTable table;
    private JTable verificationTable;
    private JButton updateButton;
    private JButton loadAllButton;
    private JButton verifyButton;
    private JButton incrementPriceButton;
    private JButton decrementPriceButton;

    public MainGUI() {
        setTitle("Actualización Distribuida de Precios");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(2, 1)); // Dividir la ventana en dos mitades

        // Panel superior con tabla principal y botón de carga
        JPanel topPanel = new JPanel(new BorderLayout());
        loadAllButton = new JButton("Cargar Todo");
        table = new JTable();

        topPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        topPanel.add(loadAllButton, BorderLayout.NORTH);
        add(topPanel);

        // Panel inferior con tabla de verificación y botones
        JPanel bottomPanel = new JPanel(new BorderLayout());
        verifyButton = new JButton("Verificar Producto");
        incrementPriceButton = new JButton("Incrementar Precio");
        decrementPriceButton = new JButton("Decrementar Precio");
        verificationTable = new JTable();

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(decrementPriceButton);
        buttonPanel.add(verifyButton);
        buttonPanel.add(incrementPriceButton);

        bottomPanel.add(new JScrollPane(verificationTable), BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel);

        // Agregar acción al botón de carga
        loadAllButton.addActionListener(e -> loadAllTableData());

        // Agregar acción al botón de verificación
        verifyButton.addActionListener(e -> verifyProductData());

        // Agregar acción al botón de incremento de precio
        incrementPriceButton.addActionListener(e -> incrementProductPrice());

        // Agregar acción al botón de decremento de precio
        decrementPriceButton.addActionListener(e -> decrementProductPrice());
    }

    private void loadAllTableData() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT TSD.FOLIO, TSD.IDPRODUCTO, TSD.UNIDADES, TSD.PRECIO, TSD.TOTAL " +
                    "FROM TICKETSD TSD " +
                    "JOIN TICKETSH TSH ON TSD.FOLIO = TSH.FOLIO " +
                    "WHERE TSH.IDESTADO IN (" +
                    "    SELECT IDESTADO " +
                    "    FROM TICKETSH " +
                    "    GROUP BY IDESTADO " +
                    "    HAVING COUNT(*) >= 5" +
                    ") " +
                    "AND TSH.IDESTADO IN (" +
                    "    SELECT TSH.IDESTADO " +
                    "    FROM TICKETSD TSD " +
                    "    JOIN TICKETSH TSH ON TSD.FOLIO = TSH.FOLIO " +
                    "    GROUP BY TSH.IDESTADO " +
                    "    HAVING AVG(TSD.TOTAL) > 1000" +
                    ") " +
                    "AND TSH.IDCIUDAD IN (" +
                    "    SELECT TSH.IDCIUDAD " +
                    "    FROM TICKETSD TSD " +
                    "    JOIN TICKETSH TSH ON TSD.FOLIO = TSH.FOLIO " +
                    "    GROUP BY TSH.IDCIUDAD " +
                    "    HAVING AVG(TSD.TOTAL) > 2000" +
                    ")";
            
            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                DefaultTableModel model = new DefaultTableModel();
                table.setModel(model);

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    model.addColumn(metaData.getColumnName(i));
                }

                boolean hasData = false;
                while (rs.next()) {
                    hasData = true;
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        row[i - 1] = rs.getObject(i);
                    }
                    model.addRow(row);
                }
                
                if (!hasData) {
                    JOptionPane.showMessageDialog(this, "Ningún Producto Cumple con los requisitos para ser actualizado", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar los datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void verifyProductData() {
        String productId = JOptionPane.showInputDialog(this, "Ingrese el ID del Producto a verificar:", "Verificar Producto", JOptionPane.QUESTION_MESSAGE);

        if (productId == null || productId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar un ID de Producto válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT TSD.FOLIO, TSD.IDPRODUCTO, TSD.UNIDADES, TSD.PRECIO, TSD.TOTAL, TSH.IDESTADO, TSH.IDCIUDAD " +
                    "FROM TICKETSD TSD " +
                    "JOIN TICKETSH TSH ON TSD.FOLIO = TSH.FOLIO " +
                    "WHERE TSD.IDPRODUCTO = ? " +
                    "AND TSH.IDESTADO IN (" +
                    "    SELECT IDESTADO " +
                    "    FROM TICKETSH " +
                    "    GROUP BY IDESTADO " +
                    "    HAVING COUNT(*) >= 5" +
                    ") " +
                    "AND TSH.IDESTADO IN (" +
                    "    SELECT TSH.IDESTADO " +
                    "    FROM TICKETSD TSD " +
                    "    JOIN TICKETSH TSH ON TSD.FOLIO = TSH.FOLIO " +
                    "    GROUP BY TSH.IDESTADO " +
                    "    HAVING AVG(TSD.TOTAL) > 1000" +
                    ") " +
                    "AND TSH.IDCIUDAD IN (" +
                    "    SELECT TSH.IDCIUDAD " +
                    "    FROM TICKETSD TSD " +
                    "    JOIN TICKETSH TSH ON TSD.FOLIO = TSH.FOLIO " +
                    "    GROUP BY TSH.IDCIUDAD " +
                    "    HAVING AVG(TSD.TOTAL) > 2000" +
                    ")";
            
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

                    boolean hasData = false;
                    while (rs.next()) {
                        hasData = true;
                        Object[] row = new Object[columnCount];
                        for (int i = 1; i <= columnCount; i++) {
                            row[i - 1] = rs.getObject(i);
                        }
                        model.addRow(row);
                    }
                    
                    if (!hasData) {
                        JOptionPane.showMessageDialog(this, "El producto con ID " + productId + " no cumple con los requisitos o no tiene ventas registradas.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al consultar el producto: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void incrementProductPrice() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String updateQuery = "UPDATE TICKETSD " +
                    "SET PRECIO = PRECIO + 1 " +
                    "WHERE FOLIO IN (" +
                    "    SELECT TSD.FOLIO " +
                    "    FROM TICKETSD TSD " +
                    "    JOIN TICKETSH TSH ON TSD.FOLIO = TSH.FOLIO " +
                    "    WHERE TSH.IDESTADO IN (" +
                    "        SELECT IDESTADO " +
                    "        FROM TICKETSH " +
                    "        GROUP BY IDESTADO " +
                    "        HAVING COUNT(*) >= 5" +
                    "    ) " +
                    "    AND TSH.IDESTADO IN (" +
                    "        SELECT TSH.IDESTADO " +
                    "        FROM TICKETSD TSD " +
                    "        JOIN TICKETSH TSH ON TSD.FOLIO = TSH.FOLIO " +
                    "        GROUP BY TSH.IDESTADO " +
                    "        HAVING AVG(TSD.TOTAL) > 1000" +
                    "    ) " +
                    "    AND TSH.IDCIUDAD IN (" +
                    "        SELECT TSH.IDCIUDAD " +
                    "        FROM TICKETSD TSD " +
                    "        JOIN TICKETSH TSH ON TSD.FOLIO = TSH.FOLIO " +
                    "        GROUP BY TSH.IDCIUDAD " +
                    "        HAVING AVG(TSD.TOTAL) > 2000" +
                    "    )" +
                    ")";

            try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                int rowsUpdated = stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, rowsUpdated + " productos han sido actualizados.", "Actualización Exitosa", JOptionPane.INFORMATION_MESSAGE);
                loadAllTableData(); // Recargar datos
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al actualizar los precios: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void decrementProductPrice() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String updateQuery = "UPDATE TICKETSD " +
                    "SET PRECIO = PRECIO - 1 " +
                    "WHERE FOLIO IN (" +
                    "    SELECT TSD.FOLIO " +
                    "    FROM TICKETSD TSD " +
                    "    JOIN TICKETSH TSH ON TSD.FOLIO = TSH.FOLIO " +
                    "    WHERE TSH.IDESTADO IN (" +
                    "        SELECT IDESTADO " +
                    "        FROM TICKETSH " +
                    "        GROUP BY IDESTADO " +
                    "        HAVING COUNT(*) >= 5" +
                    "    ) " +
                    "    AND TSH.IDESTADO IN (" +
                    "        SELECT TSH.IDESTADO " +
                    "        FROM TICKETSD TSD " +
                    "        JOIN TICKETSH TSH ON TSD.FOLIO = TSH.FOLIO " +
                    "        GROUP BY TSH.IDESTADO " +
                    "        HAVING AVG(TSD.TOTAL) > 1000" +
                    "    ) " +
                    "    AND TSH.IDCIUDAD IN (" +
                    "        SELECT TSH.IDCIUDAD " +
                    "        FROM TICKETSD TSD " +
                    "        JOIN TICKETSH TSH ON TSD.FOLIO = TSH.FOLIO " +
                    "        GROUP BY TSH.IDCIUDAD " +
                    "        HAVING AVG(TSD.TOTAL) > 2000" +
                    "    )" +
                    ")";

            try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                int rowsUpdated = stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, rowsUpdated + " productos han sido actualizados.", "Actualización Exitosa", JOptionPane.INFORMATION_MESSAGE);
                loadAllTableData(); // Recargar datos
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al actualizar los precios: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
    private static final String USER = "sa"; // Cambia esto por tu usuario
    private static final String PASSWORD = "123456789"; // Cambia esto por tu contraseña

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
