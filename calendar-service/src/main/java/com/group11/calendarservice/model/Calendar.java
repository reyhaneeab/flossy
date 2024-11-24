package com.group11.calendarservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(value="calendar")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Calendar {
    @Id
    private String id;
    private String date;
    private String time;
    private String dentistName;
}
