import javax.swing.*;
import java.awt.*;

public class MainGUI extends JFrame {
    private JTable table;
    private JTable verificationTable;
    private JButton loadAllButton;
    private JButton verifyButton;
    private JButton incrementPriceButton;

    public MainGUI() {
        setTitle("Actualización Distribuida de Precios");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior (tabla principal)
        JPanel topPanel = new JPanel(new BorderLayout());
        loadAllButton = new JButton("Cargar Todo");
        table = new JTable();

        topPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        topPanel.add(loadAllButton, BorderLayout.NORTH);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setPreferredSize(new Dimension(800, 420));
        topContainer.add(topPanel);

        add(topContainer, BorderLayout.NORTH);

        // Panel inferior (tabla de verificación)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        verifyButton = new JButton("Verificar Producto");
        incrementPriceButton = new JButton("Actualizar Precio");
        verificationTable = new JTable();

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(verifyButton);
        buttonPanel.add(incrementPriceButton);

        bottomPanel.add(new JScrollPane(verificationTable), BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setPreferredSize(new Dimension(800, 180));
        bottomContainer.add(bottomPanel);

        add(bottomContainer, BorderLayout.CENTER);

        // Acciones de los botones
        loadAllButton.addActionListener(e -> new DataLoader(table).execute());
        verifyButton.addActionListener(e -> new ProductVerifier(verificationTable).execute());
        incrementPriceButton.addActionListener(e -> new PriceUpdater().execute());
    }
}
