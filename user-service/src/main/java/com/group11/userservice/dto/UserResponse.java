package com.group11.userservice.dto;

public record UserResponse( String id,
                            String name,
                            String email,
                            String password,
                            String phone) {
}
