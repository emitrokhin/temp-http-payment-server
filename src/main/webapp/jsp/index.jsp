<!-- src/main/webapp/jsp/index.jsp -->

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Форма оплаты</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC" crossorigin="anonymous">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery.inputmask/5.0.9/inputmask.js"></script>
    <jsp:include page="common-scripts.jsp" />
</head>
<script>
    document.addEventListener("DOMContentLoaded", function () {
        if (Telegram && Telegram.WebApp) {
            Telegram.WebApp.ready();

            const today = new Date();
            const day = today.getDate();

            if (Telegram.WebApp.initDataUnsafe && Telegram.WebApp.initDataUnsafe.user && Telegram.WebApp.initDataUnsafe.user.id === 438139618) {

            } else if (day < 1 || day > 3) {
                location.href = '/unavailable';
            }

            if (environment !== 'dev') {
                Telegram.WebApp.MainButton.text = "Оформить подписку";
                Telegram.WebApp.onEvent('mainButtonClicked', function () {
                    document.getElementById('paymentForm').submit();
                });
                Telegram.WebApp.MainButton.show();
            } else {
                var devTgButton = document.createElement('button');
                devTgButton.innerText = "Оформить подписку";
                devTgButton.classList.add("fixed-bottom", "btn", "btn-danger", "w-100");
                devTgButton.style.position = "fixed";
                devTgButton.style.bottom = "0";
                devTgButton.style.left = "0";
                devTgButton.style.zIndex = "1000";

                devTgButton.addEventListener('click', function () {
                    document.getElementById('paymentForm').submit();
                });

                document.body.appendChild(devTgButton);
            }
        }

        // Маска для телефона и email
        Inputmask({"mask": "+7 (999) 999-9999"}).mask(document.getElementById("phone"));
        Inputmask({ alias: "email" }).mask(document.getElementById("email"));
    });
</script>
<body class="bg-light">
<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card">
                <div class="card-body">
                    <h6 class="gray">Подписка</h6>
                    <h2>₽2990 в месяц</h2>
                    <form action="/pay" method="POST" id="paymentForm">
                        <div class="mb-3">
                            <label for="firstName" class="form-label">Имя:</label>
                            <input type="text" class="form-control" id="firstName" name="firstName" minlength="3" required>
                            <div class="invalid-feedback">Имя должно быть не менее 3 символов</div>
                        </div>
                        <div class="mb-3">
                            <label for="lastName" class="form-label">Фамилия:</label>
                            <input type="text" class="form-control" id="lastName" name="lastName" minlength="3" required>
                            <div class="invalid-feedback">Фамилия должна быть не менее 3 символов</div>
                        </div>
                        <div class="mb-3">
                            <label for="phone" class="form-label">Телефон:</label>
                            <input type="text" class="form-control" id="phone" name="phone" required>
                            <div class="invalid-feedback">Пожалуйста, введите правильный телефон</div>
                        </div>
                        <div class="mb-3">
                            <label for="email" class="form-label">Email:</label>
                            <input type="email" class="form-control" id="email" name="email" required>
                            <div class="invalid-feedback">Пожалуйста, введите правильный email</div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
    <div class="fixed-bottom text-center bg-light py-2">
        <small>
            Оплачивая данный заказ, вы соглашаетесь с
            <a href="https://mitrohinayulya.ru/privacy/society" target="_blank">политикой обработки персональных данных</a>
            и
            <a href="https://mitrohinayulya.ru/agreement/society" target="_blank">пользовательским соглашением</a>.
        </small>
    </div>
</div>
<script>
    document.getElementById('paymentForm').addEventListener('submit', function(event) {
        let form = this;
        if (form.checkValidity() === false) {
            event.preventDefault();
            event.stopPropagation();
        }
        form.classList.add('was-validated');
    }, false);
</script>
</body>
</html>