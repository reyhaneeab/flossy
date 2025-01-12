package com.group11.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(value="dentists")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@TypeAlias("dentist")
public class Dentist {
    @Id
    private String id;
    @Field("email")
    private String email;
    private String password;
    private String officeName;
    @Field("address")
    private String address;
    private String phone;
    private List<String> doctors;
}