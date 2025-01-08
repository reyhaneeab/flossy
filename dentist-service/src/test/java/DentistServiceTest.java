package com.group11.dentistservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.dto.DentistRequest;
import com.group11.model.Dentist;
import com.group11.dentistservice.repository.DentistRepository;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
class DentistServiceTest {

    @Mock
    private DentistRepository dentistRepository;

    @Mock
    private MqttClient mqttClient;

    @InjectMocks
    private DentistService dentistService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        DentistRequest request = new DentistRequest("Bright Smiles", "dentist@example.com", "securePass", "123 Main St", "1234567890", null);
        System.out.println("DentistRequest: " + request);

    }

    @Test
    void testDentistRequestSerialization() throws Exception {
        DentistRequest request = new DentistRequest("Bright Smiles", "dentist@example.com", "securePass", "123 Main St", "1234567890", null);
        System.out.println("DentistRequest: " + request);

        Dentist dentist = Dentist.builder()
                .officeName(request.officeName())
                .email(request.email())
                .password(request.password())
                .address(request.address())
                .phone(request.phone())
                .doctors(request.doctors())
                .build();

        byte[] payload = objectMapper.writeValueAsBytes(request);
        System.out.println("Serialized Payload: " + (payload != null ? new String(payload) : "null"));

        assertNotNull(payload, "Payload should not be null");
    }
}

