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

    @FXML
    private TextField toField;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private String username; // можно позже заменить на окно авторизации

    @FXML
    public void initialize() {
        //connectToServer();
        sendButton.setOnAction(event -> sendMessage());
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void connectToServer() {
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
        String to = toField.getText().trim();

        if (message.isEmpty()) {
            addNotification("Введите сообщение.");
            return;
        }

        if (to.isEmpty()) {
            addNotification("Введите получателя.");
            return;
        }

        addChatMessage("Вы → " + to, message, true);
        messageField.clear();

        out.println(XMLBuilder.buildMessage(username, to, message));
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
