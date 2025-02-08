import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.awt.*;

public class CargaDeDatos extends SwingWorker<Void, Void> {
    private final JTable table;
    private final JDialog progressDialog = createProgressDialog();

    public CargaDeDatos(JTable table) {
        this.table = table;
    }

    @Override
    protected Void doInBackground() throws Exception {
        SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));
        cargarDatos();
        return null;
    }

    @Override
    protected void done() {
        progressDialog.dispose();
    }

    private void cargarDatos() {
        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT TSD.FOLIO, TSD.IDPRODUCTO, TSD.UNIDADES, TSD.PRECIO, TSD.TOTAL FROM TICKETSD TSD " +
                             "JOIN TICKETSH TSH ON TSD.FOLIO = TSH.FOLIO");
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
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al cargar datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JDialog createProgressDialog() {
        JDialog dialog = new JDialog();
        dialog.setLayout(new BorderLayout());
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(null);
        dialog.add(new JLabel("Cargando datos...", JLabel.CENTER), BorderLayout.NORTH);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        dialog.add(progressBar, BorderLayout.CENTER);
        return dialog;
    }
}
