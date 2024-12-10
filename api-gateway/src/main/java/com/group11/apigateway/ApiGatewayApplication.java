package com.group11.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service", r -> r.path("/users/**")
                .uri("http://localhost:8090"))
            .route("dentist-service", r -> r.path("/dentists/**")
                .uri("http://localhost:8084"))
            .route("appointment-service", r -> r.path("/appointments/**")
                .uri("http://localhost:8088"))
            .route("calendar-service", r -> r.path("/calendar/**")
                .uri("http://localhost:8083"))
                .route("fallback", r -> r.path("/**")
                        .uri("http://localhost:8080"))

            .build();
    }

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}