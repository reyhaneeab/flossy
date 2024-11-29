package com.group11.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(value="users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {


        @Id
        private String id;
        private String firstName;
        private String lastName;
        private String phone;
        private String email;
        @JsonIgnore
        private String password;
    }

