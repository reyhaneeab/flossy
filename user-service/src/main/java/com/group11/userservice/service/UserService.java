package com.group11.userservice.service;

import com.group11.userservice.dto.UserRequest;
import com.group11.userservice.dto.UserResponse;
import com.group11.userservice.model.User;
import com.group11.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;


    public UserResponse createUser(UserRequest userRequest) {
        User user = User.builder()
                .name(userRequest.name())
                .email(userRequest.email())
                .password(userRequest.password())
                .phone(userRequest.phone())
                .build();
        userRepository.save(user);
        log.info("User created successfully");
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPassword(), user.getPhone());
    }

}