/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ManejadorCliente implements Runnable {
    private  Socket socket;
    private  String clientName;
    private  Contador conexionesActivas;
    private  Logger logger;
    private  ListaClientes listaClientes;

    public ManejadorCliente(Socket socket, String clientName, Contador conexionesActivas, Logger logger, ListaClientes listaClientes) {
        this.socket = socket;
        this.clientName = clientName;
        this.conexionesActivas = conexionesActivas;
        this.logger = logger;
        this.listaClientes = listaClientes;
    }

    @Override
    public void run() {
        String mensajeCliente;
        String saludoCliente;
        Object obj;
        Socket s;
        ObjectOutputStream salida;
        ObjectInputStream entrada;

        try {
            s = this.socket;
            salida = new ObjectOutputStream(s.getOutputStream());
            entrada = new ObjectInputStream(s.getInputStream());

            // Agregar cliente a la lista de clientes conectados
            listaClientes.agregarCliente(clientName, salida);

            // Enviar confirmación al cliente
            salida.writeObject("Conexión exitosa al servidor. Bienvenido " + clientName);

            // Recibir saludo del cliente
            obj = entrada.readObject();
            if (obj instanceof String) {
                saludoCliente = (String) obj;
            } else {
                saludoCliente = String.valueOf(obj);
            }
            System.out.println("[Cliente " + clientName + "] dice: " + saludoCliente);
            logger.logPublicMessage(clientName, saludoCliente);

            // Bucle principal para recibir mensajes del cliente
            while (true) {
                obj = entrada.readObject();

                if (obj instanceof String) {
                    mensajeCliente = (String) obj;

                    // Si el mensaje no está vacío, procesarlo
                    if (!mensajeCliente.trim().isEmpty()) {
                        System.out.println("[Cliente " + clientName + "] dice: " + mensajeCliente);

                        // Retransmitir mensaje público a todos los demás clientes
                        listaClientes.retransmitirMensajePublico(clientName, mensajeCliente);

                        // Registrar el mensaje en el log
                        logger.logPublicMessage(clientName, mensajeCliente);
                    }
                }
            }

        } catch (Exception e) {
            // Si es IOException, probablemente el cliente se desconectó
            if (e.getClass().getSimpleName().equals("IOException")) {
                System.out.println("[Cliente " + clientName + "] se desconectó");
            } else {
                System.out.println("[Cliente " + clientName + "] Error: " + e.getMessage());
                logger.logError("Cliente " + clientName + ": " + e.getMessage());
            }
        } finally {
            // Remover cliente de la lista y cerrar conexiones
            listaClientes.removerCliente(clientName);
            int restantes = conexionesActivas.decrementar();
            System.out.println("[Cliente " + clientName + "] finalizado. Conexiones activas: " + restantes);
            logger.logUserDisconnected(clientName);

            try {
                socket.close();
            } catch (Exception e) {
                System.err.println("Error cerrando socket de " + clientName + ": " + e.getMessage());
            }
        }
    }
}
