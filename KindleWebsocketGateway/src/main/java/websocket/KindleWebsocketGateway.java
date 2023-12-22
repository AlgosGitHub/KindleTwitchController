package websocket;

import algo.obs.websocket.ObsController;
import algo.obs.websocket.ObsControllerRepository;
import algo.twitch.TwitchClientRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.twitch4j.TwitchClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

public class KindleWebsocketGateway extends WebSocketServer {

    // secure web socket certificate. see: readme.txt
    private static final String
        KEY_STORE_PATH = "keystore.jks", // Adjust this path
        KEY_STORE_PASSWORD = "changeme";// System.getenv("KEY_STORE_PASSWORD");

    TwitchChannelManager twitchChannelManager = new TwitchChannelManager();

    private final ObsControllerRepository obsControllerRepository;

    public KindleWebsocketGateway(InetSocketAddress address, ObsControllerRepository obsControllerRepository) {
        super(address);
        this.obsControllerRepository = obsControllerRepository;
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
            throw new InvalidHandshakeException("418 I'm a teapot");

        return super.onWebsocketHandshakeReceivedAsServer(conn, draft, request);

    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

        System.out.println("CONNECTED: " + conn.getRemoteSocketAddress().getAddress().getHostAddress());

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

        twitchChannelManager.removeConnectionFromAllChannels(conn);
        System.out.println("DISCONNECTED: " + conn.getRemoteSocketAddress().getAddress().getHostAddress() + " | " + reason + " | " + code);

    }

    @Override
    public void onMessage(WebSocket webSocket, String message) {

        try {

            // Trust fall: Parse the message as a JSON object
            JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();

            // We'll need this for common-sense logging.
            String hostAddress = webSocket.getRemoteSocketAddress().getAddress().getHostAddress();

            // Sanity check: Ensure the message contains a "type" field
            if(!jsonMessage.has("type")) throw new Exception("Message from ("+hostAddress+") does not contain a \"type\" field");

            // Extract the "type" field as a string, now that we know it's present.
            String messageType = jsonMessage.get("type").getAsString();

            // Log the message type
            System.out.println("Received Message from Kindle. | " + hostAddress + " | " + messageType);

            switch(messageType) {
                case "get_chat"         ->  handleChatSubscription(webSocket, message);
                case "get_bootstrap"    ->  handleBootstrapQuery(webSocket, message);
                case "mute_user"        ->  handleMuteUser(message);
                case "unmute_user"      ->  handleUnmuteUser(message);
                case "ban_user"         ->  handleBanUser(message);
                case "unban_user"       ->  handleUnbanUser(message);
                case "mod_check"        ->  handleModCheck(message);
                case "start_streaming"  ->  handleStartStreamingCommand(message);
                case "stop_streaming"   ->  handleStopStreamingCommand(message);
                case "change_scene"     ->  handleSceneChangeCommand(message);
                default                 ->  System.out.println("Unknown Message Type: " + messageType);
            }

        } catch (Exception ex) {
            System.out.println("Error processing message. Dumping Message & Stack Trace...\n" + message);
            ex.printStackTrace(System.out);
        }

    }

    private void handleSceneChangeCommand(String message) {

        try {

            String hashId = getHashCodeFromMessage(message);
            String sceneName = getSceneNameFromMessage(message);

            System.out.println("Change Scene - " + hashId + " | " + sceneName);

            ObsController obsController = obsControllerRepository.getSessionByHashId(hashId);

            obsController.changeScene(sceneName);

        } catch (Exception ex) {
            System.out.println("Failed to handle scene change command. Dumping Stack Trace...");
            ex.printStackTrace();
        }

    }

    private void handleStopStreamingCommand(String message) {

        try {

            String hashId = getHashCodeFromMessage(message);

            System.out.println("Stop Streaming - " + hashId);

            ObsController obsController = obsControllerRepository.getSessionByHashId(hashId);

            obsController.stopStreaming();

        } catch (Exception ex) {
            System.out.println("Failed to handle stream stop command. Dumping Stack Trace...");
            ex.printStackTrace();
        }


    }

    private void handleStartStreamingCommand(String message) {

        try {

            String hashId = getHashCodeFromMessage(message);

            System.out.println("Start Streaming - " + hashId);

            ObsController obsController = obsControllerRepository.getSessionByHashId(hashId);

            obsController.startStreaming();

        } catch (Exception ex) {
            System.out.println("Failed to handle stream start command. Dumping Stack Trace...");
            ex.printStackTrace();
        }
    }

    private void handleBootstrapQuery(WebSocket webSocket, String message) {

        try {

            String hashId = getHashCodeFromMessage(message);

            if(obsControllerRepository.sessionExists(hashId)) {

                System.out.println("Retrieving OBS Bootstrap for " + hashId);

                ObsController obsController = obsControllerRepository.getSessionByHashId(hashId);

                sendScenes(webSocket, obsController.getScenes());
                sendCurrentScene(webSocket, obsController.getCurrentScene());
                sendStreamingState(webSocket, obsController.getStreamingState());

                // set-up the subscriptions
                obsController.subscribeToSceneChanges(sceneName -> sendCurrentScene(webSocket, sceneName));
                obsController.subscribeToStreamingStateChanges(isStreaming -> sendStreamingState(webSocket, isStreaming));

            } else System.out.println("No OBS Controller Session Exists for " + hashId);

        } catch (Exception e) {
            System.out.println("Failed to handle Bootstrap Query");
        }
        //todo: implement me once we've got the OBS controllers
    }

