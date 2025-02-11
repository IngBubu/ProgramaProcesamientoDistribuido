import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CargaDeDatos extends SwingWorker<Void, Void> {
    private final JTable table;
    private final String criterio;
    private final int criterioId;
    private final JDialog progressDialog = createProgressDialog();

    public CargaDeDatos(JTable table, String criterio, int criterioId) {
        this.table = table;
        this.criterio = criterio;
        this.criterioId = criterioId;
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
        System.out.println("Iniciando carga de datos...");
        System.out.println("Criterio seleccionado: " + criterio);
        System.out.println("ID seleccionado: " + criterioId);
        
        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                   "SELECT TSD.FOLIO, TSD.IDPRODUCTO, TSD.UNIDADES, TSD.PRECIO, TSD.TOTAL " +
                   "FROM TICKETSD TSD " +
                   "JOIN TICKETSH TSH ON TSD.FOLIO = TSH.FOLIO " +
                   "WHERE TSH." + criterio + " = ? " +
                   "AND TSD.IDPRODUCTO IN ( " +
                   "    SELECT IDPRODUCTO " +
                   "    FROM TICKETSD TS " +
                   "    JOIN TICKETSH TH ON TS.FOLIO = TH.FOLIO " +
                   "    GROUP BY IDPRODUCTO " +
                   "    HAVING COUNT(DISTINCT IDESTADO) >= 5 " +
                   ") " +
                   "AND TSD.IDPRODUCTO IN ( " +
                   "    SELECT IDPRODUCTO " +
                   "    FROM TICKETSD TS " +
                   "    JOIN TICKETSH TH ON TS.FOLIO = TH.FOLIO " +
                   "    GROUP BY IDPRODUCTO, IDESTADO " +
                   "    HAVING SUM(TS.TOTAL) > 1000 " +
                   ") " +
                   "AND TSD.IDPRODUCTO IN ( " +
                   "    SELECT IDPRODUCTO " +
                   "    FROM TICKETSD TS " +
                   "    JOIN TICKETSH TH ON TS.FOLIO = TH.FOLIO " +
                   "    GROUP BY IDPRODUCTO, IDCIUDAD " +
                   "    HAVING SUM(TS.TOTAL) > 2000 " +
                   ")")) {
            stmt.setInt(1, criterioId);
            System.out.println("Ejecutando consulta SQL...");
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel model = new DefaultTableModel();
            
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
                JOptionPane.showMessageDialog(null, "No se encontraron datos con el criterio seleccionado.", "Información", JOptionPane.INFORMATION_MESSAGE);
                System.out.println("No se encontraron datos.");
            } else {
                SwingUtilities.invokeLater(() -> {
                    table.setModel(model);
                    table.revalidate();
                    table.repaint();
                    System.out.println("Tabla actualizada en la interfaz.");
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al cargar datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
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
