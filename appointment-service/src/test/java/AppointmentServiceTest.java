package com.group11.appointmentservice.service;

import com.group11.appointmentservice.repository.TimeSlotRepository;
import com.group11.appointmentservice.repository.UserRepository;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.*;


public class AppointmentServiceTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MqttClient mqttClient;

    @InjectMocks
    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
}
