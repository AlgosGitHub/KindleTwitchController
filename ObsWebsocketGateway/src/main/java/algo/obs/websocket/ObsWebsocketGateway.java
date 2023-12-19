package algo.obs.websocket;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;

public class ObsWebsocketGateway extends WebSocketServer {

    private static final String
            KEY_STORE_PATH = "keystore.jks", // Adjust this path
            KEY_STORE_PASSWORD = "changeme";// System.getenv("KEY_STORE_PASSWORD");

    public ObsWebsocketGateway(InetSocketAddress address) {
        super(address);
        setupSecureContext();
    }

    /*
        RECEIVE Websocket connections from embedded browser elements in OBS.
        FACILITATE OBS Commands sent through the Kindle Websocket Gateway
        REPORT OBS State as reported by the OBS Embed.
     */

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println("New Connection from " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        System.out.println("Connection Closed from " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        System.out.println("Message Received from " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
        System.out.println("Message: " + s);
        try {

        } catch (Exception ex) {
            System.out.println("Error processing message: " + ex.getMessage());
            System.out.println("Message: " + s);
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        System.out.println("Error from " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
        e.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Server Started");
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
}
