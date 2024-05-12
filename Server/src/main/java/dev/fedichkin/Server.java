package dev.fedichkin;

import java.io.*;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Server implements TCPConnectionListener {

    public static void main(String[] args) {

        new Server();
    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();

    private Server() {
        getServerSetting();
        System.out.println("Server running ....");
        try (ServerSocket serverSocket = new ServerSocket(TCPConnection.getPortServer())) {
            while (true) {
                try {
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e) {
                    System.out.println("TCPConnection exception: " + e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveMessage(String msg) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd-yy HH-mm");
        String result = dtf.format(LocalDateTime.now()) + ": " + msg + "\r\n";

        File file = new File("Server/src/main/java/dev/fedichkin/log.log");
        try (Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)))) {
            out.append(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sendToAllConnection(msg);
    }

    private void sendToAllConnection(String msg) {
        System.out.println(msg);
        connections.forEach(client -> client.sendString(msg));
    }

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        saveMessage("Client connected: " + tcpConnection);
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String msg) {
        saveMessage(msg);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        saveMessage("Client disconnected: " + tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception: " + e);
    }

    private void getServerSetting() {

    }

}
