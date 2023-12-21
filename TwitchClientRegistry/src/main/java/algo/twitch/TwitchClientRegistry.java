package algo.twitch;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.helix.domain.User;

import java.util.HashMap;
import java.util.Map;

public class TwitchClientRegistry {

    private static final Map<String, TwitchClient> clients = new HashMap<>();
    private static final Map<String, User> users = new HashMap<>();

    public static void addClient(String hash, TwitchClient twitchClient, User user) {
        System.out.println("Adding client hash: " + user.getDisplayName() + " -> " + hash);
        clients.put(hash, twitchClient);
        users.put(hash, user);
    }

    public static TwitchClient getClient(String hash) {
        return clients.get(hash);
    }

    public static User getUser(String hash) {
        return users.get(hash);
    }

}
