import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class VerificadorDeProducto extends SwingWorker<Void, Void> {
    private final JTable verificationTable;
    private final JDialog progressDialog = createProgressDialog();

    public VerificadorDeProducto(JTable verificationTable) {
        this.verificationTable = verificationTable;
    }

    @Override
    protected Void doInBackground() throws Exception {
        String productId = JOptionPane.showInputDialog("Ingrese el ID del Producto a verificar:");
        if (productId == null || productId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Debe ingresar un ID válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));
        verificarProducto(productId);
        return null;
    }

    @Override
    protected void done() {
        progressDialog.dispose();
    }

    private void verificarProducto(String productId) {
        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT TSD.FOLIO, TSD.IDPRODUCTO, TSD.UNIDADES, TSD.PRECIO, TSD.TOTAL, " +
                             "TSH.IDEMPLEADO, TSH.IDTIENDA, TSH.IDESTADO, TSH.IDCIUDAD " +
                             "FROM TICKETSD TSD " +
                             "JOIN TICKETSH TSH ON TSD.FOLIO = TSH.FOLIO " +
                             "WHERE TSD.IDPRODUCTO = ?")) {
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
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al verificar producto: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JDialog createProgressDialog() {
        JDialog dialog = new JDialog();
        dialog.setLayout(new BorderLayout());
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(null);
        dialog.add(new JLabel("Verificando producto...", JLabel.CENTER), BorderLayout.NORTH);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        dialog.add(progressBar, BorderLayout.CENTER);
        return dialog;
    }
}
