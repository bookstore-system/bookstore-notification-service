package com.hamtech.bookstorenotificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BookstoreNotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookstoreNotificationServiceApplication.class, args);
    }

}
