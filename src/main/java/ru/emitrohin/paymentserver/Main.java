package ru.emitrohin.paymentserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
//TODO remove jsessionid
//TODO implement roles - paid, admin
//TODO remove all unnecessary comments in html
//TODO send log to ??
//TODO reinvent auth
//TODO fix migration transactionId was unique
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);

        //TODO on init check bot is connected and cloudpayments is ok
    }
}
