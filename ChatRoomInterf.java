import java.util.List;
public interface ChatRoomInterf {
    void subscribe(String pseudo) throws Exception;
    void unsubscribe(String pseudo) throws Exception;
    void postMessage(String pseudo, String message) throws Exception;
    List<String> getChatHistory() throws Exception;
}
