package msg.messengerui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.Socket;
import msg.messengerui.common.XMLBuilder;

public class HelloController {
    @FXML
    private VBox chatBox;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendButton;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private final String username = "User"; // можно позже заменить на окно авторизации

    @FXML
    public void initialize() {
        connectToServer();
        sendButton.setOnAction(event -> sendMessage());
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Отправка авторизации
            out.println(XMLBuilder.buildAuth(username, "1234"));

            // Прием сообщений в фоновом потоке
            Thread readerThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        handleServerMessage(line);
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> addNotification("Соединение потеряно."));
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();

        } catch (IOException e) {
            addNotification("Ошибка подключения к серверу.");
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();

        if (!message.isEmpty()) {
            addChatMessage("Вы", message, true);
            messageField.clear();

            // Отправка самому себе (можно расширить на других получателей)
            out.println(XMLBuilder.buildMessage(username, username, message));
        } else {
            addNotification("Введите сообщение.");
        }
    }

    private void handleServerMessage(String msg) {
        if (msg.startsWith("<message")) {
            String from = msg.replaceAll(".*from=\"(.*?)\".*", "$1");
            String text = msg.replaceAll(".*text=\"(.*?)\".*", "$1");
            Platform.runLater(() -> addChatMessage(from, text, false));
        } else if (msg.startsWith("<error")) {
            String to = msg.replaceAll(".*to=\"(.*?)\".*", "$1");
            Platform.runLater(() -> addNotification("Ошибка: не найден пользователь " + to));
        } else if (msg.startsWith("<status")) {
            String user = msg.replaceAll(".*user=\"(.*?)\".*", "$1");
            String status = msg.replaceAll(".*status=\"(.*?)\".*", "$1");
            Platform.runLater(() -> addNotification("Пользователь " + user + " теперь " + status));
        }
    }

    public void closeConnection() {
        try {
            if (out != null) {
                out.println(XMLBuilder.buildDisconnect(username));
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Ошибка при закрытии соединения.");
        }
    }

    public void addChatMessage(String sender, String message, boolean isOutgoing) {
        Label label = new Label((isOutgoing ? "Вы: " : sender + ": ") + message);
        label.setStyle("-fx-background-color: " + (isOutgoing ? "#D1F0FF" : "#E8E8E8") + "; -fx-padding: 5; -fx-background-radius: 8;");
        chatBox.getChildren().add(label);
    }

    public void addNotification(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: gray; -fx-font-style: italic; -fx-padding: 3;");
        chatBox.getChildren().add(label);
    }
}
