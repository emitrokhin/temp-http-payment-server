// prod-script.js

// Загружаем библиотеку Telegram WebApp для production
var script = document.createElement("script");
script.src = "https://telegram.org/js/telegram-web-app.js";
script.onload = function() {
    console.log("Telegram WebApp script loaded");

    window.Telegram.WebApp.ready();

    if (typeof window.Telegram === 'undefined' ||
        typeof window.Telegram.WebApp === 'undefined' ||
        typeof window.Telegram.WebApp.initDataUnsafe === 'undefined' ||
        typeof window.Telegram.WebApp.initDataUnsafe.user === 'undefined' ||
        typeof window.Telegram.WebApp.initDataUnsafe.user.id === 'undefined') {

        window.location.href = 'https://t.me/fenomen_mitrohina_bot/society';
    }
};
document.head.appendChild(script);