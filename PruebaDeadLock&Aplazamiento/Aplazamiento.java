import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Aplazamiento {
    private static final int NUM_THREADS = 4; // Número de hilos para simular aplazamiento infinito

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);

        // Hilos con diferentes prioridades y los IDPRODUCTO de la lista proporcionada
        executorService.execute(() -> {
            try {
                simulateStarvation(1, 10, Thread.MAX_PRIORITY); // Producto 10
            } catch (Exception e) {
                System.err.println("Error en Hilo 1: " + e.getMessage());
            }
        });

        executorService.execute(() -> {
            try {
                simulateStarvation(2, 30, Thread.MAX_PRIORITY); // Producto 30
            } catch (Exception e) {
                System.err.println("Error en Hilo 2: " + e.getMessage());
            }
        });

        executorService.execute(() -> {
            try {
                simulateStarvation(3, 54, Thread.MAX_PRIORITY); // Producto 54
            } catch (Exception e) {
                System.err.println("Error en Hilo 3: " + e.getMessage());
            }
        });

        // Hilo de menor prioridad
        executorService.execute(() -> {
            try {
                simulateStarvation(4, 99, Thread.MIN_PRIORITY); // Producto 99
            } catch (Exception e) {
                System.err.println("Error en Hilo 4: " + e.getMessage());
            }
        });

        executorService.shutdown(); // Finaliza el pool de hilos cuando todos terminan
    }
    private static void simulateStarvation(int threadId, int productId, int priority) {
        Thread.currentThread().setPriority(priority);
    
        for (int attempt = 1; attempt <= 20; attempt++) {
            try (Connection connection = ConexionBD.getConnection()) {
                connection.setAutoCommit(false);
                connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
    
                System.out.println("Hilo " + threadId + ": Intento " + attempt + " de actualizar Producto con ID " + productId);
    
                // Simular bloqueo adicional para hilos de alta prioridad
                if (priority == Thread.MAX_PRIORITY) {
                    Thread.sleep(3000); // Incrementar el tiempo de espera
                }
    
                // Verificar criterios con bloqueo explícito
                String debugQuery = """
                    SELECT 
                        COUNT(DISTINCT TSH.ESTADO) AS EstadosUnicos,
                        AVG(TSH.TOTAL) AS PromedioEstado,
                        AVG(TSH.TOTAL) AS PromedioCiudad
                    FROM TICKETSH TSH
                    WHERE TSH.FOLIO IN (
                        SELECT TSD.FOLIO 
                        FROM TICKETSD TSD 
                        WHERE TSD.IDPRODUCTO = ?
                    )
                    FOR UPDATE; -- Bloqueo explícito
                """;
    
                try (PreparedStatement debugStmt = connection.prepareStatement(debugQuery)) {
                    debugStmt.setInt(1, productId);
    
                    try (ResultSet rs = debugStmt.executeQuery()) {
                        if (rs.next()) {
                            int estadosUnicos = rs.getInt("EstadosUnicos");
                            double promedioEstado = rs.getDouble("PromedioEstado");
                            double promedioCiudad = rs.getDouble("PromedioCiudad");
    
                            System.out.println("Hilo " + threadId + ": Datos para ID " + productId);
                            System.out.println("  - Estados únicos: " + estadosUnicos);
                            System.out.println("  - Promedio estado: " + promedioEstado);
                            System.out.println("  - Promedio ciudad: " + promedioCiudad);
    
                            if (estadosUnicos >= 5 && promedioEstado > 1000 && promedioCiudad > 2000) {
                                System.out.println("Hilo " + threadId + ": Producto cumple criterios.");
                            } else {
                                System.out.println("Hilo " + threadId + ": Producto no cumple criterios.");
                                continue; // Salta a la siguiente iteración si no cumple
                            }
                        } else {
                            System.out.println("Hilo " + threadId + ": No se encontraron datos para el producto.");
                            continue;
                        }
                    }
                }
    
                // Intentar la actualización
                String updateQuery = "UPDATE TICKETSD SET PRECIO = PRECIO + 10 WHERE IDPRODUCTO = ?;";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                    updateStmt.setInt(1, productId);
                    int rowsAffected = updateStmt.executeUpdate();
    
                    if (rowsAffected > 0) {
                        System.out.println("Hilo " + threadId + ": Producto con ID " + productId + " actualizado.");
                    } else {
                        System.out.println("Hilo " + threadId + ": Ningún producto cumplió los criterios.");
                    }
                }
    
                connection.commit();
                System.out.println("Hilo " + threadId + ": Transacción completada.");
                return; // Salir del bucle si la transacción es exitosa
            } catch (SQLException e) {
                if (e.getSQLState().equals("40001")) {
                    System.err.println("Hilo " + threadId + ": Deadlock detectado. Reintentando...");
                    try {
                        Thread.sleep(2000); // Esperar antes de reintentar
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    System.err.println("Hilo " + threadId + ": Error en la transacción. Rollback.");
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    
        System.err.println("Hilo " + threadId + ": No pudo completar la transacción después de varios intentos.");
    }    
}    