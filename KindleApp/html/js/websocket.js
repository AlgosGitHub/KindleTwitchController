
function connectToServer(hashCode) {

    const socket = new WebSocket('ws://192.168.0.193:8080/kindle_chat');

    socket.addEventListener('open', function (event) {

        console.log("Connected to App Server");

        // alert the Kindle app user that we've connected to the chat server successfully.
        addChatMessage("Kindle", "Connected to App Server!");

        sendChatSubscription(hashCode);
        sendBootstrapRequest(hashCode);

    });

    socket.addEventListener('message', function (event) {

        const messageData = JSON.parse(event.data);

        switch (messageData.type) {

            case "chat_message":
                addChatMessage(messageData.userName, messageData.chatMessage);
                console.log("Chat message received");
                break;

            case "set_scenes":
                populateSceneOptions(messageData.scenes);
                console.log("Scenes received");
                break;

            case "set_scene":
                setCurrentScene(messageData.scene);
                console.log("Current scene received");
                break;

            case "set_stream_state":
                setStreamingState(messageData.streaming);
                break;

            case "obs_not_found":
                handleObsNotFound();
                break;

            default:
                console.log("Unknown message type: " + messageData.type);

        }

    });

    return socket;

}

function sendBootstrapRequest(hashCode) {

    console.log("Sending Bootstrap Request");
    
    // Send a subscribe message upon connecting
    const message = {
        type: 'get_bootstrap',
        hashCode: hashCode
    };

    // send command to backend!
    socket.send(JSON.stringify(message));
    
}

function sendStopStreamingCommand() {

    console.log("Sending Stop Streaming Command");
    
    const message = {
        type: 'stop_streaming',
        hashCode: hashCode
    };

    // send command to backend!
    socket.send(JSON.stringify(message));
    
}

function sendStartStreamingCommand() {

    console.log("Sending Start Streaming Command");
    
    const message = {
        type: 'start_streaming',
        hashCode: hashCode
    };

    // send command to backend!
    socket.send(JSON.stringify(message));
    
}

function sendChangeSceneCommand(selectedScene) {

    console.log("Sending Change Scene Command");

    const message = {
        type: 'change_scene',
        hashCode: hashCode,
        scene: selectedScene
    };

    // send command to backend!
    socket.send(JSON.stringify(message));

}

function sendModCheckCommand() {

    console.log("Sending Mod Check Command");

    const message = {
        type: 'mod_check',
        hashCode: hashCode
    };
    
    // send command to backend!
    socket.send(JSON.stringify(message));

}

function sendBanCommand(userName) {

    console.log("Sending Ban Command");

    const message = {
        type: 'ban_user',
        hashCode: hashCode,
        userName: userName
    };

    // send command to backend!
    socket.send(JSON.stringify(message));

}

function sendUnbanCommand(userName) {

    console.log("Sending Unban Command");

    const message = {
        type: 'unban_user',
        hashCode: hashCode,
        userName: userName
    };

    // send command to backend!
    socket.send(JSON.stringify(message));

}

function sendMuteCommand(userName) {

    console.log("Sending Mute Command");

    const message = {
        type: 'mute_user',
        hashCode: hashCode,
        userName: userName
    };

    // send command to backend!
    socket.send(JSON.stringify(message));

}

function sendUnmuteCommand(userName) {

    console.log("Sending Unmute Command");

    const message = {
        type: 'unmute_user',
        hashCode: hashCode,
        userName: userName
    };

    // send command to backend!
    socket.send(JSON.stringify(message));

}


function sendChatSubscription(hashCode) {

    console.log("Sending Chat Subscription");

    // Send a subscribe message upon connecting
    const message = {
        type: 'get_chat',
        hashCode: hashCode
    };

    // send command to backend!
    socket.send(JSON.stringify(message));

}
