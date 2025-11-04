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

    public ManejadorCliente(Socket socket, String clientName, Contador conexionesActivas, Logger logger, ListaClientes listaClientes, ObjectInputStream entrada) {
        this.socket = socket;
        this.clientName = clientName;
        this.conexionesActivas = conexionesActivas;
        this.logger = logger;
        this.listaClientes = listaClientes;
        this.entrada = entrada;
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
            entrada = this.entrada;

            listaClientes.agregarCliente(clientName, salida);

            try {
                java.util.List<String> snapshot = listaClientes.obtenerNombres();
                salida.writeObject(snapshot);
                salida.flush();
                salida.reset();
            } catch (Exception e) {
                logger.logError("Error enviando lista inicial a " + clientName + ": " + e.getMessage());
            }

            listaClientes.notificarConexion(clientName);

            salida.writeObject("Conexión exitosa al servidor. Bienvenido " + clientName);

            obj = entrada.readObject();
            if (obj instanceof String) {
                saludoCliente = (String) obj;
            } else {
                saludoCliente = String.valueOf(obj);
            }
            
            System.out.println("[Cliente " + clientName + "] dice: " + saludoCliente);
            logger.logPublicMessage(clientName, saludoCliente);

            while (true) {
                obj = entrada.readObject();

                if (obj instanceof String) {
                    mensajeCliente = (String) obj;

                    if (!mensajeCliente.trim().isEmpty()) {
                        String trimmed = mensajeCliente.trim();

                        if ("GET_USERS".equalsIgnoreCase(trimmed)) {
                            try {
                                java.util.List<String> users = listaClientes.obtenerNombres();
                                salida.writeObject(users);
                                salida.flush();
                                salida.reset();
                            } catch (Exception e) {
                                logger.logError("Error enviando GET_USERS a " + clientName + ": " + e.getMessage());
                            }
                        } else {
                            System.out.println("[Cliente " + clientName + "] dice: " + mensajeCliente);

                            if (mensajeCliente.startsWith("@")) {
                                int espacioIndex = mensajeCliente.indexOf(" ");
                                
                                if (espacioIndex > 1) {
                                    String destinatario = mensajeCliente.substring(1, espacioIndex);
                                    String mensajePrivado = mensajeCliente.substring(espacioIndex + 1).trim();

                                    if (!mensajePrivado.isEmpty()) {
                                        boolean entregado = listaClientes.enviarMensajePrivado(clientName, destinatario, mensajePrivado);

                                        try {
                                            if (entregado) {
                                                salida.writeObject("Mensaje privado enviado a " + destinatario);
                                            } else {
                                                salida.writeObject("Error: Usuario '" + destinatario + "' no encontrado o no conectado");
                                            }
                                        } catch (Exception e) {
                                            logger.logError("Error enviando confirmación a " + clientName + ": " + e.getMessage());
                                        }

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
                                listaClientes.retransmitirMensajePublico(clientName, mensajeCliente);
                                logger.logPublicMessage(clientName, mensajeCliente);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            if (e.getClass().getSimpleName().equals("IOException")) {
                System.out.println("[Cliente " + clientName + "] se desconectó");
            } else {
                System.out.println("[Cliente " + clientName + "] Error: " + e.getMessage());
                logger.logError("Cliente " + clientName + ": " + e.getMessage());
            }
        } finally {
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
