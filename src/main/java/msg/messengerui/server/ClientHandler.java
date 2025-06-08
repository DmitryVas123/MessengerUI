package msg.messengerui.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private List<ClientHandler> clients;
    private String username;

    public ClientHandler(Socket socket, List<ClientHandler> clients) throws IOException {
        this.socket = socket;
        this.clients = clients;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void run() {
        try {
            String input;
            try {
                while((input = this.in.readLine()) != null) {
                    System.out.println("Принято: " + input);
                    if (input.startsWith("<auth")) {
                        Matcher m = Pattern.compile("username=\"(.*?)\"").matcher(input);
                        if (m.find()) {
                            this.username = m.group(1);
                            System.out.println("Пользователь вошёл: " + this.username);
                            this.out.println("<status type=\"auth_success\" />");
                        }
                    } else if (input.startsWith("<message")) {
                        Matcher m = Pattern.compile("to=\"(.*?)\".*?>(.*?)</message>").matcher(input);
                        if (m.find()) {
                            String to = m.group(1);
                            String text = m.group(2);
                            boolean found = false;

                            for(ClientHandler client : this.clients) {
                                if (to.equals(client.username)) {
                                    client.out.println("<message from=\"" + this.username + "\" text=\"" + text + "\" />");
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                this.out.println("<error type=\"user_not_found\" to=\"" + to + "\" />");
                            }
                        }
                    } else if (input.startsWith("<disconnect")) {
                        System.out.println(this.username + " отключился.");
                        break;
                    }
                }
            } catch (IOException var16) {
                System.out.println("Ошибка клиента " + this.username);
            }
        } finally {
            try {
                this.socket.close();
            } catch (IOException var15) {
            }

            this.clients.remove(this);
        }

    }
}