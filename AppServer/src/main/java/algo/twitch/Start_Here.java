package algo.twitch;

import algo.obs.websocket.ObsWebsocketGateway;
import algo.twitch.auth.TwitchOAuthEndpoint;
import websocket.KindleWebsocketGateway;

import java.net.InetSocketAddress;

public class Start_Here {
    public static void main(String[] args) {
        try {

            System.out.println("Starting Twitch OAuth2 Callback Endpoint...");
            new TwitchOAuthEndpoint(80);
            System.out.println("Starting OBS Websocket Gateway...");
            new ObsWebsocketGateway(8081).start();
            System.out.println("Starting Kindle Websocket Gateway...");
            new KindleWebsocketGateway(new InetSocketAddress("0.0.0.0", 8080)).start();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
