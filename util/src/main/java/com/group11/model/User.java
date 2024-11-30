package com.group11.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Indexed;

@Data
@Document(value="users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    }

