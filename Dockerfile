# Используем легковесный образ Maven для сборки проекта
FROM maven:3.8.6-openjdk-21-slim AS build
WORKDIR /app
COPY . .
RUN mvn clean package

# Используем легковесный образ для запуска приложения
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/target/simple-http-server-1.0-SNAPSHOT.jar /app/simple-http-server-1.0-SNAPSHOT.jar
EXPOSE 8080
CMD ["java", "-jar", "simple-http-server-1.0-SNAPSHOT.jar"]