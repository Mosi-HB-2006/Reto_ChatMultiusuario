/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 *
 * @author Mosi
 */
public class ChatWindowFinal extends javax.swing.JFrame {

    private String username;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private JTextArea publicTextArea;
    private List<String> publicBuffer;
    private List<String> latestUsers;
    private JTextArea privateTextArea;
    private List<String> privateBuffer;

    /**
     * Creates new form ChatWindowFinal
     */
    public ChatWindowFinal() {
        initComponents();
    }

    public ChatWindowFinal(String username, Socket socket, ObjectOutputStream out, ObjectInputStream in) {
        this.username = username;
        this.socket = socket;
        this.out = out;
        this.in = in;
        initComponents();
        try {
            if (this.username != null) {
                userLabel.setText("Usuario: " + this.username);
            }
        } catch (Exception ignore) {}

        // Initialize buffer and UI area
        publicBuffer = Collections.synchronizedList(new ArrayList<>());
        latestUsers = Collections.synchronizedList(new ArrayList<>());
        privateBuffer = Collections.synchronizedList(new ArrayList<>());
        publicTextArea = new JTextArea();
        publicTextArea.setEditable(false);
        publicScrollPane.setViewportView(publicTextArea);
        privateTextArea = new JTextArea();
        privateTextArea.setEditable(false);
        privateScrollPane.setViewportView(privateTextArea);

        // Background reader: store messages into buffer
        Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Object obj = in.readObject();
                        if (obj != null) {
                            if (obj instanceof List) {
                                List<?> list = (List<?>) obj;
                                boolean allStrings = areAllStrings(list);
                                if (allStrings) {
                                    synchronized (latestUsers) {
                                        latestUsers.clear();
                                        for (Object o : list) { latestUsers.add((String) o); }
                                    }
                                } else {
                                    publicBuffer.add(String.valueOf(obj));
                                }
                            } else {
                                String s = String.valueOf(obj);
                                if (s.startsWith("USERS:")) {
                                    String rest = s.substring("USERS:".length());
                                    String[] parts = rest.split(",");
                                    synchronized (latestUsers) {
                                        latestUsers.clear();
                                        for (String p : parts) {
                                            String u = p.trim();
                                            if (!u.isEmpty()) latestUsers.add(u);
                                        }
                                    }
                                } else if (s.trim().equalsIgnoreCase("GET_USERS")) {
                                    // Ignorar comandos de control si llegaran por alguna razón
                                } else if (s.startsWith("[Privado de ")) {
                                    synchronized (privateBuffer) {
                                        privateBuffer.add(s);
                                    }
                                } else if (s.startsWith("Mensaje privado enviado a ")) {
                                    // No mostrar confirmaciones del servidor, ya realizamos eco local
                                } else {
                                    publicBuffer.add(s);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // Socket closed or error; stop thread silently
                }
            }
        });
        reader.start();

        // UI refresh timer: every 5 seconds, render buffer to text area
        Timer refreshTimer = new Timer(5000, evt -> {
            List<String> snapshot;
            synchronized (publicBuffer) {
                snapshot = new ArrayList<>(publicBuffer);
            }
            String text = String.join("\n", snapshot);
            SwingUtilities.invokeLater(() -> publicTextArea.setText(text));
        });
        refreshTimer.setRepeats(true);
        refreshTimer.start();

        // Private area refresh timer
        Timer privateTimer = new Timer(3000, evt -> {
            List<String> snapshot;
            synchronized (privateBuffer) {
                snapshot = new ArrayList<>(privateBuffer);
            }
            String text = String.join("\n", snapshot);
            SwingUtilities.invokeLater(() -> privateTextArea.setText(text));
        });
        privateTimer.setRepeats(true);
        privateTimer.start();

        // Users combo refresh timer
        Timer usersTimer = new Timer(3000, evt -> {
            List<String> usersSnapshot;
            synchronized (latestUsers) {
                usersSnapshot = new ArrayList<>(latestUsers);
            }
            refreshUsersComboBox(usersSnapshot);
        });
        usersTimer.setRepeats(true);
        usersTimer.start();

