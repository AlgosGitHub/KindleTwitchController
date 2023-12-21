package algo.obs.websocket;

import org.java_websocket.WebSocket;

import java.util.function.Consumer;

/**
 * Represents a single OBS Embed session.
 * Be gentle with it. This is somebody's live stream.
 */
public class ObsController {

    private boolean isStreaming;
    private String currentScene;

    public final String hashId;

    private final String[] scenes;
    public final String hostAddress;

    private final WebSocket webSocket;
    Consumer<Boolean> stateChangeListener;
    Consumer<String> sceneChangeListener;

    public ObsController(String hashId, WebSocket webSocket, String currentScene, String[] scenes, boolean isStreaming) {
        this.hashId = hashId;
        this.webSocket = webSocket;
        this.currentScene = currentScene;
        this.scenes = scenes;
        this.isStreaming = isStreaming;
        this.hostAddress = webSocket.getRemoteSocketAddress().getAddress().getHostAddress();
        log("Ready.");
    }

    /**
     * Subscribes to the "Streaming State Change" event.
     *
     * @param stateChangeListener a lambda that accepts a boolean value, which will be called when the streaming state changes
     */
    public void subscribeToStreamingStateChanges(Consumer<Boolean> stateChangeListener) {
        this.stateChangeListener = stateChangeListener;
    }

    /**
     * Subscribes to the "Scene Change" event.
     *
     * @param sceneChangeListener a lambda that accepts a string value, which will be called when the scene changes
     */
    public void subscribeToSceneChanges(Consumer<String> sceneChangeListener) {
        this.sceneChangeListener = sceneChangeListener;
    }

    /**
     * Commands the OBS Embed to change the scene.
     *
     * @param sceneName the name of the scene to change to. Must be one of the scenes provided in the "scenes" array in the constructor.
     */
    public void changeScene(String sceneName) {
        log("Changing Scene... | " + currentScene + " -> " + sceneName);
        webSocket.send("{\"type\":\"set_scene\",\"sceneName\":\"" + sceneName + "\"}");
    }

    /**
     * Commands the OBS Embed to start streaming.
     */
    public void startStreaming() {
        log("Starting stream...");
        webSocket.send("{\"type\":\"start_stream\"}");
    }

    /**
     * Commands the OBS Embed to stop streaming.
     */
    public void stopStreaming() {
        log("Stopping stream...");
        webSocket.send("{\"type\":\"stop_stream\"}");
    }

    /**
     * Called ONLY by the ObsWebsocketGateway when it receives a "Streaming State Message" from an OBS Embed.
     *
     * @param streaming true if streaming, false if not streaming
     */
    public void setStreaming(boolean streaming) {
        log("Streaming State Changed | " + isStreaming + " -> " + streaming);
        isStreaming = streaming;
        dispatchStreamingStateChange();
    }

    /**
     * Called ONLY by the ObsWebsocketGateway when it receives a "Scene Changed Event" from an OBS Embed.
     *
     * @param sceneName the name of the scene that the user has changed to
     */
    public void setScene(String sceneName) {
        log("Scene State Changed | " + currentScene + " -> " + sceneName);
        currentScene = sceneName;
        dispatchSceneChange();
    }

    /**
     * Closes the socket connection to the OBS Embed.
     * Called ONLY by ObsEmbedSessions for good housekeeping.
     */
    void closeSocket() {
        log("Closing Socket...");
        webSocket.close();
    }

    private void log(String message) {
        System.out.println("OBS Embed Controller | " + hostAddress + " | " + message);
    }

    /**
     * Dispatches the "Streaming State Change" event to any listeners.
     */
    private void dispatchStreamingStateChange() {
        if(stateChangeListener != null) {
            log("Dispatching Streaming State Change Event...");
            stateChangeListener.accept(isStreaming);
        }
    }

    /**
     * Dispatches the "Scene Change" event to any listeners.
     */
    private void dispatchSceneChange() {
        if(sceneChangeListener != null) {
            log("Dispatching Scene Change Event...");
            sceneChangeListener.accept(currentScene);
        }
    }

    public String getCurrentScene() {
        return currentScene;
    }

    public String[] getScenes() {
        return scenes;
    }

    public boolean getStreamingState() {
        return isStreaming;
    }

}
