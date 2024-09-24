// dev-tg.js
Telegram = {
    WebApp: {
        initDataUnsafe: {
            user: {
                id: 123456789,
                first_name: "Test",
                last_name: "User",
                username: "testuser",
                language_code: "en",
                is_premium: false,
                allows_write_to_pm: true,
                photoUrl: null
            }
        },
        ready: function () {
            console.log("Fake Telegram WebApp is ready.");
        }
    }
};

// Вызов ready
Telegram.WebApp.ready();