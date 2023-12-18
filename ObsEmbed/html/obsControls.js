function getScenes() {
    return new Promise(async (resolve, reject) => {
        try {
            const scenesPromise = new Promise((innerResolve, innerReject) => {
                window.obsstudio.getScenes(function (scenes) {
                    innerResolve(scenes);
                });
            });

            const currentScenePromise = new Promise((innerResolve, innerReject) => {
                window.obsstudio.getCurrentScene(function (scene) {
                    innerResolve(scene);
                });
            });

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

function setScene(scene) {
    window.obsstudio.setCurrentScene(scene);
}

function startStream() {
    window.obsstudio.startStreaming();
}

function stopStreaming() {
    window.obsstudio.stopStreaming();
}

function isStreaming() {
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

function connectToServer(hashCode) {

    const socket = new WebSocket('ws://192.168.0.115:8080/obs_remote');

    socket.addEventListener('open', function (event) {

        // todo: indicate that we've connected with a brief GIF or something

        // authenticate with the server
        const subscribeMessage = {
            type: 'login',
            hashCode: hashCode
        };

        // fire in the hole!
        socket.send(JSON.stringify(subscribeMessage));

    });

    socket.addEventListener('message', function (event) {
        const messageData = JSON.parse(event.data);

        switch (messageData.type) {
            case "start_stream":
                startStream();
                break;
            case "stop_stream":
                stopStreaming();
                break;
            case "get_scenes":
                getScenes().then((scenes) => {
                    const message = {
                        type: 'scenes',
                        scenes: scenes,
                    };
                    socket.send(JSON.stringify(message));
                });
                break;
            case "set_scene":
                setScene(messageData.scene);
                break;
            case "is_streaming":
                isStreaming().then((streaming) => {
                    const message = {
                        type: 'streaming',
                        streaming: streaming,
                    };
                    socket.send(JSON.stringify(message));
                });
                break;
            default:
                console.log("Unknown message type: " + messageData.type);
        }

    });

    return socket;

}

let hashCode = "88888";
connectToServer(hashCode);