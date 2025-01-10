package com.group11.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(value = "calendar")
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
    @DBRef
    private List<TimeSlot> timeSlots;
    @DBRef
    private Dentist owner;
}
