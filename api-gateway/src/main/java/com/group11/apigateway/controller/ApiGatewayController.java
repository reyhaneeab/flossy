package com.group11.apigateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import com.group11.apigateway.handler.MqttHandler;

    @RestController
    @RequestMapping("/api")
    public class ApiGatewayController {
        @Autowired
        private MqttHandler mqttHandler;

        @PostMapping("/user/create")
        public String createUser(@RequestBody String request) throws Exception {
            return mqttHandler.forwardRequest("user/registration", request);
        }
    }