        // Request user list on start (in case initial broadcast was missed)
        try {
            if (out != null) {
                out.writeObject("GET_USERS");
                out.flush();
            }
        } catch (Exception ignore) {}
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        messageField = new javax.swing.JTextField();
        sendBtn = new javax.swing.JButton();
        userLabel = new javax.swing.JLabel();
        publicRadioBtn = new javax.swing.JRadioButton();
        privateRadioBtn = new javax.swing.JRadioButton();
        userComboBox = new javax.swing.JComboBox<>();
        privateScrollPane = new javax.swing.JScrollPane();
        publicScrollPane = new javax.swing.JScrollPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        messageField.setToolTipText("");
        messageField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                messageFieldActionPerformed(evt);
            }
        });

        sendBtn.setText("Enviar");
        sendBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendBtnActionPerformed(evt);
            }
        });

        userLabel.setText("Usuario: ");

        publicRadioBtn.setText("Publico");
        publicRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                publicRadioBtnActionPerformed(evt);
            }
        });

        privateRadioBtn.setText("Privado");

        userComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(userLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(publicRadioBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(privateRadioBtn))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(messageField, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(sendBtn))
                            .addComponent(publicScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(privateScrollPane, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(userComboBox, 0, 171, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(privateScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(publicScrollPane)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(messageField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sendBtn)
                    .addComponent(userComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userLabel)
                    .addComponent(publicRadioBtn)
                    .addComponent(privateRadioBtn))
                .addGap(11, 11, 11))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void messageFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_messageFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_messageFieldActionPerformed

    private void sendBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendBtnActionPerformed
        try {
            if (out != null) {
                String msg = messageField.getText();
                if (msg != null && !msg.trim().isEmpty()) {
                    String toSend = msg.trim();
                    boolean canSend = true;
                    boolean isPrivate = false;
                    String privateEcho = null;
                    if (privateRadioBtn.isSelected()) {
                        String target = (String) userComboBox.getSelectedItem();
                        if (target == null || target.trim().isEmpty()) {
                            JOptionPane.showMessageDialog(this, "Selecciona un usuario para mensaje privado", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                            canSend = false;
                        } else {
                            isPrivate = true;
                            privateEcho = "[Privado de " + (username != null ? username : "Yo") + "] " + toSend;
                            toSend = "@" + target.trim() + " " + toSend;
                        }
                    }
                    if (canSend) {
                        out.writeObject(toSend);
                        if (isPrivate && privateEcho != null) {
                            synchronized (privateBuffer) { privateBuffer.add(privateEcho); }
                        }
                        messageField.setText("");
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error enviando mensaje: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_sendBtnActionPerformed

    private void publicRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publicRadioBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_publicRadioBtnActionPerformed

    private void refreshUsersComboBox(List<String> users) {
        SwingUtilities.invokeLater(() -> {
            List<String> items = new ArrayList<>();
            String self = username != null ? username.trim() : null;
            if (users != null) {
                for (String u : users) {
                    if (u != null) {
                        String v = u.trim();
                        if (!v.isEmpty() && (self == null || !v.equals(self)) && !items.contains(v)) {
                            items.add(v);
                        }
                    }
                }
            }
            String sel = (String) userComboBox.getSelectedItem();
            javax.swing.DefaultComboBoxModel<String> model = new javax.swing.DefaultComboBoxModel<>(items.toArray(new String[0]));
            userComboBox.setModel(model);
            if (sel != null && items.contains(sel)) {
                userComboBox.setSelectedItem(sel);
            } else if (!items.isEmpty()) {
                userComboBox.setSelectedIndex(0);
            }
        });
    }

    private boolean areAllStrings(List<?> list) {
        boolean result = true;
        for (Object o : list) {
            if (!(o instanceof String)) {
                result = false;
            }
        }
        return result;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ChatWindowFinal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ChatWindowFinal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ChatWindowFinal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ChatWindowFinal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ChatWindowFinal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField messageField;
    private javax.swing.JRadioButton privateRadioBtn;
    private javax.swing.JScrollPane privateScrollPane;
    private javax.swing.JRadioButton publicRadioBtn;
    private javax.swing.JScrollPane publicScrollPane;
    private javax.swing.JButton sendBtn;
    private javax.swing.JComboBox<String> userComboBox;
    private javax.swing.JLabel userLabel;
    // End of variables declaration//GEN-END:variables
}
