let isUnlocked = false;
let countdownTimer = null;

const padlockButton = document.getElementById('padlock-mod-button');
const timeoutButton = document.getElementById('muteButton');
const banButton = document.getElementById('banButton');
const hideModButton = document.getElementById('hide-mod-button');

function unlockModButtons() {
    isUnlocked = true;
    padlockButton.innerHTML = '10';
    timeoutButton.disabled = false;
    banButton.disabled = false;
    hideModButton.disabled = true;

    countdownTimer = setInterval(() => {
        let count = parseInt(padlockButton.innerHTML);
        if (count > 0) {
            count--;
            padlockButton.innerHTML = count;
        } else {
            lockModButtons();
        }
    }, 1000);
}

function lockModButtons() {
    isUnlocked = false;
    clearInterval(countdownTimer);
    padlockButton.innerHTML = '<img src="../img/padlock.png" alt="Icon 1" style="max-width: 100%; max-height: 100%;">';
    timeoutButton.disabled = true;
    banButton.disabled = true;
    hideModButton.disabled = false;
}

padlockButton.addEventListener('click', () => {
    if (isUnlocked) {
        lockModButtons();
    } else {
        unlockModButtons();
    }
});