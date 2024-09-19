<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Успешная оплата</title>
    <script src="https://telegram.org/js/telegram-web-app.js"></script>
    <script src="telegram-js.js"></script>
</head>
<body>
<script>
    window.Telegram.WebApp.MainButton.text = "Вернуться в бот"
    window.Telegram.WebApp.onEvent('mainButtonClicked', function () {
        window.Telegram.WebApp.openTelegramLink('https://t.me/fenomen_mitrohina_bot')
    })
    window.Telegram.WebApp.MainButton.show()
</script>
<div class="container text-center mt-5">
    <h1>Оплата успешна!</h1>
    <p>В бот отправлена ссылка для доступа в сообщество</p>
    <p>Если вы заблокировали бота, или при запуске оплаты запретили ему вам писать, то нажмите кнопку ниже. После перехода в бот нажмите кнопку "Сообщество"</p>
</div>
</body>
</html>