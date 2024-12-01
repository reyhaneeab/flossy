package com.example.apigateway;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api")
public class ApiGatewayController {

    // Handle all requests to the API Gateway endpoint 
    @PostMapping("/{service}/{action}")
    public ResponseEntity<String> handleRequest(
            @PathVariable String service, // The service to route the request to
            @PathVariable String action, // The action to perform on the service
            @RequestBody String payload) {
        // Handle the request and route to the appropriate service
        // This is a placeholder for custom logic if needed
        return ResponseEntity.ok("Request handled for service: " + service + ", action: " + action);
    }
}