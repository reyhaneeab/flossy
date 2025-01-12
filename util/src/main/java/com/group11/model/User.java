package com.group11.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(value = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TypeAlias("user")
public class User {

    @Id
    private String id;

    @Field("email")
    private String email;

    private String firstName;
    private String lastName;
    private String phone;

    @JsonIgnore
    private String password;
    @Builder.Default
    private List<Dentist> interestList = new ArrayList<>();
}