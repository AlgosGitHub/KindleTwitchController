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

}

// Adding an initial message and setting up interval for testing



function connectToServer(hashCode) {

    const socket = new WebSocket('ws://192.168.0.115:8080/kindle_chat');

    socket.addEventListener('open', function (event) {

        // alert the kindle app user that we've connected to the chat server successfully.
        addChatMessage("Kindle", "Connected to App Server!");

        // Send a subscribe message upon connecting
        const subscribeMessage = {
            type: 'get_chat',
            hashCode: hashCode
        };

        // fire in the hole!
        socket.send(JSON.stringify(subscribeMessage));

    });

    socket.addEventListener('message', function (event) {
        const messageData = JSON.parse(event.data);
        if (messageData.type == "chat_message") {
            addChatMessage(messageData.userName, messageData.chatMessage);
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


// fail-safe, the variable isn't always set before the first message is added.
isUserScrolledUp = false;

let hashCode = "88888";

let socket = connectToServer(hashCode);
