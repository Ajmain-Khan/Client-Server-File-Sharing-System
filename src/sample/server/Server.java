package sample.server;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static ServerSocket SERVER_SOCKET = null;
    public final static int SERVER_PORT = 6000;

    public Server(int port) {
        try {
            this.SERVER_SOCKET = new ServerSocket(port);
            System.out.println("File Server running\nListening to port " + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server(SERVER_PORT);
        while (true) {
            try {
                Socket CLIENT_SOCKET = SERVER_SOCKET.accept();
                Thread threadListener = new Thread(new ClientConnectionHandler(CLIENT_SOCKET));
                threadListener.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
