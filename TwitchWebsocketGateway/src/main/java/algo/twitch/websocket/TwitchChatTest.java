package algo.twitch.websocket;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.TwitchChatBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;

import java.util.Optional;

public class TwitchChatTest {

    public static void main(String[] args) {
        new TwitchChatTest("");
    }

    static final String APP_CLIENT_ID = System.getenv("APP_ID");
    static final String CLIENT_SECRET = System.getenv("APP_SECRET");
    public TwitchChatTest(String code) {

        try {

            System.out.println("APP_ID: " + APP_CLIENT_ID);

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
                    .withEnableChat(true)
                    .withEnablePubSub(true)
                    .build();


            // twitch4j - chat
            TwitchChat client = twitchClient.getChat();

            client.joinChannel("AlgoBro");

            client.sendMessage("AlgoBro", "KappaHD");

            System.out.println("Connecting");

            EventManager eventManager = twitchClient.getEventManager();

            eventManager.onEvent(IRCMessageEvent.class, event -> {
                Optional<String> message = event.getMessage();

                if(message.isPresent()) {
                    System.out.println(event.getUser().getName() + " > " + message.get());
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }
}
