package algo.twitch.auth;

import spark.Spark;

public class TwitchOAuthEndpoint {

    public TwitchOAuthEndpoint(int port) {
        setupAuthCallbackEndpoint(port);
    }

    private void setupAuthCallbackEndpoint(int port) {
        TwitchAuthService authService = new TwitchAuthService();
        // Create a Spark instance
        Spark.port(port); // You can change the port as needed

        // Define a dynamic endpoint that executes the associated runnable
        Spark.get("/:endpointName", (request, response) -> {

            // extract the name of the endpoint hit by this request i.e. url.com/endpointName
            String endpointName = request.params(":endpointName");

            // sanity check: does this endpoint exist in our map of endpoints?
            if (endpointName.equals("auth_callback")) {

                // log the call & response
                String code = request.queryParams("code");

                System.out.println("Auth Completed. OAuth2 Token: " + code.substring(code.length()-4));

                String randomlyGeneratedPassPhrase = authService.getPassPhrase();

                String hashedPassphrase = authService.hashPassphrase(randomlyGeneratedPassPhrase);

                try {

                    authService.authenticate(hashedPassphrase, code);

                } catch (Exception e) {

                    e.printStackTrace();

                    response.status(500);

                    return "Internal Server Error";
                }

                // return the response, this will display in the browser
                return "Your passphrase is: " + randomlyGeneratedPassPhrase;

            } else {

                response.status(418);

                return "I am a tea pot.";

            }
        });


    }
}
