/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author 2dami
 */
public class Servidor {
    private final int PUERTO = 5000;
    private final int MAX_CLIENTES = 5;
    private final Contador conexionesActivas = new Contador();
    private final Logger logger = Logger.getInstance();
    private final ListaClientes listaClientes = new ListaClientes();
    
    public void iniciar() {
        ServerSocket servidor;
        Socket cliente;
        int activas;
        String nombre;
        Thread hilo;
        
        try {
            servidor = new ServerSocket(PUERTO);
            System.out.println("Servidor iniciado. Esperando clientes...");
            logger.log("SERVIDOR INICIADO - Puerto: " + PUERTO);

            while (true) {
                try {
                    cliente = servidor.accept();

                    if (conexionesActivas.get() >= MAX_CLIENTES) {
                        ObjectOutputStream salida = new ObjectOutputStream(cliente.getOutputStream());
                        salida.writeObject("Servidor lleno. Intenta más tarde.");
                        cliente.close();
                        
                        System.out.println("Conexión rechazada: límite de " + MAX_CLIENTES + " alcanzado.");
                        logger.log("CONEXIÓN RECHAZADA - Límite de " + MAX_CLIENTES + " clientes alcanzado");
                    } else {
                        activas = conexionesActivas.incrementar();

                        ObjectInputStream entradaCliente = null;
                        
                        try {
                            entradaCliente = new ObjectInputStream(cliente.getInputStream());
                            Object nombreObj = entradaCliente.readObject();
                            if (nombreObj instanceof String && !((String) nombreObj).trim().isEmpty()) {
                                nombre = (String) nombreObj;
                            } else {
                                nombre = "Usuario";
                            }
                        } catch (Exception e) {
                            nombre = "Usuario";
                        }
                        
                        System.out.println("Cliente " + nombre + " conectado desde " + cliente.getInetAddress().getHostAddress() + ". Conexiones activas: " + activas);
                        logger.logUserConnected(nombre, cliente.getInetAddress().getHostAddress());

                        hilo = new Thread(new ManejadorCliente(cliente, nombre, conexionesActivas, logger, listaClientes, entradaCliente));
                        hilo.start();
                    }

                } catch (IOException e) {
                    System.out.println("Error aceptando conexión: " + e.getMessage());
                    logger.logError("Error aceptando conexión: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.out.println("Error en el servidor: " + e.getMessage());
            logger.logError("Error crítico en el servidor: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        Servidor s = new Servidor();
        s.iniciar();
    }
}
