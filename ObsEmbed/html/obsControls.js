/*
    Connect to the OBS Websocket Gateway.
        > Send the initial set of scenes, current scene, and streaming state.
    Handle Commands to...
        > start streaming.
        > stop streaming.
        > change the scene.
    Relays updates to...
        > the current scene (obsSceneChanged),
        > the streaming state (obsStreamingStarted, obsStreamingStopped)

        State is retained in the cloud, so we don't need to worry about it here.
        Just observe, report, and obey.

 */

// Function to append a string as a new line to the body, for debugging, because we cannot see the console in OBS-Browser.
function log(message) {
    if (debugMode) {

        // Create a new <div> element for the log message
        const logDiv = document.createElement('div');

        // Set the text content of the <div> to the message
        logDiv.textContent = message;

        // Apply CSS styles for white text and bold font. Looks great on OBS' dark background.
        logDiv.style.color = 'white';
        logDiv.style.fontWeight = 'bold';

        // Append the <div> to the body
        document.body.appendChild(logDiv);

    } else {

        // If we're not in debug mode, just log to the console. Somebody may be listening.
        console.log(message);

    }
}

function setScene(sceneName) {

    log("Setting scene to " + sceneName);

    window.obsstudio.setCurrentScene(sceneName);

}

function startStream() {

    log("Starting stream");

    window.obsstudio.startStreaming();

}

function stopStreaming() {

    log("Stopping stream");

    window.obsstudio.stopStreaming();

}

function dispatchSceneChangedEvent(sceneName) {

    log("Dispatching Scene Change Event");

    const message = {
        type: 'scene_changed',
        hashCode: hashCode,
        currentScene: sceneName,
    };

    socket.send(JSON.stringify(message));

}

function dispatchStreamingStateChangeEvent(isStreaming) {

    log("Dispatching Streaming Change Event");

    const message = {
        type: 'streaming_state',
        streaming: isStreaming,
        hashCode: hashCode,
    };

    socket.send(JSON.stringify(message));

}

function listenForSceneChanges() {

    log("Listening for OBS Event: obsSceneChanged");

    window.addEventListener('obsSceneChanged', function(event) {
        log("Scene has Changed to " + event.detail.name);
        dispatchSceneChangedEvent(event.detail.name);
    });

}

function listenForStreamingStateChanges() {

    log("Listening for OBS Event: obsStreamingStarted");

    window.addEventListener('obsStreamingStarted', function(event) {
        log("Streaming has Started.");
        dispatchStreamingStateChangeEvent(true);
    });

    log("Listening for OBS Event: obsStreamingStopped");

    window.addEventListener('obsStreamingStopped', function(event) {
        log("Streaming has Stopped.");
        dispatchStreamingStateChangeEvent(false);
    });

}

function getScenes() {

    return new Promise(async (resolve, reject) => {

        try {

            const scenesPromise = new Promise(
                (innerResolve, innerReject) => {
                    window.obsstudio.getScenes(function (scenes) {
                        innerResolve(scenes);
                    });
                }
            );

            const currentScenePromise = new Promise(
                (innerResolve, innerReject) => {
                    window.obsstudio.getCurrentScene(function (scene) {
                        innerResolve(scene);
                    });
                }
            );

            const [scenes, currentScene] = await Promise.all([
                scenesPromise,
                currentScenePromise,
            ]);

            const result = {
                scenes,
                currentScene,
            };

            resolve(JSON.stringify(result));

        } catch (error) {
            reject(error);
        }

    });

}

function getStreamingState() {

    return new Promise(async (resolve, reject) => {

        try {

            window.obsstudio.getStatus(function (status) {

                const streamingStatus = status.streaming;

                const result = {
                    streaming: streamingStatus,
                };

                resolve(JSON.stringify(result));

            });

        } catch (error) {
            reject(error);
        }

    });

}

function sendBootstrapMessage() {

    log("Sending Bootstrap Message");

    // Use Promise.all to send both messages concurrently
    Promise.all([getScenes(), getStreamingState()]).then(([scenes, streaming]) => {

        const message = {
            type: 'bootstrap',
            hashCode: hashCode,
            scenes: scenes,
            streaming: streaming,
        };

        socket.send(JSON.stringify(message));

    });

}

function connectToServer() {

    log("Connecting to Websocket Gateway");

    const socket = new WebSocket('ws://localhost:8081/obs_remote');

    socket.addEventListener('open', function (event) {

        log("Connected to Websocket Gateway.");
        // todo: indicate that we've connected with a brief GIF or something

        // send the initial set of scenes, current scene, and streaming state
        sendBootstrapMessage();

    });

    socket.addEventListener('message', function (event) {
        const messageData = JSON.parse(event.data);

        log("Received message from gateway. Type: " + messageData.type);

        switch (messageData.type) {
            case "start_stream":
                startStream();
                break;
            case "stop_stream":
                stopStreaming();
                break;
            case "set_scene":
                setScene(messageData.sceneName);
                break;
            default:
                console.log("Unknown message type: " + messageData.type);
        }

    });

    return socket;

}

// todo: make this configurable
const debugMode = false;

log("obsControls.js loaded & running");
if(debugMode) {
    log("Debug Mode is enabled. By your command.");
}

// Listen for events
listenForSceneChanges();
listenForStreamingStateChanges();

//todo: source this from the embed's parameters
let hashCode = "88888";

// Connect to the Websocket Gateway
let socket = connectToServer();

/*
    Note from AlgoBro:

        I'd have liked to catch obsSceneListChanged events to update the scene list in the cloud,
        but I couldn't manage to capture that event. I'm not sure why. I tried a few different ways.

        If you can make that work, I'd appreciate it.

        Until then, we'll only be sending the list of scenes in the bootstrap message to minimize bandwidth consumption.
 */