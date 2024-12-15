package com.group11.dentistservice;

import com.mongodb.client.MongoClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableRetry
public class DentistServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DentistServiceApplication.class, args);
    }

}