package dev.fedichkin;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;

public class TCPConnection {
    private final Socket socket;
    private final Thread rxThread;
    private final BufferedReader in;
    private final BufferedWriter out;
    private final TCPConnectionListener eventListener;


    public TCPConnection(TCPConnectionListener eventListener, String ipAddr, int port) throws IOException {
        this(eventListener, new Socket(ipAddr, port));
    }

    public TCPConnection(TCPConnectionListener eventListener, Socket socket) throws IOException {
        this.eventListener = eventListener;
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader((socket.getInputStream())));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));


        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListener.onConnectionReady(TCPConnection.this);
                    while (!rxThread.isInterrupted()) {
                        String msg = in.readLine();
                        eventListener.onReceiveString(TCPConnection.this, msg);
                    }

                } catch (IOException e) {
                    eventListener.onException(TCPConnection.this, e);
                } finally {
                    eventListener.onDisconnect(TCPConnection.this);
                }

            }
        });
        rxThread.start();

    }

    public synchronized void sendString(String msg) {
        try {
            out.write(msg + "\r\n");
            out.flush();


        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }



    public synchronized void disconnect() {
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
        }
    }

    @Override
    public String toString() {
        return "TCPConnection: " + socket.getInetAddress() + ": " + socket.getPort();
    }

    public static String getIpAddrServer() {
        JSONObject setting = getSettingServer();
        return (String) setting.get("IP_ADDR");
    }

    public static Integer getPortServer() {
        JSONObject setting = getSettingServer();
        return Integer.parseInt(setting.get("PORT").toString());
    }

    private static JSONObject getSettingServer() {
        JSONParser jsonParser = new JSONParser();
        File file = new File("Network/src/main/java/dev/fedichkin/setting.json");

        try (FileReader reader = new FileReader(file)) {
            return (JSONObject) jsonParser.parse(reader);
        } catch (IOException e) {
            // Считаем, что файл у нас всегда есть.
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
