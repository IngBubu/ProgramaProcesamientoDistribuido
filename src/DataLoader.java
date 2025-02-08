import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.awt.BorderLayout;


public class DataLoader extends SwingWorker<Void, Void> {
    private JTable table;
    private JDialog progressDialog;

    public DataLoader(JTable table) {
        this.table = table;
    }

    @Override
    protected void done() {
        progressDialog.dispose();
    }

    @Override
    protected Void doInBackground() throws Exception {
        progressDialog = createProgressDialog("Cargando datos...");
        SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));

        try (Connection connection = ConexionBD.getConnection()) {
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
