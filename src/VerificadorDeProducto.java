import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.sql.*;

public class VerificadorDeProducto extends SwingWorker<Void, Void> {
    private JTable verificationTable;
    private JDialog progressDialog;

    public VerificadorDeProducto(JTable verificationTable) {
        this.verificationTable = verificationTable;
    }

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

        try (Connection connection = ConexionBD.getConnection()) {
            String query = "SELECT * FROM TICKETSD WHERE IDPRODUCTO = ?";

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

    private JDialog createProgressDialog(String message) {
        JDialog progressDialog = new JDialog();
        progressDialog.setLayout(new BorderLayout());
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(null);

        JLabel label = new JLabel(message, JLabel.CENTER);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        progressDialog.add(label, BorderLayout.NORTH);
        progressDialog.add(progressBar, BorderLayout.CENTER);

        return progressDialog;
    }
}
