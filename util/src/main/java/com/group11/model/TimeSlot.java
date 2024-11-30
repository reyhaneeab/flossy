package com.group11.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(value = "timeSlot")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class TimeSlot {
    @Id
    private String id;
    private String dentistName;
    private boolean booked;
    private LocalDate date;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @DBRef
    private Dentist owner;
    @DBRef
    private Calendar calendar;
    @DBRef
    private User user;
}