module msg.messengerui {
    requires javafx.controls;
    requires javafx.fxml;


    opens msg.messengerui to javafx.fxml;
    exports msg.messengerui;
}