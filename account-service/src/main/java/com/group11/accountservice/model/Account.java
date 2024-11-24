package com.group11.accountservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.springframework.boot.autoconfigure.amqp.RabbitConnectionDetails;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(value="dentist")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
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

