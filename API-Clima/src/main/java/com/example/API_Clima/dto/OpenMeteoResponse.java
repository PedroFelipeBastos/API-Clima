package com.example.API_Clima.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Espelha o JSON devolvido pela API externa (Open-Meteo).
 *
 * <p>ignoreUnknown = true e necessario: o Open-Meteo devolve varios campos
 * extras (generationtime_ms, elevation, current_units...) que nao usamos.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoResponse(Current current, Daily daily) {

    /** Bloco "current": medicao mais recente. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Current(
            String time,
            @JsonProperty("temperature_2m") Double temperatura,
            @JsonProperty("relative_humidity_2m") Integer umidade,
            @JsonProperty("weather_code") Integer codigoTempo,
            @JsonProperty("wind_speed_10m") Double velocidadeVento,
            @JsonProperty("wind_direction_10m") Integer direcaoVento) {
    }

    /** Bloco "daily": o Open-Meteo devolve listas, uma posicao por dia. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Daily(
            @JsonProperty("temperature_2m_max") List<Double> temperaturaMaxima,
            @JsonProperty("temperature_2m_min") List<Double> temperaturaMinima) {
    }
}
