package com.group11.userservice.test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.model.User;
import com.group11.dto.UserRequest;
import com.group11.userservice.repository.UserRepository;
import com.group11.userservice.service.UserService;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MqttClient mqttClient;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testCreateUser_Success() throws Exception {
        UserRequest request = new UserRequest("test@example.com", "John", "Doe", "1234567890", "password");
        MqttMessage mqttMessage = new MqttMessage(new ObjectMapper().writeValueAsBytes(request));

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");

        userService.createUser(mqttMessage);

        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());

        verify(mqttClient, times(1)).publish(eq("user/registration/response"), any(byte[].class), eq(1), eq(false));
    }

    @Test
    void testCreateUser_EmailAlreadyExists() throws Exception {
        // Arrange
        UserRequest request = new UserRequest("existing@example.com", "Jane", "Smith", "0987654321", "password");
        MqttMessage mqttMessage = new MqttMessage(new ObjectMapper().writeValueAsBytes(request));

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> userService.createUser(mqttMessage));
        assertEquals("Email already exists", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
        verify(mqttClient, never()).publish(anyString(), any(byte[].class), anyInt(), anyBoolean());
    }

    @Test
    void testInit_SuccessfulConnection() throws Exception {
        when(mqttClient.isConnected()).thenReturn(false);

        userService.init();

        verify(mqttClient, times(1)).isConnected();
        verify(mqttClient, times(1)).subscribe("user/registration");
    }

    @Test
    void testInit_ConnectionAlreadyEstablished() throws Exception {
        // Arrange
        when(mqttClient.isConnected()).thenReturn(true);

        // Act
        userService.init();

        // Assert
        verify(mqttClient, never()).connect(); // No connection needed if already connected
        verify(mqttClient, times(1)).subscribe("user/registration");
    }
}
