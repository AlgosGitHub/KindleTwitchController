package algo.twitch.auth;

import algo.twitch.TwitchClientRegistry;
import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import org.apache.http.client.utils.URIBuilder;

import java.util.ArrayList;
import java.util.Objects;

public class TwitchAuthService {

    static final String REDIRECT_URL = "http://localhost/auth_callback";
    static String APP_CLIENT_ID;
    static String APP_CLIENT_SECRET;

    public TwitchAuthService() {
        try {
            loadEnv();
            userOAuthURL(new ArrayList<OAuthScope>() {{
                add(OAuthScope.CHANNEL_MODERATE);
                add(OAuthScope.CHAT_EDIT);
                add(OAuthScope.CHAT_READ);
                add(OAuthScope.MODERATOR_MANAGE_BANNED_USER);
            }});
        } catch (Exception e) {
            System.out.println("Error loading environment variables: " + e.getMessage());
        }
    }

    private void loadEnv() throws Exception {
        APP_CLIENT_ID = System.getenv("APP_ID");
        APP_CLIENT_SECRET = System.getenv("APP_SECRET");
        if (Objects.equals(APP_CLIENT_ID, "")) {
            throw new Exception("APP_ID not found in environment variables");
        }
        if (Objects.equals(APP_CLIENT_SECRET, "")) {
            throw new Exception("APP_SECRET not found in environment variables");
        }
    }

    private void userOAuthURL(ArrayList<OAuthScope> scopes) {
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("https").setHost("id.twitch.tv").setPath("/oauth2/authorize")
                .addParameter("response_type", "code")
                .addParameter("client_id", APP_CLIENT_ID)
                .addParameter("scope", scopes.stream().map(OAuthScope::getScope).reduce((a, b) -> a + " " + b).orElse(""))
                .addParameter("redirect_uri", REDIRECT_URL);

        String url = uriBuilder.toString();
        System.out.println("OAuthURL: " + url);
    }

    //TODO: add proper implementation
    public String generateSecretKey() {
        return SecretKeyGenerator.nextKey();
    }

    public TwitchClient authenticate(String hashedPassphrase, String code) {

        System.out.println("Authenticating Twitch User...");

        TwitchIdentityProvider twitchIdentityProvider = new TwitchIdentityProvider(APP_CLIENT_ID, APP_CLIENT_SECRET, REDIRECT_URL);

        try {

            // the Credential manager keeps our auth tokens fresh.
            OAuth2Credential credentials = twitchIdentityProvider.getCredentialByCode(code);
            CredentialManager credentialManager = CredentialManagerBuilder.builder().build();
            credentialManager.registerIdentityProvider(twitchIdentityProvider);

            // this is where the magic happens.
            TwitchClient twitchClient = TwitchClientBuilder.builder()
                    .withClientId(APP_CLIENT_ID)
                    .withClientSecret(APP_CLIENT_SECRET)
                    .withChatAccount(credentials)
                    .withEnableHelix(true)
                    .withCredentialManager(credentialManager)
                    .withEnableChat(true)
                    .withEnablePubSub(true)
                    .build();

            // get the authenticated user's details, we'll need the user-name for IRC chat.
            UserList resultList = twitchClient.getHelix().getUsers(credentials.getAccessToken(), null, null).execute();

            // get the first result, there should only be one.
            User user = resultList.getUsers().getFirst();

            System.out.println("Twitch User Authenticated: " + user.getDisplayName());

            //todo: alert AlgoBro of all new sign-ups.

            TwitchClientRegistry.addClient(hashedPassphrase, twitchClient, user);

            return twitchClient;
        } catch (RuntimeException e) {
            System.out.println("Error during authentication: " + e.getMessage());
        }

        // todo: more elegant error handling.
        throw new RuntimeException("Failed to authenticate user.");
    }
}
