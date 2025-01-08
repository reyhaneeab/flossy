package com.group11.appointmentservice.service;

import com.group11.appointmentservice.repository.TimeSlotRepository;
import com.group11.appointmentservice.repository.UserRepository;
import com.group11.model.TimeSlot;
import com.group11.model.User;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;


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

    @Test
    void testBookAppointment_Success() throws Exception {
        //fake/mock request payload
        String payload = "{ \"timeSlotId\": \"123\", \"userId\": \"456\" }";
        MqttMessage message = new MqttMessage(payload.getBytes());
        //faek/mock timeslot
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setId("123");
        timeSlot.setBooked(false);

        User user = new User();
        user.setId("456");

        Mockito.when(timeSlotRepository.findById("123")).thenReturn(java.util.Optional.of(timeSlot));
        Mockito.when(userRepository.findById("456")).thenReturn(java.util.Optional.of(user));

        appointmentService.bookAppointment(message);

        assertTrue(timeSlot.getBooked());
        assertEquals(user, timeSlot.getBookedBy());
        Mockito.verify(mqttClient).publish(Mockito.eq("timeSlot/book/response"), Mockito.any(), Mockito.eq(1), Mockito.eq(false));
    }

    @Test
    void testBookAppointment_AlreadyBooked() throws Exception { //simulates the appoitment already being booked
        String payload = "{ \"timeSlotId\": \"123\", \"userId\": \"456\" }";
        MqttMessage message = new MqttMessage(payload.getBytes());

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setId("123");
        timeSlot.setBooked(true);

        User user = new User();
        user.setId("456");

        Mockito.when(timeSlotRepository.findById("123")).thenReturn(java.util.Optional.of(timeSlot));
        Mockito.when(userRepository.findById("456")).thenReturn(java.util.Optional.of(user));

        Exception exception = assertThrows(Exception.class, () -> {
            appointmentService.bookAppointment(message);
        });

        assertEquals("Appointment is already booked", exception.getMessage());
    }

    @Test
    void testCancelAppointment_Successful() throws Exception {
        String payload = "{ \"timeSlotId\": \"123\", \"userId\": \"456\" }";
        MqttMessage message = new MqttMessage(payload.getBytes());

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setId("123");
        User user = new User();
        user.setId("456");

        timeSlot.setBooked(true);
        timeSlot.setBookedBy(user);

        Mockito.when(timeSlotRepository.findById("123")).thenReturn(java.util.Optional.of(timeSlot));
        Mockito.when(userRepository.findById("456")).thenReturn(java.util.Optional.of(user));

        appointmentService.cancelAppointment(message);

        assertTrue(timeSlot.getBooked());
        assertNull(timeSlot.getBookedBy());
        Mockito.verify(timeSlotRepository).save(timeSlot);
        Mockito.verify(mqttClient).publish(Mockito.eq("timeSlot/cancel/response"), Mockito.any(), Mockito.eq(1), Mockito.eq(false));
    }


}
