package com.example.API_Clima.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Espelha o JSON da API de geocodificacao do Open-Meteo, usada para
 * descobrir a latitude/longitude a partir do nome da cidade.
 *
 * <p>Atencao: quando nenhuma cidade e encontrada, o Open-Meteo simplesmente
 * OMITE o campo "results" — ele chega como null, e nao como lista vazia.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeocodingResponse(List<Resultado> results) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Resultado(
            String name,
            Double latitude,
            Double longitude,
            String country,
            String admin1,
            String timezone) {
    }
}
