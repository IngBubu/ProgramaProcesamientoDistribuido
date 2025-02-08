import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class MainGUI extends JFrame {
    private JTable table;
    private JButton updateButton;
    private JButton loadAllButton;

    public MainGUI() {
        setTitle("Actualización Distribuida de Precios");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior con botón de carga
        JPanel topPanel = new JPanel(new FlowLayout());
        loadAllButton = new JButton("Cargar Todo");
        topPanel.add(loadAllButton);
        add(topPanel, BorderLayout.NORTH);

        // Tabla para mostrar datos
        table = new JTable();
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Botón para actualizar precios
        updateButton = new JButton("Actualizar Precio");
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(updateButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Agregar acción al botón de carga
        loadAllButton.addActionListener(e -> loadAllTableData());
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
