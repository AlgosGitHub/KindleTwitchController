package algo.obs.websocket;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

/**
 * Facilitates communication between the OBS Embeds and this App Server.
 * Relays App State Updates and Commands to/from their respective Obs Controllers.
 */
public class ObsWebsocketGateway extends SecureWebSocketServer {

    public final ObsControllerRepository sessions = new ObsControllerRepository();

    public ObsWebsocketGateway(int port) {
        super(port);
    }

    private void log(WebSocket webSocket, String message) {
        String hostAddress = webSocket.getRemoteSocketAddress().getAddress().getHostAddress();
        System.out.println("OBS Websocket Gateway | " + hostAddress + " | " + message);
    }
    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        log(webSocket, "OBS Embed Connected");
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        log(webSocket, "OBS Embed Disconnected Gracefully. Closing Session[s] from our side.");
        sessions.closeSessionsWithHostAddress(webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        log(webSocket, "Unexpected Socket Error. Closing Session[s] from our side.");
        sessions.closeSessionsWithHostAddress(webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onStart() {
        System.out.println("OBS Websocket Gateway is Online. By Your Command.");
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
            log(webSocket, "Received Message from OBS Embed. | " + messageType);

            switch(messageType) {

                // the usual suspects
                case  "bootstrap"        ->  handleBoostrapMessage(webSocket, jsonMessage);
                case  "streaming_state"  ->  handleStreamingStateChangedEvent(jsonMessage);
                case  "scene_changed"    ->  handleSceneChangedEvent(jsonMessage);

                // If we don't recognize the message type, we'll log it and move on.
                default -> throw new IllegalArgumentException(messageType);

            }

        } catch (IllegalArgumentException ex) {
            System.out.println("Unrecognized Message Type. Dumping Message...\n" + message);
        } catch (Exception ex) {
            System.out.println("Error processing message. Dumping Message & Stack Trace...\n" + message);
            ex.printStackTrace(System.out);
        }

    }

    private void handleSceneChangedEvent(JsonObject jsonMessage) {

        try {

            // Extract the "hashCode" string
            String userHashId = jsonMessage.get("hashCode").getAsString();

            // Extract the "scene-name" string
            String sceneName = jsonMessage.get("currentScene").getAsString();

            // Update our retained state
            sessions.getSessionByHashId(userHashId).setScene(sceneName);

        } catch (SessionNotFound e) {
            System.out.println("Failed to handle Scene Change Event. Matching Session Not Found for given hashId:" + e.hashId);
        } catch (Exception e) {
            System.out.println("Failed to handle Scene Change Event. Unexpected Error. Dumping Stack Trace...");
            e.printStackTrace(System.out);
        }

    }

    private void handleStreamingStateChangedEvent(JsonObject jsonMessage) {

        try {

            // Extract the "hashCode" string
            String userHashId = jsonMessage.get("hashCode").getAsString();

            // Extract the "streaming" boolean value
            boolean isStreaming = jsonMessage.get("streaming").getAsBoolean();

            // Update our retained state
            sessions.getSessionByHashId(userHashId).setStreaming(isStreaming);

        } catch (SessionNotFound e) {
            throw new RuntimeException(e);
        }

    }

    private void handleBoostrapMessage(WebSocket webSocket, JsonObject jsonMessage) {

        log(webSocket, "Processing Bootstrap Message.");

        // Extract the "hashCode" string
        String hashId = jsonMessage.get("hashCode").getAsString();

        // Extract the "scenes" field as a JSON string
        String scenesJsonString = jsonMessage.get("scenes").getAsString();

        // Parse the nested JSON string
        JsonObject scenesObject = JsonParser.parseString(scenesJsonString).getAsJsonObject();

        // Extract the "streaming" boolean value
        boolean isStreaming = jsonMessage.get("streaming").getAsBoolean();

        // Extract the "currentScene" string
        String currentScene = scenesObject.get("currentScene").getAsJsonObject().get("name").getAsString();

        // Extract the names of the scenes
        JsonArray scenesArray = scenesObject.getAsJsonArray("scenes");
        String[] scenes = scenesArray.asList().stream().map(scene -> scene.getAsString()).toArray(String[]::new);

        ObsController obsController = new ObsController(hashId, webSocket, currentScene, scenes, isStreaming);

        // define our retained state
        sessions.addObsController(hashId, obsController);

    }

}

/*
        // A quick test to ensure we can change scenes and start/stop streaming
        Thread.startVirtualThread(() -> {
            try {

                Thread.sleep(5000);
                obsController.changeScene("TEST SCENE");
                Thread.sleep(5000);
                obsController.startStreaming();
                Thread.sleep(5000);
                obsController.stopStreaming();

            } catch (Exception e) {
                e.printStackTrace();
            }

        });
*/