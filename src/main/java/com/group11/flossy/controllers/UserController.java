package com.group11.flossy.controllers;

import com.group11.flossy.dto.UserResponse;
import com.group11.flossy.models.User;
import com.group11.flossy.dto.UserRequest;
import com.group11.flossy.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody UserRequest userRequest){
        return userService.createUser(userRequest);
    }

}
