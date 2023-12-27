
let streaming= false;

// false is the default, so that we're auto-scrolling chat by default.
let isUserScrolledUp = false;

const hashCode = getCookie("password");
console.log("Saved Password: " + hashCode);

let socket = connectToServer(hashCode);

// set up the stop-scrolling listener. to stop-scrolling when the user scrolls up in the chat panel.
setUpScrollListener();
