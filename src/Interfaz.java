import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Interfaz extends JFrame {
    private JTable tablaCarga;
    private JButton btnCargarDatos;
    private JButton btnIncrementarPrecio;
    private JRadioButton rbtnTienda, rbtnEmpleado, rbtnCiudad;
    private JComboBox<Integer> comboBoxCriterio;
    private ButtonGroup criterioGroup;
    private JProgressBar progressBar;
    private JLabel lblEstado;

    public Interfaz() {
        setTitle("Actualización Distribuida de Precios");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel de selección de criterio (ahora en la parte superior)
        JPanel topPanel = new JPanel(new FlowLayout());
        rbtnTienda = new JRadioButton("IDTIENDA");
        rbtnEmpleado = new JRadioButton("IDEMPLEADO");
        rbtnCiudad = new JRadioButton("IDCIUDAD");
        criterioGroup = new ButtonGroup();
        criterioGroup.add(rbtnTienda);
        criterioGroup.add(rbtnEmpleado);
        criterioGroup.add(rbtnCiudad);
        comboBoxCriterio = new JComboBox<>();
        btnCargarDatos = new JButton("Cargar Datos");

        topPanel.add(new JLabel("Seleccione criterio:"));
        topPanel.add(rbtnTienda);
        topPanel.add(rbtnEmpleado);
        topPanel.add(rbtnCiudad);
        topPanel.add(comboBoxCriterio);
        topPanel.add(btnCargarDatos);

        add(topPanel, BorderLayout.NORTH);

        // Panel de tabla principal
        JPanel centerPanel = new JPanel(new BorderLayout());
        tablaCarga = new JTable(new DefaultTableModel());
        JScrollPane scrollPane = new JScrollPane(tablaCarga);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Panel inferior con barra de progreso y estado
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel progressPanel = new JPanel(new FlowLayout());
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        lblEstado = new JLabel("Esperando actualización...");
        
        btnIncrementarPrecio = new JButton("Actualizar Precio");
        progressPanel.add(btnIncrementarPrecio);
        progressPanel.add(progressBar);
        progressPanel.add(lblEstado);
        bottomPanel.add(progressPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Acciones de los botones
        btnCargarDatos.addActionListener(e -> cargarDatosCriterio());
        btnIncrementarPrecio.addActionListener(e -> iniciarActualizacion());

        // Cargar IDs en el ComboBox
        rbtnTienda.addActionListener(e -> cargarIds("IDTIENDA"));
        rbtnEmpleado.addActionListener(e -> cargarIds("IDEMPLEADO"));
        rbtnCiudad.addActionListener(e -> cargarIds("IDCIUDAD"));
    }

    private String getSelectedCriterio() {
        if (rbtnTienda.isSelected()) return "IDTIENDA";
        if (rbtnEmpleado.isSelected()) return "IDEMPLEADO";
        if (rbtnCiudad.isSelected()) return "IDCIUDAD";
        return null;
    }

    private void cargarIds(String criterio) {
        comboBoxCriterio.removeAllItems();
        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT DISTINCT " + criterio + " FROM TICKETSH")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                comboBoxCriterio.addItem(rs.getInt(1));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar IDs: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void iniciarActualizacion() {
        String criterio = getSelectedCriterio();
        Integer criterioId = (Integer) comboBoxCriterio.getSelectedItem();
        if (criterio == null || criterioId == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un criterio y un ID válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        new ActualizadorDePrecio(criterio, criterioId, progressBar, lblEstado).execute();
    }
    private void cargarDatosCriterio() {
        String criterio = getSelectedCriterio();
        Integer criterioId = (Integer) comboBoxCriterio.getSelectedItem();
    
        if (criterio == null || criterioId == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un criterio y un ID válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        new CargaDeDatos(tablaCarga, criterio, criterioId).execute();
    }
    
}
