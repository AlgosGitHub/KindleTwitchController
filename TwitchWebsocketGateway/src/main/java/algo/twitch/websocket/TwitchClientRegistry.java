package algo.twitch.websocket;

import com.github.twitch4j.TwitchClient;

import java.util.HashMap;
import java.util.Map;

public class TwitchClientRegistry {

    private static final Map<String, TwitchClient> clients = new HashMap<>();

    public static void addClient(String hash, TwitchClient twitchClient) {
        System.out.println("Adding client hash: " + hash);
        clients.put(hash, twitchClient);
    }

    public static TwitchClient getClient(String hash) {
        return clients.get(hash);
    }

}
