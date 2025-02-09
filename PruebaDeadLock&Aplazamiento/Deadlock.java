import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Deadlock {
    private static final int NUM_THREADS = 5; // Ahora usaremos 5 hilos

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);

        // Configuramos 5 hilos con combinaciones de productos
        executorService.execute(() -> {
            try {
                forceDeadlock(1, 89, 53); // Producto A -> Producto B
            } catch (Exception e) {
                System.err.println("Error en Hilo 1: " + e.getMessage());
            }
        });

        executorService.execute(() -> {
            try {
                forceDeadlock(2, 53, 89); // Producto B -> Producto A
            } catch (Exception e) {
                System.err.println("Error en Hilo 2: " + e.getMessage());
            }
        });

        executorService.execute(() -> {
            try {
                forceDeadlock(3, 106, 132); // Producto C -> Producto D
            } catch (Exception e) {
                System.err.println("Error en Hilo 3: " + e.getMessage());
            }
        });

        executorService.execute(() -> {
            try {
                forceDeadlock(4, 132, 106); // Producto D -> Producto C
            } catch (Exception e) {
                System.err.println("Error en Hilo 4: " + e.getMessage());
            }
        });

        executorService.execute(() -> {
            try {
                forceDeadlock(5, 53, 132); // Producto B -> Producto C
            } catch (Exception e) {
                System.err.println("Error en Hilo 5: " + e.getMessage());
            }
        });

        executorService.shutdown(); // Finaliza el pool de hilos cuando todos terminan
    }

    private static void forceDeadlock(int threadId, int productId1, int productId2) throws SQLException {
        try (Connection connection = ConexionBD.getConnection()) {
            // Configurar la transacción con aislamiento SERIALIZABLE
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            System.out.println("Hilo " + threadId + ": Iniciando transacción...");

            // Bloquea el primer producto
            String updateQuery = "UPDATE TICKETSD " +
                                 "SET PRECIO = PRECIO + 10 " +
                                 "WHERE IDPRODUCTO = ? AND PRECIO < 500 AND UNIDADES > 0";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                updateStmt.setInt(1, productId1);
                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Hilo " + threadId + ": Bloqueado Producto con ID " + productId1);
                } else {
                    System.out.println("Hilo " + threadId + ": Ningún producto cumplió los criterios para ID " + productId1);
                    connection.rollback();
                    return;
                }
            }

            // Simula un retraso para aumentar las probabilidades de conflicto
            try {
                Thread.sleep(2000); // Aumenta el tiempo para generar más conflictos
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Bloquea el segundo producto
            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                updateStmt.setInt(1, productId2);
                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Hilo " + threadId + ": Bloqueado Producto con ID " + productId2);
                } else {
                    System.out.println("Hilo " + threadId + ": Ningún producto cumplió los criterios para ID " + productId2);
                    connection.rollback();
                    return;
                }
            }

            // Si todo tiene éxito, realiza el commit
            connection.commit();
            System.out.println("Hilo " + threadId + ": Transacción completada.");
        } catch (SQLException e) {
            System.err.println("Hilo " + threadId + ": Error en la transacción. Realizando rollback.");
            throw e; // Relanzar la excepción para que se registre en el log
        }
    }
}
