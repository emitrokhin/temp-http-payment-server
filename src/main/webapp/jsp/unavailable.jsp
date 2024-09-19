<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Оплата недоступна</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC" crossorigin="anonymous">
    <jsp:include page="common-scripts.jsp" /></head>
<body>
<script>
    if (environment !== 'dev') {
        Telegram.WebApp.MainButton.text = "Вернуться в бот"
        Telegram.WebApp.onEvent('mainButtonClicked', function () {
            Telegram.WebApp.close()
        })
        Telegram.WebApp.MainButton.show()
    } else {
        var devTgButton = document.createElement('button');
        devTgButton.innerText = "Повторить оплату";
        devTgButton.classList.add("fixed-bottom", "btn", "btn-danger", "w-100");
        devTgButton.style.position = "fixed";
        devTgButton.style.bottom = "0";
        devTgButton.style.left = "0";
        devTgButton.style.zIndex = "1000";

        devTgButton.addEventListener('click', function () {
            window.close()
        });

        document.body.appendChild(devTgButton);
    }
</script>
<div class="container text-center mt-5">
    <h1>Оплата недоступна</h1>
    <p>Оплата подписки доступна только с 1 по 3 число каждого месяца.</p>
</div>
</body>
</html>
