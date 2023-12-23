package algo.twitch;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.helix.domain.User;

import java.util.HashMap;
import java.util.Map;

/**
 * This is for keeping track of each Twitch User's TwitchClient instances.
 */
public class TwitchClientRegistry {

    private final static Map<String, TwitchClientRegistryEntry> registry = new HashMap<>();

    record TwitchClientRegistryEntry(TwitchClient twitchClient, User user) {}

    /**
     * Called upon authentication of a Twitch Account during Sign-up.
     * Persist this hash until their Authentication expires or is withdrawn.
     * We'll know that happens when we get a 401 Unauthorized response from Twitch.
     *
     * @param hash
     * @param twitchClient
     * @param user
     */
    public static void addClient(String hash, TwitchClient twitchClient, User user) {
        System.out.println("Adding client hash: " + user.getDisplayName() + " -> " + hash);
        registry.put(hash, new TwitchClientRegistryEntry(twitchClient, user));
    }

    public static TwitchClient getClient(String hash) {
        return registry.get(hash).twitchClient;
    }

    public static User getUser(String hash) {
        return registry.get(hash).user;
    }

}
