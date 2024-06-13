import org.apache.xmlrpc.WebServer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChatRoomServer {
    private List<String> chatHistory;
    private Map<String, List<String>> userMessages;
    private Map<String, String> users;

    public ChatRoomServer() {
        this.chatHistory = new ArrayList<>();
        this.users = new HashMap<>();
        this.userMessages = new HashMap<>();
    }

    public String subscribe(String pseudo) {
        users.put(pseudo, pseudo);
        userMessages.put(pseudo, new ArrayList<>());
        for (String message : chatHistory) {
            sendMessageToUser(pseudo, message);
        }
        broadcastMessage(pseudo, pseudo + " a rejoint la salle de discussion.");
        return "Subscribed successfully";
    }

    public String unsubscribe(String pseudo) {
        if (users.remove(pseudo) != null) {
            userMessages.remove(pseudo);
            broadcastMessage(pseudo, pseudo + " a quitté la salle de discussion.");
        }
        return "Unsubscribed successfully";
    }

    public String postMessage(String pseudo, String message) {
        String formattedMessage = "[" + pseudo + "]: " + message;
        chatHistory.add(formattedMessage);
        broadcastMessage(pseudo, formattedMessage);
        return "Message posted successfully";
    }

    public List<String> getChatHistory() {
        return chatHistory;
    }

    private void broadcastMessage(String sender, String message) {
        for (String pseudo : users.values()) {
            sendMessageToUser(pseudo, sender.equals(pseudo) ? "[vous]: " + message.substring(sender.length() + 3) : message);
        }
    }

    private void sendMessageToUser(String pseudo, String message) {
        List<String> messages = userMessages.get(pseudo);
        if (messages != null) {
            messages.add(message);
        }
    }

    public String[] getMessages(String pseudo) {
        List<String> messages = userMessages.get(pseudo);
        if (messages == null) {
            return new String[0];
        }
        String[] result = new String[messages.size()];
        result = messages.toArray(result);
        messages.clear();
        return result;
    }

    public static void main(String[] args) {
        try {
            WebServer server = new WebServer(8080);
            server.addHandler("ChatRoomServer", new ChatRoomServer());
            server.start();
            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
