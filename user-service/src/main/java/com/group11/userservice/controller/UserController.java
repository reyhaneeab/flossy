package com.group11.userservice.controller;

import com.group11.model.User;
import com.group11.userservice.config.JwtTokenProvider;
import com.group11.userservice.dto.LoginRequest;
import com.group11.userservice.dto.SignupRequest;
import com.group11.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.group11.userservice.dto.LoginResponse;
import org.springframework.beans.factory.annotation.Value;
@RestController

@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

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
    @PostMapping("/login")
public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
    try {
        User user = userService.loginUser(request);
        String token = jwtTokenProvider.generateToken(user);
        return ResponseEntity.ok().body(new LoginResponse(token));
    } catch (Exception e) {
        log.error("Error logging in user", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }
}

}
