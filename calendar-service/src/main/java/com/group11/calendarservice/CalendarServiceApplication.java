package com.group11.calendarservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class CalendarServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(CalendarServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CalendarServiceApplication.class, args);
        logger.info("Calendar Service started successfully");
    }

}
