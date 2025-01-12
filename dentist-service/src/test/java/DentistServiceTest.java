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

public class DentistServiceTest {

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

        dentistService = new DentistService(objectMapper, dentistRepository, mqttClient, redisTemplate);

        Mockito.when(redisTemplate.opsForValue()).thenReturn(ops);
        Mockito.doNothing().when(ops).set(Mockito.anyString(), Mockito.any(), Mockito.anyLong(), Mockito.any());

        DentistRequest request = new DentistRequest("Flossy", "flossy@gmail.com", "Supersecurepassword", "backerStreer", "1234567890", null);
        System.out.println("DentistRequest: " + request);
    }


    @Test
    void testDentistRequest() throws Exception {
        DentistRequest request = new DentistRequest("Flossy", "flossy@gmail.com", "Supersecurepassword", "backerStreer", "1234567890", null);
        System.out.println("DentistRequest: " + request);

        byte[] payload = objectMapper.writeValueAsBytes(request);
        System.out.println("Serialized Payload: " + (payload != null ? new String(payload) : "null"));

        assertNotNull(payload, "Payload should not be null");
    }

    @Test
    void testCreateDentistMessage() throws Exception {
        DentistRequest request = testCreateDentistRequest();
        System.out.println("DentistRequest: " + request);

        MqttMessage message = testCreateMqttMessage(request);

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
        assertEquals("Flossy", capturedDentist.getOfficeName());
        assertEquals("flossy@gmail.com", capturedDentist.getEmail());
    }

    private DentistRequest testCreateDentistRequest() {
        return new DentistRequest("Flossy", "flossy@gmail.com", "Supersecurepassword", "backerStreer", "1234567890", null);
    }

    private MqttMessage testCreateMqttMessage(DentistRequest request) throws Exception {
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
    @Test
    void testUpdateDentist() {
        Dentist existingDentist = new Dentist("1", "Flossy", "flossy@gmail.com", "Supersecurepassword", "backerStreer", "1234567890", null);
        DentistRequest updatedRequest = new DentistRequest("Flossy2", "flossy2@gmail.com", "Supersecurepassword2", "backerStreer2", "2234567890", null);

        Mockito.when(dentistRepository.findById("1")).thenReturn(java.util.Optional.of(existingDentist));
        Mockito.when(dentistRepository.save(Mockito.any(Dentist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Dentist updatedDentist = dentistService.updateDentist("1", updatedRequest);

        assertNotNull(updatedDentist);
        assertEquals("Flossy2", updatedDentist.getOfficeName());
        assertEquals("flossy2@gmail.com", updatedDentist.getEmail());
        assertEquals("Supersecurepassword2", updatedDentist.getPassword());
        assertEquals("backerStreer2", updatedDentist.getAddress());
        assertEquals("2234567890", updatedDentist.getPhone());
    }

}
