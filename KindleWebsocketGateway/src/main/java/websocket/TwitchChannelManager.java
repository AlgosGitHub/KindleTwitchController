package websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.twitch4j.TwitchClient;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TwitchChannelManager {
    private final Map<String, List<WebSocket>> channelConnections = new HashMap<>();
    private final Map<String, Boolean> channelSubscriptions = new HashMap<>();

    public void addConnection(String twitchChannel, WebSocket conn) {
        System.out.println("Adding connection for channel: " + twitchChannel);
        List<WebSocket> connections = this.channelConnections.computeIfAbsent(twitchChannel, k -> new ArrayList<>());
        connections.add(conn);
    }

    public void removeConnectionFromAllChannels(WebSocket conn) {
        for (Map.Entry<String, List<WebSocket>> entry : this.channelConnections.entrySet()) {
            List<WebSocket> connections = entry.getValue();
            System.out.println("Removing connection from channel: " + entry.getKey());
            connections.remove(conn);
            if (connections.isEmpty()) {
                String twitchChannel = entry.getKey();
                this.channelSubscriptions.remove(twitchChannel);
                System.out.println("Unsubscribing from channel: " + twitchChannel);
            }
        }
    }

    public void initiateChatSubscription(TwitchClient twitchClient, String twitchChannel) {
        if (!this.channelSubscriptions.containsKey(twitchChannel)) {
            System.out.println("Subscribing to channel: " + twitchChannel);
            this.channelSubscriptions.put(twitchChannel, true);
            new TwitchChatClient(twitchClient, twitchChannel, chatMessage -> {
                try {
                    Map<String, Object> jsonMap = new HashMap<>();
                    jsonMap.put("type", "chat_message");
                    jsonMap.put("userName", chatMessage.userName());
                    jsonMap.put("chatMessage", chatMessage.chatMessage());

                    // Serialize the Java object to JSON
                    String jsonString = new ObjectMapper().writeValueAsString(jsonMap);

                    // Get the list of connections for this channel
                    List<WebSocket> connections = this.channelConnections.get(twitchChannel);
                    if (connections != null) {
                        for (WebSocket conn : connections) {
                            // sanity check: is the connection still open?
                            if (conn.isOpen())
                                // send the message to the frontend
                                conn.send(jsonString);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        }
    }
}