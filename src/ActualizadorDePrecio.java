import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ActualizadorDePrecio extends SwingWorker<Void, Void> {
    private final JDialog progressDialog = createProgressDialog();

    @Override
    protected Void doInBackground() throws Exception {
        String productId = JOptionPane.showInputDialog("Ingrese el ID del Producto a actualizar:");
        if (productId == null || productId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Debe ingresar un ID válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));
        actualizarPrecio(productId);
        return null;
    }

    @Override
    protected void done() {
        progressDialog.dispose();
    }

    private void actualizarPrecio(String productId) {
        try (Connection connection = ConexionBD.getConnection();
             CallableStatement stmt = connection.prepareCall("{CALL ActualizarPrecios(?, ?)}")) {
            stmt.setString(1, "IDPRODUCTO");
            stmt.setInt(2, Integer.parseInt(productId));
            stmt.execute();
            JOptionPane.showMessageDialog(null, "Precio actualizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar precio: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JDialog createProgressDialog() {
        JDialog dialog = new JDialog();
        dialog.setLayout(new BorderLayout());
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(null);
        dialog.add(new JLabel("Actualizando precio...", JLabel.CENTER), BorderLayout.NORTH);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        dialog.add(progressBar, BorderLayout.CENTER);
        return dialog;
    }
}
