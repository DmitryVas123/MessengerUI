package msg.messengerui.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int PORT = 12345;
    private static final List<ClientHandler> clients = new ArrayList();

    public Server() {
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Server is launched at the port 12345");

        while(true) {
            Socket socket = serverSocket.accept();
            System.out.println("New connection: " + String.valueOf(socket.getInetAddress()));
            ClientHandler handler = new ClientHandler(socket, clients);
            clients.add(handler);
            (new Thread(handler)).start();
        }
    }
}