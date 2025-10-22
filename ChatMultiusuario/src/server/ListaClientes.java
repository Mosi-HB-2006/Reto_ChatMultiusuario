/*
 * Lista simple de clientes conectados usando ArrayList para retransmisión de mensajes
 */
package server;

import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase simple para manejar la lista de clientes conectados usando ArrayList
 *
 * @author 2dami
 */
public class ListaClientes {

    private List<Object[]> clientes;
    private Logger logger;

    public ListaClientes() {
        this.clientes = new ArrayList<>();
        this.logger = Logger.getInstance();
    }

    /**
     * Agrega un cliente a la lista
     */
    public synchronized void agregarCliente(String nombre, ObjectOutputStream salida) {
        clientes.add(new Object[]{nombre, salida});
        logger.log("Cliente agregado: " + nombre + " (Total: " + clientes.size() + ")");
    }

    /**
     * Remueve un cliente de la lista
     */
    public synchronized void removerCliente(String nombre) {
        clientes.removeIf(cliente -> nombre.equals((String) cliente[0]));
        logger.log("Cliente removido: " + nombre + " (Total: " + clientes.size() + ")");
    }

    /**
     * Retransmite un mensaje público a todos los clientes excepto al remitente
     */
    public synchronized void retransmitirMensajePublico(String remitente, String mensaje) {
        String mensajeFormateado = "[" + remitente + "] " + mensaje;
        int enviados = 0;

        for (Object[] cliente : clientes) {
            String nombreCliente = (String) cliente[0];
            ObjectOutputStream salidaCliente = (ObjectOutputStream) cliente[1];

            try {
                salidaCliente.writeObject(mensajeFormateado);
                enviados++;
            } catch (Exception e) {
                logger.logError("Error retransmitiendo a " + nombreCliente + ": " + e.getMessage());
                // Si hay error, remover el cliente (probablemente desconectado)
                clientes.remove(cliente);
            }
        }

        logger.log("Mensaje público retransmitido de " + remitente + " a " + enviados + " clientes");
    }

    /**
     * Obtiene el número de clientes conectados
     */
    public synchronized int getNumeroClientes() {
        return clientes.size();
    }

    /**
     * Notifica a todos los clientes (excepto al nuevo) cuando alguien se
     * conecta
     */
    public synchronized void notificarConexion(String nuevoCliente) {
        String mensajeNotificacion = ">>> " + nuevoCliente + " se ha conectado al chat <<<";
        int enviados = 0;

        for (Object[] cliente : clientes) {
            String nombreCliente = (String) cliente[0];
            ObjectOutputStream salidaCliente = (ObjectOutputStream) cliente[1];

            try {
                salidaCliente.writeObject(mensajeNotificacion);
                enviados++;
            } catch (Exception e) {
                logger.logError("Error notificando conexión a " + nombreCliente + ": " + e.getMessage());
                // Si hay error, remover el cliente (probablemente desconectado)
                clientes.remove(cliente);
            }
        }

        logger.log("Notificación de conexión enviada a " + enviados + " clientes");
    }

    /**
     * Envía un mensaje privado a un destinatario específico
     *
     * @param remitente El nombre del cliente que envía el mensaje
     * @param destinatario El nombre del cliente que debe recibir el mensaje
     * @param mensaje El contenido del mensaje
     * @return true si el mensaje fue entregado correctamente, false si el
     * destinatario no existe
     */
    public synchronized boolean enviarMensajePrivado(String remitente, String destinatario, String mensaje) {
        boolean entregado = false;

        for (Object[] cliente : clientes) {
            String nombreCliente = (String) cliente[0];
            ObjectOutputStream salidaCliente = (ObjectOutputStream) cliente[1];

            if (nombreCliente.equals(destinatario)) {
                try {
                    String mensajeFormateado = "[Privado de " + remitente + "] " + mensaje;
                    salidaCliente.writeObject(mensajeFormateado);
                    logger.log("Mensaje privado entregado de " + remitente + " a " + destinatario);
                    entregado = true;
                    return entregado; // Encontramos al destinatario y enviamos correctamente
                } catch (Exception e) {
                    logger.logError("Error enviando mensaje privado a " + destinatario + ": " + e.getMessage());
                    // Si hay error, remover el cliente (probablemente desconectado)
                    clientes.remove(cliente);
                    return false; // Error al enviar, salimos del método
                }
            }
        }

        return entregado; // Destinatario no encontrado
    }
}
