package server;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;

public class Monitor implements Runnable {
    private final ListaClientes listaClientes;
    private final Contador conexionesActivas;
    private final Logger logger;
    private final long intervalo; // intervalo en milisegundos
    private volatile boolean ejecutando;
    private LocalDateTime inicioServidor;
    private AtomicReference<String> ultimoMensaje;
    private LocalDateTime horaUltimoMensaje;

    public Monitor(ListaClientes listaClientes, Contador conexionesActivas, Logger logger, long intervalo) {
        this.listaClientes = listaClientes;
        this.conexionesActivas = conexionesActivas;
        this.logger = logger;
        this.intervalo = intervalo;
        this.ejecutando = true;
        this.inicioServidor = LocalDateTime.now();
        this.ultimoMensaje = new AtomicReference<>("Ningún mensaje aún");
        this.horaUltimoMensaje = LocalDateTime.now();
    }

    @Override
    public void run() {
        logger.log("MONITOR INICIADO - Intervalo de reporte: " + intervalo + "ms");
        System.out.println("Monitor del servidor iniciado. Reportes cada " + intervalo/1000 + " segundos.");

        while (ejecutando) {
            try {
                Thread.sleep(intervalo);
                if (ejecutando) {
                    generarReporte();
                }
            } catch (InterruptedException e) {
                System.out.println("Monitor interrumpido: " + e.getMessage());
                break;
            }
        }
        
        logger.log("MONITOR DETENIDO");
        System.out.println("Monitor del servidor detenido.");
    }

    private void generarReporte() {
        LocalDateTime ahora = LocalDateTime.now();
        Duration tiempoActivo = Duration.between(inicioServidor, ahora);
        
        // Formatear duración - compatible con Java 8
        long segundosTotales = tiempoActivo.getSeconds();
        long horas = segundosTotales / 3600;
        long minutos = (segundosTotales % 3600) / 60;
        long segundos = segundosTotales % 60;
        
        String tiempoFormateado = String.format("%02d:%02d:%02d", horas, minutos, segundos);
        
        // Obtener estadísticas
        int clientesConectados = listaClientes.getNumeroClientes();
        int conexionesTotales = conexionesActivas.get(); // Esto muestra el contador histórico
        
        String ultimoMsg = ultimoMensaje.get();
        String horaUltimoMsg = horaUltimoMensaje.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        
        // Construir reporte
        StringBuilder reporte = new StringBuilder();
        reporte.append("=== ESTADÍSTICAS DEL SERVIDOR ===\n");
        reporte.append("• Tiempo activo: ").append(tiempoFormateado).append("\n");
        reporte.append("• Clientes conectados: ").append(clientesConectados).append("\n");
        reporte.append("• Conexiones totales (histórico): ").append(conexionesTotales).append("\n");
        reporte.append("• Último mensaje: ").append(ultimoMsg).append("\n");
        reporte.append("• Hora último mensaje: ").append(horaUltimoMsg).append("\n");
        
        // Obtener lista de clientes conectados
        if (clientesConectados > 0) {
            reporte.append("• Clientes actuales: ").append(String.join(", ", listaClientes.obtenerNombres())).append("\n");
        } else {
            reporte.append("• Clientes actuales: Ninguno\n");
        }
        
        reporte.append("=================================");
        
        // Mostrar reporte
        System.out.println(reporte.toString());
        logger.log("REPORTE MONITOR - " + 
                   "Tiempo: " + tiempoFormateado + 
                   " | Clientes: " + clientesConectados + 
                   " | Conexiones totales: " + conexionesTotales);
    }

    public void actualizarUltimoMensaje(String mensaje) {
        this.ultimoMensaje.set(mensaje);
        this.horaUltimoMensaje = LocalDateTime.now();
    }

    public void detener() {
        this.ejecutando = false;
    }

    public LocalDateTime getInicioServidor() {
        return inicioServidor;
    }
}