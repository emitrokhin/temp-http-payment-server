// send-data.js

function sendUserData() {
    const user = Telegram.WebApp.initDataUnsafe.user;

    if (user) {
        const userData = {
            id: user.id,
            first_name: user.first_name,
            last_name: user.last_name || null,
            username: user.username || null,
            language_code: user.language_code || null,
            is_premium: user.is_premium || false,
            allows_write_to_pm: user.allows_write_to_pm || false,
            photo_url: user.photo_url || null
        };

        // Отправляем POST-запрос на сервер
        fetch('/save-user-data', { // Замените URL на ваш серверный обработчик
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData)
        })
            .then(response => response.json())
            .then(data => {
                console.log('User data sent successfully:', data);
            })
            .catch((error) => {
                console.error('Error sending user data:', error);
            });
    } else {
        console.error('User data is not available');
    }
}

// Ждем полной загрузки страницы
document.addEventListener("DOMContentLoaded", function () {
    sendUserData();
});