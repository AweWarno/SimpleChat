package dev.fedichkin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class Client extends JFrame implements ActionListener, TCPConnectionListener {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client();
            }
        });
    }

    private final JTextArea log = new JTextArea();
    private final JTextField fieldNickName = new JTextField();
    private final JTextField fieldInput =  new JTextField();



    private TCPConnection connection;

    private Client() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        fieldInput.addActionListener(this);
        fieldNickName.setText("Username");

        log.setEditable(false);
        log.setLineWrap(true);
        add(log, BorderLayout.CENTER);
        add(fieldInput, BorderLayout.SOUTH);
        add(fieldNickName, BorderLayout.NORTH);

        setVisible(true);
        try {
            connection = new TCPConnection(this, TCPConnection.getIpAddrServer(), TCPConnection.getPortServer());
        } catch (IOException e) {
            printMsg("Connection exception: " + e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = fieldInput.getText();
        if (msg.isEmpty()) return;
        if (msg.equals("/exit")) { System.exit(0); }

        fieldInput.setText(null);
        connection.sendString(fieldNickName.getText() + ": " + msg);
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMsg("Connection ready...");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String msg) {
        printMsg(msg);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMsg("Connection close...");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMsg("Connection exception: " + e);
    }

    private synchronized void printMsg(String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\r\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }
}
