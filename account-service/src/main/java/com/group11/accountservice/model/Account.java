package com.group11.accountservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(value="account")
public class Account {


        @Id
        private String id;
        private String username;
        private String firstName;
        private String lastName;
        private String phone;
        private String email;
        private String password;
    }

