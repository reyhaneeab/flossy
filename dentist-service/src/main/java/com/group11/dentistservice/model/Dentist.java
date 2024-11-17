package com.group11.dentistservice.model;

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
public class Dentist {
    @Id
    private String id;
    private String officeName;
    private String officeEmail;
    private String password;
    private String phone;
    private String address;
    private List<String> pastAppointments;
    private List<String> doctors;




}