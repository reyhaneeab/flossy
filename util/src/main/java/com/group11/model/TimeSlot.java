package com.group11.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(value = "timeSlots")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@TypeAlias("timeSlot")
public class TimeSlot {
    @Id
    private String id;
    private Boolean booked;
    private LocalDate date;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @DocumentReference
    private Dentist owner;
    @DocumentReference
    private Calendar calendar;
    @DocumentReference
    private User bookedBy;
}