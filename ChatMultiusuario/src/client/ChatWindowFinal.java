/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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

        userLabel.setText("Usuario: " + this.username);

        publicBuffer = Collections.synchronizedList(new ArrayList<>());
        latestUsers = Collections.synchronizedList(new ArrayList<>());
        privateBuffer = Collections.synchronizedList(new ArrayList<>());

        publicTextArea = new JTextArea();
        publicTextArea.setEditable(false);
        publicScrollPane.setViewportView(publicTextArea);

        privateTextArea = new JTextArea();
        privateTextArea.setEditable(false);
        privateScrollPane.setViewportView(privateTextArea);

        Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    while (true) {

                        Object obj = in.readObject();

                        if (obj != null) {
                            if (obj instanceof List) {
                                List<String> list = (List<String>) obj;
                                latestUsers.clear();
                                for (Object o : list) {
                                    latestUsers.add((String) o);
                                }

                            } else {
                                String s = String.valueOf(obj);
                                if (s.startsWith("USERS:")) {
                                    String rest = s.substring("USERS:".length());
                                    String[] parts = rest.split(",");
                                    latestUsers.clear();
                                    for (String p : parts) {
                                        String u = p.trim();
                                        if (!u.isEmpty()) {
                                            latestUsers.add(u);
                                        }
                                    }
                                } else if (s.startsWith("[Privado de ")) {
                                    privateBuffer.add(s);
                                } else {
                                    publicBuffer.add(s);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // Socket closed or error; stop thread silently
                    try {
                        System.out.println("Error occured, closing connections");
                        socket.close();
                        out.close();
                        in.close();
                    } catch (IOException ex) {
                        Logger.getLogger(ChatWindowFinal.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });

        reader.start();

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

        Timer usersTimer = new Timer(3000, evt -> {
            List<String> usersSnapshot;
            synchronized (latestUsers) {
                usersSnapshot = new ArrayList<>(latestUsers);
            }
            refreshUsersComboBox(usersSnapshot);
        });
        usersTimer.setRepeats(true);
        usersTimer.start();

        if (out != null) {
            try {
                out.writeObject("GET_USERS");
                out.flush();
            } catch (IOException ex) {
                Logger.getLogger(ChatWindowFinal.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
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

        buttonGroup1.add(publicRadioBtn);
        publicRadioBtn.setSelected(true);
        publicRadioBtn.setText("Publico");
        publicRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                publicRadioBtnActionPerformed(evt);
            }
        });

        buttonGroup1.add(privateRadioBtn);
        privateRadioBtn.setText("Privado");

        userComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "User 1", "User 2", "User 3", "User 4" }));

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
                            privateBuffer.add(privateEcho);
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
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
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
