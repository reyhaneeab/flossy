package com.group11.dentistservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class DentistServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DentistServiceApplication.class, args);
    }

}
