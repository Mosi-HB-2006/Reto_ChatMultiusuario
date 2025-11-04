/*
 * Lista simple de clientes conectados usando ArrayList para retransmisión de mensajes
 */
package server;

import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 2dami
 */
public class ListaClientes {

    private List<Object[]> clientes;
    private Logger logger;

    public ListaClientes() {
        this.clientes = new ArrayList<>();
        this.logger = Logger.getInstance();
    }

    public synchronized void agregarCliente(String nombre, ObjectOutputStream salida) {
        clientes.add(new Object[]{nombre, salida});
        logger.log("Cliente agregado: " + nombre + " (Total: " + clientes.size() + ")");
        difundirUsuarios();
    }

    public synchronized void removerCliente(String nombre) {
        clientes.removeIf(cliente -> nombre.equals((String) cliente[0]));
        logger.log("Cliente removido: " + nombre + " (Total: " + clientes.size() + ")");
        difundirUsuarios();
    }

    public void retransmitirMensajePublico(String remitente, String mensaje) {
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
                clientes.remove(cliente);
            }
        }

        logger.log("Mensaje público retransmitido de " + remitente + " a " + enviados + " clientes");
    }

    public synchronized List<String> obtenerNombres() {
        List<String> nombres = new ArrayList<>();
        for (Object[] c : clientes) {
            nombres.add((String) c[0]);
        }
        return nombres;
    }

    private synchronized void difundirUsuarios() {
        List<String> nombres = obtenerNombres();
        for (Object[] cliente : clientes) {
            ObjectOutputStream salidaCliente = (ObjectOutputStream) cliente[1];
            try {
                salidaCliente.writeObject(nombres);
                salidaCliente.flush();
                try { salidaCliente.reset(); } catch (Exception ignore) {}
            } catch (Exception e) {
                logger.logError("Error enviando lista de usuarios a " + (String) cliente[0] + ": " + e.getMessage());
            }
        }
    }

    public void difundirUsuariosAhora() {
        difundirUsuarios();
    }

    public synchronized int getNumeroClientes() {
        return clientes.size();
    }

    public void notificarConexion(String nuevoCliente) {
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
                clientes.remove(cliente);
            }
        }

        logger.log("Notificación de conexión enviada a " + enviados + " clientes");
    }
    
    public boolean enviarMensajePrivado(String remitente, String destinatario, String mensaje) {
        for (Object[] cliente : clientes) {
            String nombreCliente = (String) cliente[0];
            ObjectOutputStream salidaCliente = (ObjectOutputStream) cliente[1];

            if (nombreCliente.equals(destinatario)) {
                try {
                    String mensajeFormateado = "[Privado de " + remitente + "] " + mensaje;
                    salidaCliente.writeObject(mensajeFormateado);
                    
                    logger.log("Mensaje privado entregado de " + remitente + " a " + destinatario);
                    return true;
                } catch (Exception e) {
                    logger.logError("Error enviando mensaje privado a " + destinatario + ": " + e.getMessage());
                    clientes.remove(cliente);
                    return false;
                }
            }
        }

        return false;
    }
}
