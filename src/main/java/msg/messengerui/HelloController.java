package msg.messengerui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.Socket;
import java.util.*;

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

    @FXML
    private ListView<String> userList;

    @FXML
    private ScrollPane chatScroll;

    @FXML
    private ScrollPane notifScroll;

    @FXML
    private VBox messageBox;

    //@FXML
    //private ToggleButton themeToggle;

    @FXML
    private Scene scene;

    private Map<String, List<String>> chats = new HashMap<>();

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private String username; // можно позже заменить на окно авторизации

    @FXML
    public void initialize() {
        sendButton.setOnAction(event -> sendMessage());
        messageField.setOnAction(event -> sendMessage());
        messageField.setPromptText("Select an user");
        userList.setPlaceholder(new Label("No online users"));

//        themeToggle.setOnAction(event -> {
//            scene.getStylesheets().clear();
//            if (themeToggle.isSelected()) {
//                scene.getStylesheets().add(getClass().getResource("dark-theme.css").toExternalForm());
//                themeToggle.setText("Day");
//            } else {
//                scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
//                themeToggle.setText("Night");
//            }
//        });

        userList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                toField.setText(newVal);
                loadChatForUser(newVal);
                messageField.setDisable(false);
                sendButton.setDisable(false);
                messageField.setPromptText("Write a message");
            }
        });

        messageField.setDisable(true);
        sendButton.setDisable(true);
        toField.setEditable(false);


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
                    Platform.runLater(() -> addNotification("Connection is lost"));
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();

        } catch (IOException e) {
            addNotification("Error with connection to server");
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        String to = toField.getText().trim();

        if (message.isEmpty()) {
            addNotification("Enter a message");
            return;
        }

        if (to.isEmpty()) {
            addNotification("First, select the user from the list.");
            return;
        }

        addChatMessage("You → " + to, message, true);
        chats.computeIfAbsent(to, k -> new ArrayList<>()).add("You: " + message);
        messageField.clear();

        out.println(XMLBuilder.buildMessage(username, to, message));
    }

    private void handleServerMessage(String msg) {
        if (msg.startsWith("<message")) {
            String from = msg.replaceAll(".*from=\"(.*?)\".*", "$1");
            String text = msg.replaceAll(".*text=\"(.*?)\".*", "$1");
            Platform.runLater(() -> {
                chats.computeIfAbsent(from, k -> new ArrayList<>()).add(from + ": " + text);
                if (from.equals(userList.getSelectionModel().getSelectedItem())) {
                    addChatMessage(from, text, false);
                }
            });

        } else if (msg.startsWith("<error")) {
            String to = msg.replaceAll(".*to=\"(.*?)\".*", "$1");
            Platform.runLater(() -> addNotification("Error: the user is not found " + to));

        } else if (msg.startsWith("<status")) {
            String user = msg.replaceAll(".*user=\"(.*?)\".*", "$1");
            String status = msg.replaceAll(".*status=\"(.*?)\".*", "$1");

            if (msg.equals("<status type=\"auth_success\" />")) {
                return;
            }
            Platform.runLater(() -> {
                addNotification("User " + user + " is " + status);
                if (status.equals("online")) {
                    if (!userList.getItems().contains(user)) {
                        userList.getItems().add(user);
                    }
                } else if (status.equals("offline")) {
                    userList.getItems().remove(user);
                }
            });
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
            System.out.println("Error with closing the connection");
        }
    }

    public void addChatMessage(String sender, String message, boolean isOutgoing) {
        String time = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

        Label senderLabel = new Label(isOutgoing ? "You" : sender);
        senderLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: #000000;");
        messageLabel.setWrapText(true);

        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");

        VBox messageBox = new VBox(senderLabel, messageLabel, timeLabel);
        messageBox.setSpacing(2);
        messageBox.setStyle("-fx-background-color: " + (isOutgoing ? "#D1F0FF" : "#E8E8E8") +
                "; -fx-padding: 8; -fx-background-radius: 8; -fx-max-width: 300; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0.0, 0, 1);");
        messageBox.setMaxWidth(300);

        HBox container = new HBox(messageBox);
        container.setMaxWidth(Double.MAX_VALUE);
        container.setStyle("-fx-padding: 5;");

        if (isOutgoing) {
            container.setStyle("-fx-alignment: CENTER_RIGHT; -fx-padding: 5;");
        } else {
            container.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5;");
        }

        chatBox.getChildren().add(container);
        scrollChatToBottom();
    }


    public void addNotification(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: gray; -fx-font-style: italic; -fx-padding: 3;");
        messageBox.getChildren().add(label);
        scrollNotificationsToBottom();
    }

    private void loadChatForUser(String user) {
        chatBox.getChildren().clear();

        List<String> messages = chats.getOrDefault(user, new ArrayList<>());
        for (String message : messages) {
            Label label = new Label(message);
            label.setStyle("-fx-background-color: " + (message.startsWith("You: ") ? "#D1F0FF" : "#E8E8E8") + "; -fx-padding: 5; -fx-background-radius: 8;");
            chatBox.getChildren().add(label);
        }
    }

    private void scrollChatToBottom() {
        Platform.runLater(() -> {
            chatBox.layout();
            if (chatScroll != null) {
                chatScroll.setVvalue(1.0);
            }
        });
    }

    private void scrollNotificationsToBottom() {
        Platform.runLater(() -> {
            messageBox.layout();
            if (notifScroll != null) {
                notifScroll.setVvalue(1.0);
            }
        });
    }


    public void setScene(Scene scene) {
        this.scene = scene;
    }
}
