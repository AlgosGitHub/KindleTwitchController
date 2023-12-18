package algo.twitch.websocket;

import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;

import java.util.Optional;
import java.util.function.Consumer;

public class TwitchChatClient {

    public record ChatMessage(String userName, String chatMessage) {}

    public TwitchChatClient(TwitchClient twitchClient, String twitchChannel, Consumer<ChatMessage> onMessage) {

        try {

            // twitch4j - chat
            TwitchChat client = twitchClient.getChat();

            // source the user's name from their authorization token
            client.joinChannel(twitchChannel);

            System.out.println("Connected to Twitch Chat Channel: " + twitchChannel);

            EventManager eventManager = twitchClient.getEventManager();

            eventManager.onEvent(IRCMessageEvent.class, event -> {
                Optional<String> message = event.getMessage();
                Optional<String> userDisplayName = event.getUserDisplayName();

                if(message.isPresent() && userDisplayName.isPresent()) {
                    System.out.println(event.getUser().getName() + " > " + message.get());
                    onMessage.accept(new ChatMessage(userDisplayName.get(), message.get()));
                }

            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
/*
            Thread.startVirtualThread(() -> {
                int iteration = 0;
                while(true) {
                    try {
                        client.sendMessage("AlgoBro", "MrDestructoid algobrHI " + ++iteration );
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });*/