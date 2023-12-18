function addChatMessage(user, message) {
    var chat = document.getElementById('chat');
    var chatContainer = document.getElementById('chat-container');
    var newMessage = document.createElement('div');
    newMessage.classList.add('chat-message');
    newMessage.textContent = new Date().toLocaleTimeString() + " | " + user + ": " + message;

    // Ensure the onclick function is correctly attached
    newMessage.addEventListener('click', function() {
        selectMessage(newMessage);
    });

    chat.appendChild(newMessage);

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
document.getElementById('chat-container').addEventListener('scroll', function() {
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

    // Update the selected message text
    var messageText = document.getElementById('message-text');
    messageText.textContent = messageElement.textContent;
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
        if(messageData.type == "chat_message") {
            addChatMessage(messageData.userName, messageData.chatMessage);
        }
    });

}

// fail-safe, the variable isn't always set before the first message is added.
isUserScrolledUp = false;

connectToServer("88888");
