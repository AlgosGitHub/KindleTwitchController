package algo.obs.websocket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A repository of all active OBS Embed sessions.
 * Allows us to look up a session by its hashId.
 * Also allows us to close all sessions with a given host address.
 */
public class ObsControllerRepository {

    private static Map<String, ObsController> sessions = new HashMap<>();

    public ObsController getSessionByHashId(String hashId) throws SessionNotFound {

        // Sanity Check: Does the session exist?
        if(!sessionExists(hashId)) throw new SessionNotFound(hashId);

        return sessions.get(hashId);

    }

    public boolean sessionExists(String hashId) {
        return sessions.containsKey(hashId);
    }


    public void addObsController(String hashId, ObsController obsController) {

        // Sanity check: Ensure we don't already have a session for this user
        if(sessionExists(hashId)) {

            System.out.println("Warning: Session already exists for " + hashId + ". Closing previous session, replacing with new session, and handing-over any existing event listeners.");

            ObsController retiringController = sessions.remove(hashId);

            if(retiringController.stateChangeListener != null)
                obsController.stateChangeListener = retiringController.stateChangeListener;

            if(retiringController.sceneChangeListener != null)
                obsController.sceneChangeListener = retiringController.sceneChangeListener;

            retiringController.closeSocket();

        }

        // Proceed with adding the session
        sessions.put(hashId, obsController);

    }

    public void closeSessionsWithHostAddress(String hostAddress) {

        // Collect the sessions with the matching host address
        List<ObsController> toCloseAndRemove = sessions.values().stream().filter(session -> session.hostAddress.equals(hostAddress)).collect(Collectors.toList());

        // Close those sockets, and remove them from the sessions map
        toCloseAndRemove.forEach(controller -> {
            controller.closeSocket();
            sessions.remove(controller.hashId);
            System.out.println("Closed Session | " + hostAddress + " | " + controller.hashId);
        });

    }

}
