
let streaming= false;
let selectedUser = "";
let selectedUserMuted = false;
let selectedUserBanned = false;

// fail-safe, the variable isn't always set before the first message is added.
let isUserScrolledUp = false;

const savedPassword = getCookie("password");
console.log("Saved password: " + savedPassword);
let hashCode = savedPassword;

function addChatMessage(user, message) {

    var newMessage = document.createElement('div');
    newMessage.classList.add('chat-message');

    // Create spans for timestamp, user, and message
    var timestampSpan = document.createElement('span');
    timestampSpan.classList.add('timestamp');
    timestampSpan.textContent = new Date().toLocaleTimeString();

    var separator1 = document.createElement('span');
    separator1.textContent = " | ";

    var userSpan = document.createElement('span');
    userSpan.classList.add('user');
    userSpan.textContent = user;

    var separator2 = document.createElement('span');
    separator2.textContent = ": ";

    var messageSpan = document.createElement('span');
    messageSpan.classList.add('message');
    messageSpan.textContent = message;

    // Append the spans to the newMessage container
    newMessage.appendChild(timestampSpan);
    newMessage.appendChild(separator1);
    newMessage.appendChild(userSpan);
    newMessage.appendChild(separator2);
    newMessage.appendChild(messageSpan);

    // Ensure the onclick function is correctly attached
    newMessage.addEventListener('click', function () {
        selectMessage(newMessage);
    });

    var chat = document.getElementById('chat');
    chat.appendChild(newMessage);

    var chatContainer = document.getElementById('chat-container');
    // Automatically scroll to the bottom unless the user has scrolled up
    if (!isUserScrolledUp) {
        chatContainer.scrollTop = chatContainer.scrollHeight;
    }

    // Keeping only the last 50 messages
    var messages = chat.getElementsByClassName('chat-message');
    while (messages.length > 50) {
        chat.removeChild(messages[0]);
    }
}
// Detect if the user has scrolled up
document.getElementById('chat-container').addEventListener('scroll', function () {
    var chat = document.getElementById('chat-container');
    // A buffer is used to determine if the user has scrolled up significantly
    var scrollBuffer = 10;
    isUserScrolledUp = chat.scrollTop + chat.clientHeight + scrollBuffer < chat.scrollHeight;
});
function selectMessage(messageElement) {
    // Remove the 'selected' class from all messages
    var allMessages = document.getElementsByClassName('chat-message');
    for (var i = 0; i < allMessages.length; i++) {
        allMessages[i].classList.remove('selected');
    }

    // Add the 'selected' class to the clicked message
    messageElement.classList.add('selected');

    var user = messageElement.querySelector('.user').textContent;

    // Update the selected user name
    var messageUser = document.getElementById('message-user');
    messageUser.textContent = user;

    // ensure the mute and ban buttons are set correctly
    setMuteButton();
    setBanButton();

    // Show the selected user panel
    showSelectedUserDiv();

}

// Adding an initial message and setting up interval for testing


function sendChatSubscription(hashCode) {
    // Send a subscribe message upon connecting
    const message = {
        type: 'get_chat',
        hashCode: hashCode
    };

    // fire in the hole!
    socket.send(JSON.stringify(message));
}

function sendBootstrapRequest(hashCode) {
    // Send a subscribe message upon connecting
    const message = {
        type: 'get_bootstrap',
        hashCode: hashCode
    };

    // fire in the hole!
    socket.send(JSON.stringify(message));
}

function connectToServer(hashCode) {

    const socket = new WebSocket('ws://192.168.0.193:8080/kindle_chat');

    socket.addEventListener('open', function (event) {

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
            default:
                console.log("Unknown message type: " + messageData.type);
        }

    });

    return socket;

}

function sendModCheck() {

    var message = {
        type: 'mod_check',
        hashCode: hashCode
    };
    socket.send(JSON.stringify(message));

}

function banUser() {

    var messageText = document.getElementById('message-user').textContent;
    var message = {
        type: 'ban_user',
        hashCode: hashCode,
        userName: messageText
    };
    socket.send(JSON.stringify(message));

    // update the button to say "Unban"
    setUnbanButton();

}

function unbanUser() {

    var messageText = document.getElementById('message-user').textContent;
    var message = {
        type: 'unban_user',
        hashCode: hashCode,
        userName: messageText
    };
    socket.send(JSON.stringify(message));

    // reset the button position to BAN
    setBanButton();

}

function muteUser() {

    var messageText = document.getElementById('message-user').textContent;
    var message = {
        type: 'mute_user',
        hashCode: hashCode,
        userName: messageText
    };
    socket.send(JSON.stringify(message));

    // update the button to say "Unmute"
    setUnmuteButton();

}