    private void sendCurrentScene(WebSocket webSocket, String currentScene) {

        try {

            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("type", "set_scene");
            jsonMap.put("scene", currentScene);

            // Serialize the Java object to JSON
            String jsonString = new ObjectMapper().writeValueAsString(jsonMap);

            webSocket.send(jsonString);

        } catch (Exception e) {
            System.out.println("Failed to send current scene");
        }

    }

    private void sendScenes(WebSocket webSocket, String[] scenes) {

        try {

            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("type", "set_scenes");
            jsonMap.put("scenes", scenes);

            // Serialize the Java object to JSON
            String jsonString = new ObjectMapper().writeValueAsString(jsonMap);

            webSocket.send(jsonString);

        } catch (Exception e) {
            System.out.println("Failed to send scenes");
        }

    }

    private void sendStreamingState(WebSocket webSocket, boolean isStreaming) {

        try {

            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("type", "set_stream_state");
            jsonMap.put("streaming", isStreaming);

            // Serialize the Java object to JSON
            String jsonString = new ObjectMapper().writeValueAsString(jsonMap);

            webSocket.send(jsonString);

        } catch (Exception e) {
            System.out.println("Failed to send streaming state");
        }

    }

    private void handleModCheck(String message) {
        try {

            String hashCode = getHashCodeFromMessage(message);
            modCheck(hashCode);

        } catch (Exception ex) {
            System.out.println("Failed to get hash code from message");
            ex.printStackTrace();
        }
    }

    private void modCheck(String hashCode) {
        TwitchClient twitchClient = TwitchClientRegistry.getClient(hashCode);
        String twitchChannel = TwitchClientRegistry.getUser(hashCode).getDisplayName();
        twitchClient.getChat().sendMessage(twitchChannel, "ModCheck");
    }

    private void handleBanUser(String message) {
        try {

            String hashCode = getHashCodeFromMessage(message);
            String userName = getUserNameFromMessage(message);
            banUser(userName, hashCode);

        } catch (Exception ex) {
            System.out.println("Failed to get hash code from message");
            ex.printStackTrace();
        }
    }

    private void handleUnbanUser(String message) {
        try {

            String hashCode = getHashCodeFromMessage(message);
            String userName = getUserNameFromMessage(message);
            unbanUser(userName, hashCode);

        } catch (Exception ex) {
            System.out.println("Failed to get hash code from message");
            ex.printStackTrace();
        }
    }

    private void handleUnmuteUser(String message) {

        try {

            String hashCode = getHashCodeFromMessage(message);
            String userName = getUserNameFromMessage(message);
            unmuteUser(userName, hashCode);

        } catch (Exception ex) {
            System.out.println("Failed to get hash code from message");
            ex.printStackTrace();
        }

    }

    private void handleMuteUser(String message) {
        try {
            String hashCode = getHashCodeFromMessage(message);
            String userName = getUserNameFromMessage(message);
            muteUser(userName, hashCode);
        } catch (Exception ex) {
            System.out.println("Failed to get hash code from message");
            ex.printStackTrace();
        }
    }

    private void muteUser(String userName, String hashCode) {

        System.out.println("Muting user: " + userName);
        TwitchClient twitchClient = TwitchClientRegistry.getClient(hashCode);
        String twitchChannel = TwitchClientRegistry.getUser(hashCode).getDisplayName();
        twitchClient.getChat().sendMessage(twitchChannel, "/timeout " + userName + " 10000");

    }

    private void unmuteUser(String userName, String hashCode) {

        System.out.println("Unmuting user: " + userName);
        TwitchClient twitchClient = TwitchClientRegistry.getClient(hashCode);
        String twitchChannel = TwitchClientRegistry.getUser(hashCode).getDisplayName();
        twitchClient.getChat().sendMessage(twitchChannel, "/untimeout " + userName);
    }

    private void banUser(String userName, String hashCode) {

        System.out.println("Banning user: " + userName);
        TwitchClient twitchClient = TwitchClientRegistry.getClient(hashCode);
        String twitchChannel = TwitchClientRegistry.getUser(hashCode).getDisplayName();
        twitchClient.getChat().sendMessage(twitchChannel, "/ban " + userName);
    }

    private void unbanUser(String userName, String hashCode) {

        System.out.println("Unbanning user: " + userName);
        TwitchClient twitchClient = TwitchClientRegistry.getClient(hashCode);
        String twitchChannel = TwitchClientRegistry.getUser(hashCode).getDisplayName();
        twitchClient.getChat().sendMessage(twitchChannel, "/unban " + userName);
    }

    private void handleChatSubscription(WebSocket conn, String message) {
        try {
            String hashCode = getHashCodeFromMessage(message);
            String twitchChannel = TwitchClientRegistry.getUser(hashCode).getDisplayName();
            twitchChannelManager.addConnection(twitchChannel, conn);
            TwitchClient twitchClient = TwitchClientRegistry.getClient(hashCode);
            twitchChannelManager.initiateChatSubscription(twitchClient, twitchChannel);
        } catch (Exception ex) {
            System.out.println("Failed to get hash code from message");
            ex.printStackTrace();
        }
    }

    private String getSceneNameFromMessage(String message) throws JsonProcessingException {

        // Create an ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        // Parse the JSON string into a JsonNode
        JsonNode jsonNode = objectMapper.readTree(message);

        // Extract a named parameter
        String sceneName = jsonNode.get("scene").asText();

        return sceneName;

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

    private String getUserNameFromMessage(String message) throws JsonProcessingException {

        // Create an ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        // Parse the JSON string into a JsonNode
        JsonNode jsonNode = objectMapper.readTree(message);

        // Extract a named parameter
        String hashCode = jsonNode.get("userName").asText();

        return hashCode;

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
