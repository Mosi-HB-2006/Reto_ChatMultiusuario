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
    private  ObjectInputStream entrada;
    private Monitor monitor;

    public ManejadorCliente(Socket socket, String clientName, Contador conexionesActivas, Logger logger, ListaClientes listaClientes, ObjectInputStream entrada, Monitor monitor) {
        this.socket = socket;
        this.clientName = clientName;
        this.conexionesActivas = conexionesActivas;
        this.logger = logger;
        this.listaClientes = listaClientes;
        this.entrada = entrada;
        this.monitor = monitor;
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
            entrada = this.entrada; // reutilizar la entrada proporcionada por el servidor

            // Agregar cliente a la lista de clientes conectados
            listaClientes.agregarCliente(clientName, salida);

            // Enviar la lista de usuarios al nuevo cliente inmediatamente
            try {
                java.util.List<String> snapshot = listaClientes.obtenerNombresSnapshot();
                salida.writeObject(snapshot);
                salida.flush();
                try { salida.reset(); } catch (Exception ignore) {}
            } catch (Exception e) {
                logger.logError("Error enviando lista inicial a " + clientName + ": " + e.getMessage());
            }

            // Notificar a los demás clientes que este usuario se ha conectado
            listaClientes.notificarConexion(clientName);

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
                        String trimmed = mensajeCliente.trim();

                        // Comando para solicitar la lista de usuarios (no loguear ni retransmitir)
                        if ("GET_USERS".equalsIgnoreCase(trimmed)) {
                            try {
                                java.util.List<String> snapshot = listaClientes.obtenerNombresSnapshot();
                                salida.writeObject(snapshot);
                                salida.flush();
                                try { salida.reset(); } catch (Exception ignore) {}
                            } catch (Exception e) {
                                logger.logError("Error enviando GET_USERS a " + clientName + ": " + e.getMessage());
                            }
                        } else {
                            System.out.println("[Cliente " + clientName + "] dice: " + mensajeCliente);

                            // Verificar si es un mensaje privado (empieza con @usuario)
                            if (mensajeCliente.startsWith("@")) {
                                // Procesar mensaje privado
                                int espacioIndex = mensajeCliente.indexOf(" ");
                                if (espacioIndex > 1) { // Debe haber al menos un carácter después del @
                                    String destinatario = mensajeCliente.substring(1, espacioIndex);
                                    String mensajePrivado = mensajeCliente.substring(espacioIndex + 1).trim();

                                    if (!mensajePrivado.isEmpty()) {
                                        boolean entregado = listaClientes.enviarMensajePrivado(clientName, destinatario, mensajePrivado);

                                        // Informar al remitente si el mensaje fue entregado
                                        try {
                                            if (entregado) {
                                                salida.writeObject("Mensaje privado enviado a " + destinatario);
                                            } else {
                                                salida.writeObject("Error: Usuario '" + destinatario + "' no encontrado o no conectado");
                                            }
                                        } catch (Exception e) {
                                            logger.logError("Error enviando confirmación a " + clientName + ": " + e.getMessage());
                                        }

                                        // Registrar el mensaje privado en el log
                                        logger.logPrivateMessage(clientName, destinatario, mensajePrivado);
                                    } else {
                                        try {
                                            salida.writeObject("Error: El mensaje privado no puede estar vacío");
                                        } catch (Exception e) {
                                            logger.logError("Error enviando error a " + clientName + ": " + e.getMessage());
                                        }
                                    }
                                } else {
                                    try {
                                        salida.writeObject("Error: Formato de mensaje privado incorrecto. Use: @usuario mensaje");
                                    } catch (Exception e) {
                                        logger.logError("Error enviando formato incorrecto a " + clientName + ": " + e.getMessage());
                                    }
                                }
                            } else {
                                // Es un mensaje público - retransmitir a todos los demás clientes
                                listaClientes.retransmitirMensajePublico(clientName, mensajeCliente);

                                // Registrar el mensaje en el log
                                logger.logPublicMessage(clientName, mensajeCliente);
                                monitor.actualizarUltimoMensaje(clientName + ": " + mensajeCliente);
                            }
                        }
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
