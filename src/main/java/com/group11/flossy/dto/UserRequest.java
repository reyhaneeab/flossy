package com.group11.flossy.dto;

public record UserRequest(  String id,
                            String name,
                            String email,
                            String password,
                            String phone) {
}
