let isUnlocked = false;
let countdownTimer = null;

const padlockButton = document.getElementById('padlock-button');
const timeoutButton = document.getElementById('muteButton');
const banButton = document.getElementById('banButton');
const countdownButton = document.getElementById('hide-button');

function unlockButtons() {
    isUnlocked = true;
    padlockButton.innerHTML = '10';
    timeoutButton.disabled = false;
    banButton.disabled = false;
    countdownButton.disabled = true;

    countdownTimer = setInterval(() => {
        let count = parseInt(padlockButton.innerHTML);
        if (count > 0) {
            count--;
            padlockButton.innerHTML = count;
        } else {
            lockButtons();
        }
    }, 1000);
}

function lockButtons() {
    isUnlocked = false;
    clearInterval(countdownTimer);
    padlockButton.innerHTML = '<img src="img/padlock.png" alt="Icon 1" style="max-width: 100%; max-height: 100%;">';
    timeoutButton.disabled = true;
    banButton.disabled = true;
    countdownButton.disabled = false;
}

padlockButton.addEventListener('click', () => {
    if (isUnlocked) {
        lockButtons();
    } else {
        unlockButtons();
    }
});