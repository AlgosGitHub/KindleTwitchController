package algo.twitch.websocket;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import spark.Spark;

public class TwitchOAuthEndpoint {

    public TwitchOAuthEndpoint(int port) {
        setupAuthCallbackEndpoint(port);
    }

    public static void main(String[] args) {
        new TwitchOAuthEndpoint(80);
    }


    private void setupAuthCallbackEndpoint(int port) {

        // Create a Spark instance
        Spark.port(port); // You can change the port as needed

        // Define a dynamic endpoint that executes the associated runnable
        Spark.get("/:endpointName", (request, response) -> {

            // extract the name of the endpoint hit by this request i.e. url.com/endpointName
            String endpointName = request.params(":endpointName");

            // sanity check: does this endpoint exist in our map of endpoints?
            if (endpointName.equals("auth_callback")) {

                // log the call & response
                String code = request.queryParams("code");

                System.out.println("Auth Completed. OAuth2 Token: " + code.substring(code.length()-4));

                String randomlyGeneratedPassPhrase = getPassPhrase();

                String hashedPassphrase = hashPassphrase(randomlyGeneratedPassPhrase);

                TwitchClient twitchClient = finishAuth(code);

                TwitchClientRegistry.addClient(hashedPassphrase, twitchClient);

                // return the response, this will display in the browser
                return "Your passphrase is: " + randomlyGeneratedPassPhrase;

            } else {

                response.status(418);

                return "I am a tea pot.";

            }
        });


    }

    private String hashPassphrase(String randomlyGeneratedPassPhrase) {
        return "88888";
    }

    private String getPassPhrase() {
        return "Potato Potato Potato";
    }

    static final String APP_CLIENT_ID = System.getenv("APP_ID");
    static final String CLIENT_SECRET = System.getenv("APP_SECRET");

    private TwitchClient finishAuth(String code) {

        // finish the authentication process that the user began.
        TwitchIdentityProvider twitchIdentityProvider = new TwitchIdentityProvider(APP_CLIENT_ID, CLIENT_SECRET, "http://localhost/auth_callback");

        OAuth2Credential credentials = twitchIdentityProvider.getCredentialByCode(code);

        // credential manager
        CredentialManager credentialManager = CredentialManagerBuilder.builder().build();
        credentialManager.registerIdentityProvider(twitchIdentityProvider);

        TwitchClient twitchClient = TwitchClientBuilder.builder()
                .withClientId(APP_CLIENT_ID)
                .withClientSecret(CLIENT_SECRET)
                .withChatAccount(credentials)
                .withCredentialManager(credentialManager)
                .withEnableChat(true)
                .withEnablePubSub(true)
                .build();

        return twitchClient;

    }

}
