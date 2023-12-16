package algo.obs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.message.response.scenes.GetSceneListResponse;
import io.obswebsocket.community.client.message.response.stream.GetStreamStatusResponse;
import spark.Spark;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class OBS_API_Endpoints {

    private static final String OBS_PASSWORD = System.getenv("OBS_PASSWORD");

    private static final Map<String, Function<Map<String, String>, String>> endpoints = new HashMap<>();

    private OBSRemoteController controller;

    public OBS_API_Endpoints(int port) {
        setupObsController();
        setupEndpointService(port);
        defineEndpoints();
    }

    private void setupObsController() {

        try {

            controller = OBSRemoteController.builder()
                .host("localhost")                  // Default host
                .port(4455)                         // Default port
                .password(OBS_PASSWORD)           // Provide your password here
                .connectionTimeout(3)        // Seconds the client will wait for OBS to respond
                .build();

            controller.connect();

        } catch (Exception ex) {
            throw new RuntimeException("Failed to connect to OBS", ex);
        }

    }

    private void defineEndpoints() {

        System.out.println("Defining Endpoints...");

        System.out.println("\t > Start Stream");
        setupStartStreamEndpoint();

        System.out.println("\t > Stop Stream");
        setupStopStreamEndpoint();

        System.out.println("\t > Get Stream State");
        setupGetStreamStateEndpoint();

        System.out.println("\t > Get Scenes");
        setupGetScenesEndpoint();

        System.out.println("\t > Set Scene");
        setupSetSceneEndpoint();

        System.out.println("All Endpoints Defined.");

    }

    private void setupSetSceneEndpoint() {
        addEndpoint("set_scene", (params) -> {

            System.out.println("Setting Scene...");

            try {

                String sceneName = params.get("scene_name");

                System.out.println("Setting Scene to: " + sceneName);

                controller.setCurrentProgramScene(sceneName, 100);

            } catch (Exception e) {
                System.out.println("Scene Set Failed!");
                e.printStackTrace();
                return "0";
            }

            System.out.println("Scene Set");

            return "1";

        });
    }

    private String createScenesResponse(GetSceneListResponse response) throws JsonProcessingException {

        List<String> sceneNames = response.getScenes().stream().map(scene -> scene.getSceneName()).toList();

        // Create a Map to represent the JSON structure
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("scenes", sceneNames);
        jsonMap.put("active_scene", response.getCurrentProgramSceneName());

        // Create an XmlMapper

        // Serialize the Map to JSON
        String jsonString = new ObjectMapper().writeValueAsString(jsonMap);

        return jsonString;

    }

    private void setupGetScenesEndpoint() {
        addEndpoint("get_scenes", (params) -> {

            System.out.println("Getting Scenes...");

            try {

                GetSceneListResponse sceneList = controller.getSceneList(100);

                System.out.println("Scenes...");
                sceneList.getScenes().forEach(scene -> System.out.println("\t > " + scene.getSceneName()));

                System.out.println("Current Scene: " + sceneList.getCurrentProgramSceneName());

                String toReturn = createScenesResponse(sceneList);

                System.out.println("Returning JSON \t" + toReturn);

                return toReturn;

            } catch (Exception e) {
                System.out.println("Scenes Check Failed!");
                e.printStackTrace();
                return "0";
            }

        });
    }

    private void setupGetStreamStateEndpoint() {
        addEndpoint("get_stream_state", (params) -> {

            System.out.println("Getting Stream State...");

            try {

                GetStreamStatusResponse streamStatus = controller.getStreamStatus(100);

                System.out.println("Is Streaming?: " + streamStatus.getOutputActive());

                return streamStatus.getOutputActive() ? "1" : "0";

            } catch (Exception e) {
                System.out.println("Stream State Check Failed!");
                e.printStackTrace();
                return "0";
            }

        });
    }

    private void setupStopStreamEndpoint() {
        addEndpoint("stop_stream", (params) -> {

            System.out.println("Stopping Stream...");

            try {

                controller.stopStream(100);

            } catch (Exception e) {
                System.out.println("Stream Stop Failed!");
                e.printStackTrace();
                return "0";
            }

            System.out.println("Stream Stopped");

            return "1";

        });
    }

    private void setupStartStreamEndpoint() {
        addEndpoint("start_stream", (params) -> {

            System.out.println("Starting Stream...");

            try {

                controller.startStream(100);

            } catch (Exception e) {
                System.out.println("Stream Start Failed!");
                e.printStackTrace();
                return "0";
            }

            System.out.println("Stream Started");

            return "1";

        });
    }

    private void setupEndpointService(int port) {

        // Create a Spark instance
        Spark.port(port); // You can change the port as needed

        // Define a dynamic endpoint that executes the associated runnable
        Spark.get("/:endpointName", (request, response) -> {

            // extract the name of the endpoint hit by this request i.e. url.com/endpointName
            String endpointName = request.params(":endpointName");

            // sanity check: does this endpoint exist in our map of endpoints?
            if (endpoints.containsKey(endpointName)) {

                // fetch the appropriate endpointFunction from our map of endpoints, if we have one
                Function<Map<String, String>, String> endpointFunction = endpoints.get(endpointName);

                // log the call & params
                System.out.println("Endpoint Called with Params: ");
                request.params().forEach((k, v) -> System.out.println("\t > " + k + " \t=\t " + v));

                // execute the endpointFunction with the params from the request, retain the response
                String responseStr = endpointFunction.apply(request.params());

                // log the call & response
                System.out.println("Endpoint executed: " + endpointName + "\n Our Response... \n " + responseStr);

                // return the response, this will display in the browser
                return responseStr;

            } else {

                response.status(418);

                return "I am a tea pot.";

            }
        });


    }

    private static void addEndpoint(String endpointName, Function<Map<String, String>, String> endpointFunction) {
        System.out.println("Adding Endpoint Function: " + endpointName);
        endpoints.put(endpointName, endpointFunction);
    }

}
