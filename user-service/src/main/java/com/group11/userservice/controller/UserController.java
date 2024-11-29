package com.group11.userservice.controller;

import com.group11.userservice.dto.SignupRequest;
import com.group11.userservice.dto.UserResponse;
import com.group11.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;


    @PostMapping("/signup/form")
    public String registerUserFromForm(@ModelAttribute SignupRequest request) {
        try {
            userService.createUser(request);
            log.info("User registered successfully: {}", request.firstName());
            return "redirect:/success"; // NOTE: Add confirmation message later
        } catch (Exception e) {
            log.error("Error registering user", e);
            return "redirect:/signup?error";
        }
    }}
