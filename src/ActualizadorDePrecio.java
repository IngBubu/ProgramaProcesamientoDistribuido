import javax.swing.*;
import java.awt.BorderLayout;
import java.sql.*;

public class ActualizadorDePrecio extends SwingWorker<Void, Void> {
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

        try (Connection connection = ConexionBD.getConnection()) {
            String procedureCall = "{CALL ActualizarPrecios(?, ?)}";

            try (CallableStatement stmt = connection.prepareCall(procedureCall)) {
                stmt.setString(1, "IDPRODUCTO");
                stmt.setInt(2, Integer.parseInt(productId));
                stmt.execute();
                JOptionPane.showMessageDialog(null, "Precio actualizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
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