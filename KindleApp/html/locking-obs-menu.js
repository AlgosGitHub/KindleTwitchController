let isObsUnlocked = false;
let obsLockCountdownTimer = null;

const padlockObsButton = document.getElementById('padlock-obs-button');
const streamStartSwitch = document.getElementById('streamStartSwitch');
const sceneSwitch = document.getElementById('sceneSwitch');
const hideObsButton = document.getElementById('hide-obs-button');

function unlockObsButtons() {
    isObsUnlocked = true;
    padlockObsButton.innerHTML = '10';
    streamStartSwitch.disabled = false;
    sceneSwitch.disabled = false;
    hideObsButton.disabled = true;

    obsLockCountdownTimer = setInterval(() => {
        let count = parseInt(padlockObsButton.innerHTML);
        if (count > 0) {
            count--;
            padlockObsButton.innerHTML = count;
        } else {
            lockObsButtons();
        }
    }, 1000);
}

function lockObsButtons() {
    isObsUnlocked = false;
    clearInterval(obsLockCountdownTimer);
    padlockObsButton.innerHTML = '<img src="img/padlock.png" alt="Icon 1" style="max-width: 100%; max-height: 100%;">';
    streamStartSwitch.disabled = true;
    sceneSwitch.disabled = true;
    hideObsButton.disabled = false;
}

padlockObsButton.addEventListener('click', () => {
    if (isObsUnlocked) {
        lockObsButtons();
    } else {
        unlockObsButtons();
    }
});