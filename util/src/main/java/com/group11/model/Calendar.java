package com.group11.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@Document(value = "calendars")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@TypeAlias("calendar")
public class Calendar {
    @Id
    private String id;
    private int year;
    private int weekNumber;
    @DocumentReference
    private List<TimeSlot> timeSlots;
    @DocumentReference
    private Dentist owner;
}
