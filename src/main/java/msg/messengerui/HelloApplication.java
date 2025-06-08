package msg.messengerui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Parent root = fxmlLoader.load(); // Сначала загружаем FXML
        HelloController controller = fxmlLoader.getController(); // Теперь можно получить контроллер

        // Диалог ввода имени
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Авторизация");
        dialog.setHeaderText("Введите имя пользователя");
        dialog.setContentText("Имя:");

        String username = dialog.showAndWait().orElse(null);
        if (username == null || username.trim().isEmpty()) {
            System.exit(0); // Выход, если имя не указано
        }

        controller.setUsername(username);
        controller.connectToServer();

        Scene scene = new Scene(root, 1000, 800);
        stage.setTitle("Hello!");
        stage.setScene(scene);

        stage.setOnCloseRequest(e -> controller.closeConnection());
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

//TODO: Exception in thread "JavaFX Application Thread" java.lang.NullPointerException: Cannot invoke "msg.messengerui.HelloController.closeConnection()" because "controller" is null