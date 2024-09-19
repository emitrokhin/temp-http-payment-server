<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Оплата подписки на месяц. Сообщество "Как дома" Юлии Митрохиной</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC" crossorigin="anonymous">    <script src="https://widget.cloudpayments.ru/bundles/cloudpayments.js"></script>
    <jsp:include page="common-scripts.jsp" />

    <style>
        .hide-scroll::-webkit-scrollbar {
            display: none;
        }

        .hide-scroll {
            -ms-overflow-style: none;  /* IE and Edge */
            scrollbar-width: none;  /* Firefox */
        }
    </style>
</head>
<body class="hide-scroll">
<script>
    window.Telegram.WebApp.MainButton.hide()
    // Проверка текущего числа
    const today = new Date();
    const day = today.getDate();

    if (window.Telegram.WebApp.initDataUnsafe.user.id === 438139618) {

    }
    else if (day < 1 || day > 3) {
        window.location.href = '/unavailable';
    }

    var price = 2990.00;
    var pkId = "pk_dc972c529da05acb3a61d5f049cbc";
    var payments = new cp.CloudPayments({
        language: "ru-RU",
        email: "",
        applePaySupport: false,
        googlePaySupport: false,
        yandexPaySupport: false,
        tinkoffPaySupport: false,
        tinkoffInstallmentSupport: false,
        sbpSupport: true,
    });

    var receipt = {
        "Items": [
            {
                "label": "Оплата подписки на 1 мес. Сообщество \"Как дома\" Юлии Митрохиной",
                "price": price,
                "quantity": 1.00,
                "amount": price,
                "method": 4,
                "object": 4,
                "measurementUnit": "шт"
            }
        ],
        email: ${requestScope.email},
        "calculationPlace": "mitrohinayulya.ru",
        "taxationSystem": 1,
        "amounts": {
            "electronic": price,
            "advancePayment": 0.00,
            "credit": 0.00,
            "provision": 0.00
        }
    };

    var data = {
        CloudPayments: {
            CustomerReceipt: receipt,
            recurrent: {
                interval: 'Month',
                period: 1,
                customerReceipt: receipt
            }
        }
    };

    window.addEventListener('load', function () {
        payments.pay("charge", {
                publicId: pkId,
                description: "Оплата подписки на 1 мес. Сообщество \"Как дома\" Юлии Митрохиной",
                amount: price,
                currency: "RUB",
                accountId: window.Telegram.WebApp.initDataUnsafe.user.id,
                invoiceId: "INV" + window.Telegram.WebApp.initDataUnsafe.user.id,
                skin: "mini",
                data: data
            },
            {
                onSuccess: function (options) { // success
                    window.location.href = "/success"
                },
                onFail: function (reason, options) { // fail
                    window.location.href = "/fail"
                }
            }).then(function (widgetResult) {
            console.log('result', widgetResult);
        }).catch(function (error) {
            console.log('error', error);
        });
    });
</script>
</body>
</html>