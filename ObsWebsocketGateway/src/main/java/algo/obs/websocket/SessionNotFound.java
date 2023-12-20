package algo.obs.websocket;

public class SessionNotFound extends Exception {
    public final String hashId;
    public SessionNotFound(String hashId) {
        this.hashId = hashId;
    }
}