function unmuteUser() {

    var messageText = document.getElementById('message-user').textContent;
    var message = {
        type: 'unmute_user',
        hashCode: hashCode,
        userName: messageText
    };
    socket.send(JSON.stringify(message));

    // reset the button position to MUTE
    setMuteButton();

}

function setBanButton() {

    // update the ban button label to "Unban"
    document.getElementById("banButton").textContent = "Ban";

    // swap the button function to unbanUser
    document.getElementById("banButton").removeEventListener("click", unbanUser);
    document.getElementById("banButton").addEventListener("click", banUser);

}

function setUnbanButton() {

    // update the ban button label to "Unban"
    document.getElementById("banButton").textContent = "Unban";

    // swap the button function to unbanUser
    document.getElementById("banButton").removeEventListener("click", banUser);
    document.getElementById("banButton").addEventListener("click", unbanUser);

}

function setMuteButton() {

    // update the mute button label to "Unmute"
    document.getElementById("muteButton").textContent = "Mute";

    // swap the button function to unmuteUser
    document.getElementById("muteButton").removeEventListener("click", unmuteUser);
    document.getElementById("muteButton").addEventListener("click", muteUser);

}

function setUnmuteButton() {

    // update the mute button label to "Unmute"
    document.getElementById("muteButton").textContent = "Unmute";

    // swap the button function to unmuteUser
    document.getElementById("muteButton").removeEventListener("click", muteUser);
    document.getElementById("muteButton").addEventListener("click", unmuteUser);

}



// Function to populate the select element with scene options
function populateSceneOptions(scenes) {

    const selectElement = document.getElementById("sceneSwitch");

    scenes.forEach(sceneName => {
        const option = document.createElement("option");
        option.textContent = sceneName;
        console.log("Adding scene: " + sceneName);
        selectElement.appendChild(option);
    });

}

// Function to set the current scene in the select element
function setCurrentScene(currentScene) {

    addChatMessage("OBS", "Scene set to " + currentScene)

    var selectElement = document.getElementById("sceneSwitch");
    for (var i = 0; i < selectElement.options.length; i++) {
        if (selectElement.options[i].text === currentScene) {
            selectElement.selectedIndex = i;
            break;
        }
    }
}

// Function to handle scene change
function changeScene(selectedScene) {

    addChatMessage("Kindle", "Changing Scene to: " + selectedScene)

    console.log("Selected scene: " + selectedScene);

    const message = {
        type: 'change_scene',
        hashCode: hashCode,
        scene: selectedScene
    };

    socket.send(JSON.stringify(message));

}

function setStreamingState(isStreaming) {

    // set our global state
    streaming = isStreaming;

    // update the switch label
    const startSwitch = document.getElementById("streamStartSwitch");
    if(isStreaming) {
        console.log("Stream is Started");
        addChatMessage("OBS", "Stream is Started");
        startSwitch.textContent = "Stop Stream";
    } else {
        console.log("Stream is Stopped");
        addChatMessage("OBS", "Stream is Stopped");
        startSwitch.textContent = "Start Stream";
    }

    // re-enable the switch, it's disabled by default. It's also disabled if we sent the start/stop command.
    startSwitch.disabled = false;

}

function sendStopStreamingCommand() {
    addChatMessage("Kindle", "Sending Stop Command to OBS...");
    const message = {
        type: 'stop_streaming',
        hashCode: hashCode
    };
    socket.send(JSON.stringify(message));
}

function sendStartStreamingCommand() {
    addChatMessage("Kindle", "Sending Start Command to OBS...");
    const message = {
        type: 'start_streaming',
        hashCode: hashCode
    };
    socket.send(JSON.stringify(message));
}

function startSwitch() {

    const streamSwitch = document.getElementById("streamStartSwitch");

    // disable the switch until we receive a call-back from OBS
    streamSwitch.disabled = true;

    if(streaming) {
        sendStopStreamingCommand();
    } else {
        sendStartStreamingCommand();
    }

}

function getCookie(cookieName) {
    var cookies = document.cookie.split(';');
    for (var i = 0; i < cookies.length; i++) {
        var cookie = cookies[i].trim();
        var cookieParts = cookie.split('=');
        if (cookieParts[0] === cookieName) {
            return cookieParts[1];
        }
    }
    return null;
}

function showSelectedUserDiv() {
    const myDiv = document.getElementById("selected-message-panel");
    myDiv.style.display = "block";
}

// Function to hide the DIV
function hideSelectedUserDiv() {
    const myDiv = document.getElementById("selected-message-panel");
    myDiv.style.display = "none";
}

// hide the selected user div by default. Assigning a default class style doesn't work.
hideSelectedUserDiv();

let socket = connectToServer(hashCode);
document.getElementById("hide-button").addEventListener("click", hideSelectedUserDiv);
