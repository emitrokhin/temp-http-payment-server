package ru.emitrohin.paymentserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
//TODO flyway migration
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
