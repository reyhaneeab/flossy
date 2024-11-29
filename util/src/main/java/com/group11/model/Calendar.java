package com.group11.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(value = "calendar")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Calendar {
    @Id
    private String date;
    private String time;
    @Builder.Default
    private List<TimeSlot> timeSlots = new ArrayList<>();
    private String dentistOffice;
}
