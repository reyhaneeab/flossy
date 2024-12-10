package com.group11.apigateway.exception;
// Custom exception for service unavailable
public class ServiceUnavailableException extends RuntimeException{
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
