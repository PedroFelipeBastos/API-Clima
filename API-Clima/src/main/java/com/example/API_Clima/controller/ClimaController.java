package com.example.API_Clima.controller;
 
import java.util.Map;
 
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
 
import com.example.API_Clima.service.ClimaService;
 
@RestController
public class ClimaController {
 
    private final ClimaService service;
 
    public ClimaController(ClimaService service) {
        this.service = service;
    }
 
    @GetMapping("/clima/belo-horizonte")
    public Map<String, Object> climaBeloHorizonte() {
        return service.preverTempo();
    }
}