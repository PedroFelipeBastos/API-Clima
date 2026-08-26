package com.example.API_Clima.application;
 
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
 
@SpringBootApplication(scanBasePackages = {"com.example"})
public class ApiClimaApplication {
 
    public static void main(String[] args) {
        SpringApplication.run(ApiClimaApplication.class, args);
    }
 
}