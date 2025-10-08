/*
 * Logger global thread-safe para el servidor de chat
 */
package server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Clase Logger thread-safe para registrar eventos del servidor de chat
 * @author 2dami
 */
public class Logger {
    private static Logger instance;
    private static final Object lock = new Object();
    private ObjectOutputStream writer;
    private DateTimeFormatter formatter;

    private Logger() {
        try {
            writer = new ObjectOutputStream(new FileOutputStream("chat_server.txt"));
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        } catch (IOException e) {
            System.err.println("Error inicializando logger: " + e.getMessage());
        }
    }

    public static Logger getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new Logger();
                }
            }
        }
        return instance;
    }

    public void log(String message) {
        synchronized (lock) {
            String timestamp = LocalDateTime.now().format(formatter);
            String logMessage = "[" + timestamp + "] " + message;

            // Imprimir en consola también
            System.out.println(logMessage);

            // Escribir en archivo
            if (writer != null) {
                try {
                    writer.writeObject(logMessage);
                } catch (IOException e) {
                    System.err.println("Error escribiendo en log: " + e.getMessage());
                }
            }
        }
    }

    public void logUserConnected(String username, String ipAddress) {
        log("USUARIO CONECTADO - Usuario: " + username + " | IP: " + ipAddress);
    }

    public void logUserDisconnected(String username) {
        log("USUARIO DESCONECTADO - Usuario: " + username);
    }

    public void logPublicMessage(String username, String message) {
        log("MENSAJE PÚBLICO - De: " + username + " | Mensaje: " + message);
    }

    public void logPrivateMessage(String fromUser, String toUser, String message) {
        log("MENSAJE PRIVADO - De: " + fromUser + " | Para: " + toUser + " | Mensaje: " + message);
    }

    public void logError(String errorMessage) {
        log("ERROR - " + errorMessage);
    }

    public void close() {
        synchronized (lock) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Error cerrando logger: " + e.getMessage());
                }
            }
        }
    }
}
