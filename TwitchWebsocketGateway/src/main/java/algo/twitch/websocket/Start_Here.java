package algo.twitch.websocket;

import java.net.InetSocketAddress;

public class Start_Here {
    public static void main(String[] args) {
        try {

            System.out.println("Starting...");
            new TwitchOAuthEndpoint(80);
            System.out.println("Endpoint Online.");
            new WebsocketGateway(new InetSocketAddress("0.0.0.0", 8080)).start();
            System.out.println("Gateway Online.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
