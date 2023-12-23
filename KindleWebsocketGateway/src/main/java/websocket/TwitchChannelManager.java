package websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import org.java_websocket.WebSocket;

import java.util.*;
import java.util.function.Consumer;

public class TwitchChannelManager {

    //todo: wrap this up into its own class. START HERE ----

    private record ChatMessage(String userName, String chatMessage) {}

    private record ChatSubscriptionDetails(TwitchChat twitchChat, String twitchChannel, Consumer<String> jsonMessageConsumer) {}

    Map<TwitchChat, Map<String, List<ChatSubscriptionDetails>>>
            socketSubscriptionsViaChatChannelViaTwitchChat = new HashMap<>();

    private void addChatSubscription(ChatSubscriptionDetails subscriptionDetails) {

        TwitchChat twitchChat = subscriptionDetails.twitchChat();

        Map<String, List<ChatSubscriptionDetails>> socketSubscriptionsViaChatChannel =
                socketSubscriptionsViaChatChannelViaTwitchChat.computeIfAbsent(twitchChat, chat -> {
                    subscribeToChatEvents(chat);
                    return new HashMap<>();
                });

        // lock-out any interference from other threads
        synchronized (socketSubscriptionsViaChatChannel) {

            // hopefully they're subscribing to AlgoBro's channel. He's cool!
            String twitchChannel = subscriptionDetails.twitchChannel().toLowerCase();

            // is there an existing list of subscribers / a subscription to this channel?
            List<ChatSubscriptionDetails> channelSubscribersForThisClient =
                    socketSubscriptionsViaChatChannel.computeIfAbsent(twitchChannel, channelName -> {

                        System.out.println("Joining Twitch Channel: " + channelName);

                // subscribe TwitchChat to twitch channel for our new subscriber.
                twitchChat.joinChannel(channelName);

                // create a new list of subscribers for this channel
                return new ArrayList<>();

                // add the new subscriber to the list of subscribers for this channel on the way out.
            });

            // add our new subscriber to the list of subscribers for this channel
            channelSubscribersForThisClient.add(subscriptionDetails);

        }

    }

    void removeChatSubscription(ChatSubscriptionDetails subscriptionDetails) {

        TwitchChat twitchChat = subscriptionDetails.twitchChat();

        Map<String, List<ChatSubscriptionDetails>> socketSubscriptionsViaChatChannel = socketSubscriptionsViaChatChannelViaTwitchChat.computeIfAbsent(twitchChat, chat -> new HashMap<>());

        // lock-out any interference from other threads
        synchronized (socketSubscriptionsViaChatChannel) {

            String twitchChannel = subscriptionDetails.twitchChannel().toLowerCase();

            // remove the subscription from the list of channel consumers
            socketSubscriptionsViaChatChannel.get(twitchChannel).remove(subscriptionDetails);

            if(socketSubscriptionsViaChatChannel.get(twitchChannel).isEmpty()) {

                System.out.println("Leaving Twitch Channel: " + twitchChannel);

                // unsubscribe TwitchChat from the channel
                twitchChat.leaveChannel(twitchChannel);

                // remove the empty list
                socketSubscriptionsViaChatChannel.remove(twitchChannel);
            }

        }

    }

    ///--- TODO: END HERE

    Map<WebSocket, ChatSubscriptionDetails> chatSubscriptionsViaWebSocket = new HashMap<>();

    /**
     * @param twitchChat the TwitchChat object to subscribe to
     * @param twitchChannel the name of the Twitch channel to subscribe to
     * @param webSocket the WebSocket to send chat messages to
     */
    public void addChatSubscription(TwitchChat twitchChat, String twitchChannel, WebSocket webSocket) {

        ChatSubscriptionDetails subscriptionDetails =
                new ChatSubscriptionDetails(twitchChat, twitchChannel, jsonMessage -> {

                    try {

                        // sanity check: ensure the socket is still open before sending the message.
                        if(webSocket.isOpen())
                            webSocket.send(jsonMessage);
                        else {
                            System.out.println("WebSocket is closed. It's dead, Jim. Removing the subscription.");
                            unsubscribeSocket(webSocket);
                        }

                    } catch (Exception ex) { // Anything can happen. Don't be lackin.
                        System.out.println("Error sending message to WebSocket. Removing subscription.");
                        unsubscribeSocket(webSocket);
                    }

                });

        chatSubscriptionsViaWebSocket.put(webSocket, subscriptionDetails);
        addChatSubscription(subscriptionDetails);

    }

    /**
     * When a WebSocket disconnects, we only know its socket-object.
     * Use that as a key to remove it from all channels.
     *
     * @param conn the connection to remove from all channels
     */
    public void unsubscribeSocket(WebSocket conn) {

        ChatSubscriptionDetails toDisconnect = chatSubscriptionsViaWebSocket.remove(conn);

        removeChatSubscription(toDisconnect);

    }


    private void subscribeToChatEvents(TwitchChat chat) {

        System.out.println("Creating a Twitch Chat Message Event Listener.");

        // note: there can only be ONE IRCMessageEvent handler per TwitchChat Client. Much like the Highlander.
        chat.getEventManager().onEvent(IRCMessageEvent.class, event -> {

            Map<String, List<ChatSubscriptionDetails>>
                    chatConsumersByChannel = socketSubscriptionsViaChatChannelViaTwitchChat.get(chat);

            // message channel
            String twitchChannelName = event.getChannel().getName().toLowerCase();

            // message details
            Optional<String> message = event.getMessage();
            Optional<String> userDisplayName = event.getUserDisplayName();

            // validate that this is actually a chat message.
            if(message.isPresent() && userDisplayName.isPresent()) {

                ChatMessage chatMessage = new ChatMessage(userDisplayName.get(), message.get());

                System.out.println("Chat Message Received | " + twitchChannelName + " | " + chatMessage.userName() + " > " + chatMessage.chatMessage());

                List<ChatSubscriptionDetails> chatSubscriptions = chatConsumersByChannel.get(twitchChannelName);

                try {

                    // serialize the outbound message for our consumers.
                    String jsonMessage = createChatJsonMessage(chatMessage);

                    // dispatch the message to all chat consumers on this client
                    chatSubscriptions.forEach(subscriptionDetails -> {
                        Consumer<String> messageConsumer = subscriptionDetails.jsonMessageConsumer();
                        messageConsumer.accept(jsonMessage);
                    });

                } catch (JsonProcessingException e) {
                    System.out.println("Error serializing chat message into JSON. Skipping Message.");
                    e.printStackTrace();
                }

            }

        });

    }

    private String createChatJsonMessage(ChatMessage chatMessage) throws JsonProcessingException {

        Map<String, Object> jsonMap = new HashMap<>();

        jsonMap.put("type", "chat_message");
        jsonMap.put("userName", chatMessage.userName());
        jsonMap.put("chatMessage", chatMessage.chatMessage());

        // Serialize the Java object to JSON
        String jsonString = new ObjectMapper().writeValueAsString(jsonMap);

        return jsonString;

    }

}