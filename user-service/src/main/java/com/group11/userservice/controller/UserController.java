package com.group11.userservice.controller;

import com.group11.userservice.dto.SignupRequest;
import com.group11.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest request) {
        try {
            userService.createUser(request);
            log.info("User registered successfully: {}", request.firstName());
            return ResponseEntity.ok().body("User registered successfully");
        } catch (Exception e) {
            log.error("Error registering user", e);
            return ResponseEntity.badRequest().body("Error registering user: " + e.getMessage());
        }
    }
}
