package com.dss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
public class BirthdayEmailServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BirthdayEmailServiceApplication.class, args);
    }
}
