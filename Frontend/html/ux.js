function addFakeMessage() {

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
    userSpan.textContent = "user";

    var separator2 = document.createElement('span');
    separator2.textContent = ": ";

    var messageSpan = document.createElement('span');
    messageSpan.classList.add('message');
    messageSpan.textContent = "message";

// Append the spans to the newMessage container
    newMessage.appendChild(timestampSpan);
    newMessage.appendChild(separator1);
    newMessage.appendChild(userSpan);
    newMessage.appendChild(separator2);
    newMessage.appendChild(messageSpan);

    // Ensure the onclick function is correctly attached
    newMessage.addEventListener('click', function() {
        selectMessage(newMessage);
    });

    var chat = document.getElementById('chat');
    chat.appendChild(newMessage);

    // Automatically scroll to the bottom unless the user has scrolled up
    if (!isUserScrolledUp) {
        chatContainer.scrollTop = chatContainer.scrollHeight;
    }

    // Keeping only the last 10 messages
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

}

let passphrase;

function savePassphrase() {
    passphrase = document.getElementById('passphrase-input').value;
}

// Adding an initial message and setting up interval for testing
setInterval(addFakeMessage, 1300);