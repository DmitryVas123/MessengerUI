package msg.messengerui.client;

import msg.messengerui.common.XMLBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12345);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        Scanner scanner = new Scanner(System.in);

        // Сначала имя
        System.out.print("Введите имя пользователя: ");
        String username = scanner.nextLine();
        out.println(XMLBuilder.buildAuth(username, "1234"));

        // Поток получения сообщений от сервера
        new Thread(() -> {
            try {
                String serverMsg;
                while ((serverMsg = in.readLine()) != null) {
                    if (serverMsg.startsWith("<message")) {
                        String from = serverMsg.replaceAll(".*from=\"(.*?)\".*", "$1");
                        String text = serverMsg.replaceAll(".*text=\"(.*?)\".*", "$1");
                        System.out.println(from + ": " + text);
                    } else if (serverMsg.startsWith("<error")) {
                        String type = serverMsg.replaceAll(".*type=\"(.*?)\".*", "$1");
                        String to = serverMsg.replaceAll(".*to=\"(.*?)\".*", "$1");
                        System.out.println("Ошибка: не найден пользователь " + to + " (" + type + ")");
                    } else if (serverMsg.startsWith("<status")) {
                        String type = serverMsg.replaceAll(".*type=\"(.*?)\".*", "$1");
                        System.out.println("Сервер: статус: " + type);
                    } else {
                        System.out.println("Сервер: " + serverMsg); // на случай других сообщений
                    }
                }
            } catch (IOException e) {
                System.out.println("Ошибка чтения от сервера");
            }
        }).start();

        // Поток отправки сообщений
        new Thread(() -> {
            try {
                while (true) {
                    String input = scanner.nextLine();
                    if (input.equals("/exit")) {
                        out.println(XMLBuilder.buildDisconnect(username));
                        socket.close(); // Закрываем соединение
                        break;
                    }
                    if (input.contains(":")) {
                        String[] parts = input.split(":", 2);
                        String to = parts[0];
                        String msg = parts[1];
                        out.println(XMLBuilder.buildMessage(username, to, msg));
                    } else {
                        System.out.println("Формат: <имя получателя>:<сообщение>");
                    }
                }
            } catch (IOException e) {
                System.out.println("Ошибка ввода");
            }
        }).start();
    }
}
