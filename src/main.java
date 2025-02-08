import javax.swing.SwingUtilities;

public class main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Interfaz gui = new Interfaz();
            gui.setVisible(true);
        });
    }
}
