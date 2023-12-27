function addChatMessage(user, message) {

    const newMessage = document.createElement('div');
    newMessage.classList.add('chat-message');

    /* note: keep this, we'll want it later when we re-introduce timestamps

        // Create spans for timestamp, user, and message
        var timestampSpan = document.createElement('span');
        timestampSpan.classList.add('timestamp');
        timestampSpan.textContent = new Date().toLocaleTimeString();

        var separator1 = document.createElement('span');
        separator1.textContent = " | ";

    */

    const userSpan = document.createElement('span');
    userSpan.classList.add('user');
    userSpan.textContent = user;

    const separator2 = document.createElement('span');
    separator2.textContent = ": ";

    const messageSpan = document.createElement('span');
    messageSpan.classList.add('message');
    messageSpan.textContent = message;

    // Append the spans to the newMessage container
    //newMessage.appendChild(timestampSpan);
    //newMessage.appendChild(separator1);
    newMessage.appendChild(userSpan);
    newMessage.appendChild(separator2);
    newMessage.appendChild(messageSpan);

    // Ensure the onclick function is correctly attached
    newMessage.addEventListener('click', function () {
        selectMessage(newMessage);
    });

    const chat = document.getElementById('chat');
    chat.appendChild(newMessage);

    const chatContainer = document.getElementById('chat-container');

    // Automatically scroll to the bottom unless the user has scrolled up
    if (!isUserScrolledUp) {
        chatContainer.scrollTop = chatContainer.scrollHeight;
    }

    // Keeping only the last 50 messages
    const messages = chat.getElementsByClassName('chat-message');
    while (messages.length > 50) {

        // remove the first message, until only the latest 50 remain.
        chat.removeChild(messages[0]);

    }

}

function setUpScrollListener() {

    // Detect if the user has scrolled up from the bottom, so we can stop auto-scrolling with new messages
    document.getElementById('chat-container').addEventListener('scroll', function () {
        const chat = document.getElementById('chat-container');
        // A buffer is used to determine if the user has scrolled up significantly
        const scrollBuffer = 10;
        isUserScrolledUp = chat.scrollTop + chat.clientHeight + scrollBuffer < chat.scrollHeight;
    });

}

function selectMessage(messageElement) {
    // Remove the 'selected' class from all messages
    const allMessages = document.getElementsByClassName('chat-message');
    for (let i = 0; i < allMessages.length; i++) {
        allMessages[i].classList.remove('selected');
    }

    // Add the 'selected' class to the clicked message
    messageElement.classList.add('selected');

    const user = messageElement.querySelector('.user').textContent;

    // Update the selected user name
    const messageUser = document.getElementById('message-user');
    messageUser.textContent = user;

    // ensure the mute and ban buttons are set correctly
    setMuteButton();
    setBanButton();

    // Show the selected user panel
    showSelectedUserDiv();

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


function banUser() {


    const messageText = document.getElementById('message-user').textContent;

    addChatMessage("Kindle", "Banned " + messageText);

    sendBanCommand(messageText);

    // update the button to say "Unban"
    setUnbanButton();

}

function unbanUser() {

    const messageText = document.getElementById('message-user').textContent;

    addChatMessage("Kindle", "Unbanned " + messageText);

    sendUnbanCommand(messageText);

    // reset the button position to BAN
    setBanButton();

}

function muteUser() {

    const messageText = document.getElementById('message-user').textContent;

    addChatMessage("Kindle", "Muted " + messageText);

    sendMuteCommand(messageText);

    // update the button to say "Unmute"
    setUnmuteButton();

}

function unmuteUser() {

    const messageText = document.getElementById('message-user').textContent;

    addChatMessage("Kindle", "Unmuted " + messageText);

    sendUnmuteCommand(messageText);

    // reset the button position to MUTE
    setMuteButton();

}

function showSelectedUserDiv() {
    const myDiv = document.getElementById("selected-message-panel");
    myDiv.style.display = "block";
}

function hideSelectedUserDiv() {
    const myDiv = document.getElementById("selected-message-panel");
    myDiv.style.display = "none";
}
