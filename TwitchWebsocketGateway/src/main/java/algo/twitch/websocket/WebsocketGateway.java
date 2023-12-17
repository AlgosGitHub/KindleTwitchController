package algo.twitch.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.twitch4j.TwitchClient;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebsocketGateway extends WebSocketServer {

    // secure web socket certificate. see: readme.txt
    private static final String
        KEY_STORE_PATH = "keystore.jks", // Adjust this path
        KEY_STORE_PASSWORD = "changeme";// System.getenv("KEY_STORE_PASSWORD");

    public static List<WebSocket> activeSessions = new ArrayList<>();


    public WebsocketGateway(InetSocketAddress address) {
        super(address);
        setupSecureContext();

    }

    private void setupSecureContext() {

        try {

            // sanity check: validate the keystore exists on the local machine
            if(!new File(KEY_STORE_PATH).exists()) {
                System.out.println("Key Store Does Not Exist at " + KEY_STORE_PATH);
                return;
            }

            // Load the JDK's key store
            final KeyStore ks = KeyStore.getInstance("JKS");
            try (InputStream in = new FileInputStream(KEY_STORE_PATH)) {
                ks.load(in, KEY_STORE_PASSWORD.toCharArray());
            }

            // Set up key manager factory to use our key store
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, KEY_STORE_PASSWORD.toCharArray());

            // Initialize the SSLContext to work with our key managers.
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);

            // Formally switch the WebSocket to secure mode.
            setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));

        } catch (Exception e) {
            System.out.println("Failed to setup secure context");
            e.printStackTrace();
        }

    }

    @Override
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request ) throws InvalidDataException {

        if(!request.getResourceDescriptor().equals("/kindle_chat"))
            throw new InvalidHandshakeException("You're not allowed to connect!");

        return super.onWebsocketHandshakeReceivedAsServer(conn, draft, request);

    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

        activeSessions.add(conn);
        System.out.println("CONNECTED: " + conn.getRemoteSocketAddress().getAddress().getHostAddress());

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

        activeSessions.remove(conn);
        System.out.println("DISCONNECTED: " + conn.getRemoteSocketAddress().getAddress().getHostAddress() + " | " + reason + " | " + code);

    }

    @Override
    public void onMessage(WebSocket conn, String message) {

        System.out.println("Received message from session " + conn.getRemoteSocketAddress().getAddress().getHostAddress());

        System.out.println("Message: \n"+message);

        if(message.contains("\"type\":\"get_chat\"")) {
            handleGetChatMessage(conn, message);
        }

    }

    private void handleGetChatMessage(WebSocket conn, String message) {
        try {
            String hashCode = getHashCodeFromMessage(message);
            initiateChatSubscription(conn, hashCode);
        } catch (Exception ex) {
            System.out.println("Failed to get hash code from message");
            ex.printStackTrace();
        }
    }

    private String getHashCodeFromMessage(String message) throws JsonProcessingException {

        // Create an ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        // Parse the JSON string into a JsonNode
        JsonNode jsonNode = objectMapper.readTree(message);

        // Extract a named parameter
        String hashCode = jsonNode.get("hashCode").asText();

        return hashCode;

    }

    private void initiateChatSubscription(WebSocket conn, String hashCode) {

        TwitchClient twitchClient = TwitchClientRegistry.getClient(hashCode);
        String twitchChannel = "AlgoBro"; //todo: source this via the hashCode

        new TwitchChatClient(twitchClient, twitchChannel, chatMessage -> {
            try {

                Map<String, Object> jsonMap = new HashMap<>();
                jsonMap.put("type", "chat_message");
                jsonMap.put("userName", chatMessage.userName());
                jsonMap.put("chatMessage", chatMessage.chatMessage());

                // Serialize the Java object to JSON
                String jsonString = new ObjectMapper().writeValueAsString(jsonMap);

                // sanity check: is the connection still open?
                if(conn.isOpen())
                    conn.send(jsonString);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }


    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("ERROR: " + ex.getMessage());
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        // Start function
        System.out.println("Websocket server has started");

    }


}