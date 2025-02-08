import javax.swing.*;
import java.awt.*;

public class Interfaz extends JFrame {
    private JTable tablaCarga;
    private JTable tablaVerificacion;
    private JButton btnCarga;
    private JButton verifyButton;
    private JButton btnIncrementarPrecio;

    public Interfaz() {
        setTitle("Actualización Distribuida de Precios");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior (tabla principal)
        JPanel topPanel = new JPanel(new BorderLayout());
        btnCarga = new JButton("Cargar Todo");
        tablaCarga = new JTable();

        topPanel.add(new JScrollPane(tablaCarga), BorderLayout.CENTER);
        topPanel.add(btnCarga, BorderLayout.NORTH);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setPreferredSize(new Dimension(800, 420));
        topContainer.add(topPanel);

        add(topContainer, BorderLayout.NORTH);

        // Panel inferior (tabla de verificación)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        verifyButton = new JButton("Verificar Producto");
        btnIncrementarPrecio = new JButton("Actualizar Precio");
        tablaVerificacion = new JTable();

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(verifyButton);
        buttonPanel.add(btnIncrementarPrecio);

        bottomPanel.add(new JScrollPane(tablaVerificacion), BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setPreferredSize(new Dimension(800, 180));
        bottomContainer.add(bottomPanel);

        add(bottomContainer, BorderLayout.CENTER);

        // Acciones de los botones
        btnCarga.addActionListener(e -> new CargaDeDatos(tablaCarga).execute());
        verifyButton.addActionListener(e -> new VerificadorDeProducto(tablaVerificacion).execute());
        btnIncrementarPrecio.addActionListener(e -> new ActualizadorDePrecio().execute());
    }
}
