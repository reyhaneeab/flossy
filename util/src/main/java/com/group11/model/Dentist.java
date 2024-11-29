package com.group11.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;
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
    @NotNull
    private String officeName;
    @NotNull
    private String email;
    @NotNull
    private String password;
    private String phone;
    @NotNull
    private String address;
    private List<String> doctors;




}