# temp-http-payment-server
Простой сервер для хостинга html страниц


Установка java maven на timeweb

curl -LO https://download.java.net/java/GA/jdk21/fd2272bbf8e04c3dbaee13770090416c/35/GPL/openjdk-21_linux-x64_bin.tar.gz
tar xzf openjdk-21_linux-x64_bin.tar.gz
export PATH=$(readlink -f ./jdk-21/bin):$PATH
curl -LO https://dlcdn.apache.org/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.tar.gz
tar xzf apache-maven-3.9.5-bin.tar.gz
export PATH=$(readlink -f ./apache-maven-3.9.5/bin):$PATH

запуск
mvn clean package
java -jar target/paymentserver-1.0-SNAPSHOT.jar &

запуск caddy

sudo nano /etc/caddy/Caddyfile

app.mitrohinayulya.ru {
    reverse_proxy localhost:8080
}

caddy run --config /etc/caddy/Caddyfile &
