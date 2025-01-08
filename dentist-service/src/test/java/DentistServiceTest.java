package com.group11.dentistservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.dto.DentistRequest;
import com.group11.model.Dentist;
import com.group11.dentistservice.repository.DentistRepository;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;

class DentistServiceTest {

    @Mock
    private DentistRepository dentistRepository;

    @Mock
    private MqttClient mqttClient;

    @InjectMocks
    private DentistService dentistService;

    private ObjectMapper objectMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> ops;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();

        //pass all dependencies to the constructor
        dentistService = new DentistService(objectMapper, dentistRepository, mqttClient, redisTemplate);

        Mockito.when(redisTemplate.opsForValue()).thenReturn(ops);
        Mockito.doNothing().when(ops).set(Mockito.anyString(), Mockito.any(), Mockito.anyLong(), Mockito.any());


        DentistRequest request = new DentistRequest("Bright Smiles", "dentist@example.com", "securePass", "123 Main St", "1234567890", null);
        System.out.println("DentistRequest: " + request);
    }


    @Test
    void testDentistRequest() throws Exception {
        DentistRequest request = new DentistRequest("Flossy", "flossy@gmail.com", "Supersecurepassword", "backerStreer", "1234567890", null);
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

    @Test
    void testCreateDentistMessage() throws Exception {
        DentistRequest request = createDentistRequest();
        System.out.println("DentistRequest: " + request);

        MqttMessage message = createMqttMessage(request);

        Dentist dentist = Dentist.builder()
                .officeName(request.officeName())
                .email(request.email())
                .password(request.password())
                .address(request.address())
                .phone(request.phone())
                .doctors(request.doctors())
                .build();

        Mockito.when(dentistRepository.save(Mockito.any(Dentist.class))).thenReturn(dentist);

        dentistService.getCreateDentistMessage(message);

        ArgumentCaptor<Dentist> dentistCaptor = ArgumentCaptor.forClass(Dentist.class);
        Mockito.verify(dentistRepository).save(dentistCaptor.capture());

        Dentist capturedDentist = dentistCaptor.getValue();
        assertNotNull(capturedDentist);
        assertEquals("Bright Smiles", capturedDentist.getOfficeName());
        assertEquals("dentist@example.com", capturedDentist.getEmail());
    }

    private DentistRequest createDentistRequest() {
        return new DentistRequest("Bright Smiles", "dentist@example.com", "securePass", "123 Main St", "1234567890", null);
    }

    private MqttMessage createMqttMessage(DentistRequest request) throws Exception {
        MqttMessage message = new MqttMessage();
        byte[] payload = objectMapper.writeValueAsBytes(request);
        System.out.println("Serialized Payload: " + (payload != null ? new String(payload) : "null"));

        if (payload != null) {
            message.setPayload(payload);
        } else {
            throw new IllegalArgumentException("Failed to serialize DentistRequest to array");
        }
        return message;
    }
}
