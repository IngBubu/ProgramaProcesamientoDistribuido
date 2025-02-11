import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ActualizadorDePrecio extends SwingWorker<Void, Integer> {
    private final JDialog progressDialog;
    private final String criterio;
    private final int criterioId;
    private final JProgressBar progressBar;
    private final JLabel lblEstado;

    public ActualizadorDePrecio(String criterio, int criterioId, JProgressBar progressBar, JLabel lblEstado) {
        this.criterio = criterio;
        this.criterioId = criterioId;
        this.progressBar = progressBar;
        this.lblEstado = lblEstado;
        this.progressDialog = createProgressDialog(); // Ahora se inicializa correctamente
    }

    @Override
    protected Void doInBackground() throws Exception {
        if (criterio == null) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar un criterio.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        SwingUtilities.invokeLater(() -> {
            progressDialog.setVisible(true);
            lblEstado.setText("Actualizando...");
        });
        actualizarPrecios();
        return null;
    }

    @Override
    protected void process(java.util.List<Integer> chunks) {
        int progress = chunks.get(chunks.size() - 1);
        progressBar.setValue(progress);
        lblEstado.setText("Actualizando producto " + progress + "...");
    }

    @Override
    protected void done() {
        progressDialog.dispose();
        lblEstado.setText("Actualización completada");
    }

    private void actualizarPrecios() {
        try (Connection connection = ConexionBD.getConnection()) {
            connection.setAutoCommit(false);
            
            String query = "SELECT DISTINCT IDPRODUCTO FROM TICKETSD " +
                           "JOIN TICKETSH ON TICKETSD.FOLIO = TICKETSH.FOLIO " +
                           "WHERE TICKETSH." + criterio + " = ?";
    
            try (PreparedStatement stmt = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                stmt.setInt(1, criterioId);
                ResultSet rs = stmt.executeQuery();
                
                // Obtener el total de productos para la barra de progreso
                rs.last(); // Mueve el cursor al último registro
                int totalProductos = rs.getRow(); // Obtiene el número total de filas
                progressBar.setMaximum(totalProductos);
                rs.beforeFirst(); // Regresar el cursor al inicio
    
                // Actualizar productos uno por uno
                int progress = 0;
                while (rs.next()) {
                    int idProducto = rs.getInt("IDPRODUCTO");
                    actualizarProducto(connection, idProducto);
                    progress++;
                    publish(progress);
                }
                connection.commit();
                JOptionPane.showMessageDialog(null, "Precios actualizados correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1205) {
                JOptionPane.showMessageDialog(null, "Error: Conflicto de concurrencia (Deadlock).", "Error de Concurrencia", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Error al actualizar precios: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    

    private void actualizarProducto(Connection connection, int idProducto) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE TICKETSD SET PRECIO = PRECIO + 1 WHERE IDPRODUCTO = ?")) {
            stmt.setInt(1, idProducto);
            stmt.executeUpdate();
        }
    }

    private JDialog createProgressDialog() {
        JDialog dialog = new JDialog();
        dialog.setLayout(new BorderLayout());
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(null);
        dialog.add(new JLabel("Actualizando precios...", JLabel.CENTER), BorderLayout.NORTH);
        
        // Verificar si progressBar no es null antes de usarlo
        if (progressBar != null) {
            progressBar.setIndeterminate(false);
            progressBar.setValue(0);
            dialog.add(progressBar, BorderLayout.CENTER);
        }

        return dialog;
    }
}
