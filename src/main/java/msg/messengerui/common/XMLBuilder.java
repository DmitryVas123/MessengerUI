package msg.messengerui.common;

public class XMLBuilder {
    public XMLBuilder() {
    }

    public static String buildAuth(String username, String password) {
        return String.format("<auth username=\"%s\" password=\"%s\" />", username, password);
    }

    public static String buildMessage(String from, String to, String text) {
        return String.format("<message from=\"%s\" to=\"%s\" type=\"text\">%s</message>", from, to, text);
    }

    public static String buildStatus(String user, String type) {
        return String.format("<status user=\"%s\" type=\"%s\" />", user, type);
    }

    public static String buildDisconnect(String user) {
        return String.format("<disconnect user=\"%s\" />", user);
    }

    public static String buildUsersRequest() {
        return "<users />";
    }
}
