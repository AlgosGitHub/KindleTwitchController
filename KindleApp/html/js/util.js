
function getCookie(cookieName) {

    const cookies = document.cookie.split(';');

    for (let i = 0; i < cookies.length; i++) {

        const cookie = cookies[i].trim();
        const cookieParts = cookie.split('=');

        if (cookieParts[0] === cookieName) {
            return cookieParts[1];
        }

    }

    return null;

}
