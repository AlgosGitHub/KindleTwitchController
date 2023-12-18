package algo.twitch.websocket;

import java.net.InetSocketAddress;

public class Start_Here {
    public static void main(String[] args) {
        System.out.println("Starting...");
        new TwitchOAuthEndpoint(80);
        new WebsocketGateway(new InetSocketAddress("0.0.0.0", 8080)).start();
        System.out.println("Goliath Online.");
    }
}
