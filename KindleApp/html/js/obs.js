
function populateSceneOptions(scenes) {

    addChatMessage("OBS", "Populating Scene List");

    const selectElement = document.getElementById("sceneSwitch");

    scenes.forEach(sceneName => {
        const option = document.createElement("option");
        option.textContent = sceneName;
        console.log("Adding scene: " + sceneName);
        selectElement.appendChild(option);
    });

}

function setCurrentScene(currentScene) {

    addChatMessage("OBS", "Scene set to " + currentScene);

    const selectElement = document.getElementById("sceneSwitch");
    for (let i = 0; i < selectElement.options.length; i++) {
        if (selectElement.options[i].text === currentScene) {
            selectElement.selectedIndex = i;
            break;
        }
    }
}

function changeScene(selectedScene) {

    addChatMessage("Kindle", "Changing Scene to: " + selectedScene);

    sendChangeSceneCommand(selectedScene);

}

function setStreamingState(isStreaming) {

    // set our global state
    streaming = isStreaming;

    // get the switch element, to update the label
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

}

function startSwitch() {

    const streamSwitch = document.getElementById("streamStartSwitch");
    const label = document.getElementById("stream-state-change-label");

    // disable the switch until we receive a call-back from OBS
    streamSwitch.disabled = true;

    if(streaming) {
        // are you sure you want to STOP the stream?
        label.textContent = "STOP";
        showDialog(() => {
            addChatMessage("Kindle", "Sending Stop Command to OBS...");
            sendStopStreamingCommand();
        });
    } else {
        // are you sure you want to START the stream?
        label.textContent = "START";
        showDialog(() => {
            addChatMessage("Kindle", "Sending Start Command to OBS...");
            sendStartStreamingCommand();
        });
    }

}

function showObsControlDiv() {
    const myDiv = document.getElementById("control-panel");
    myDiv.style.display = "block";
    hideObsVisibilityButton();
}

function hideObsControlDiv() {
    const myDiv = document.getElementById("control-panel");
    myDiv.style.display = "none";
    showObsVisibilityButton();
}

function hideObsVisibilityButton() {
    const showObsButton = document.getElementById("show-obs-button");
    showObsButton.style.display = "none";
}

function showObsVisibilityButton() {
    const showObsButton = document.getElementById("show-obs-button");
    showObsButton.style.display = "block";
}

function handleObsNotFound() {
    addChatMessage("Kindle", "OBS Embed not found. Please check your OBS Embed and then refresh this page.");
    hideObsVisibilityButton();
}

function showDialog(successCallback) {
    const overlay = document.getElementById('overlay');
    const dialog = document.getElementById('confirmationDialog');

    overlay.style.display = 'block';
    dialog.style.display = 'block';

    // Add event listener to handle the "Yes" button click
    const yesButton = document.getElementById('yesButton');
    yesButton.addEventListener('click', () => {
        closeDialog();
        if (typeof successCallback === 'function') {
            successCallback();
        }
    });

    // Add event listener to close the dialog and overlay when clicking outside the dialog
    overlay.addEventListener('click', closeDialog);

    // Function to close the dialog and overlay
    function closeDialog() {
        overlay.style.display = 'none';
        dialog.style.display = 'none';
        yesButton.removeEventListener('click', closeDialog);
    }
}
