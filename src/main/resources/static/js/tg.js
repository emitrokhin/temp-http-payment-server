Telegram.WebApp.ready();
//TODO Server redirect
if (typeof Telegram === 'undefined' ||
    typeof Telegram.WebApp === 'undefined' ||
    typeof Telegram.WebApp.initDataUnsafe === 'undefined' ||
    typeof Telegram.WebApp.initDataUnsafe.user === 'undefined' ||
    typeof Telegram.WebApp.initDataUnsafe.user.id === 'undefined') {

    window.location.href = 'https://t.me/fenomen_mitrohina_bot/society';
}

console.log("Telegram WebApp script loaded");
