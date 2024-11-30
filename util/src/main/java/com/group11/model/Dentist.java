package com.group11.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;


@Document(value="dentist_office")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Dentist {
    @Id
    private String id;
    private String officeName;
    private String email;
    private String password;
    private String address;
    private String phone;
    private List<String> doctors;




}