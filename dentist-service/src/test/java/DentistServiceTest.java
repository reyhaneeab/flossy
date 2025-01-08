package com.group11.dentistservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.dentistservice.repository.DentistRepository;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;

public class DentistServiceTest {

    @Mock
    private DentistRepository dentistRepository;

    @Mock
    private MqttClient mqttClient;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private DentistService dentistService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
}
