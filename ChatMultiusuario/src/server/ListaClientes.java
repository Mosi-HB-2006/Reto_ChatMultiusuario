/*
 * Lista simple de clientes conectados usando ArrayList para retransmisión de mensajes
 */
package server;

import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase simple para manejar la lista de clientes conectados usando ArrayList
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

            // No enviar el mensaje al propio remitente
            if (!nombreCliente.equals(remitente)) {
                try {
                    salidaCliente.writeObject(mensajeFormateado);
                    enviados++;
                } catch (Exception e) {
                    logger.logError("Error retransmitiendo a " + nombreCliente + ": " + e.getMessage());
                    // Si hay error, remover el cliente (probablemente desconectado)
                    clientes.remove(cliente);
                }
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
     * Lista los nombres de todos los clientes conectados
     */
    public synchronized String[] getClientesConectados() {
        String[] nombres = new String[clientes.size()];
        for (int i = 0; i < clientes.size(); i++) {
            nombres[i] = (String) clientes.get(i)[0];
        }
        return nombres;
    }
}
