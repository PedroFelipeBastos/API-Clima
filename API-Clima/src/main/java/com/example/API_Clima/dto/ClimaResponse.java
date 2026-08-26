package com.example.API_Clima.dto;

import java.time.LocalDateTime;

/**
 * Objeto proprio da aplicacao: e isto que a nossa API devolve.
 *
 * <p>Fica separado do {@link OpenMeteoResponse} de proposito. Se a API
 * externa mudar o formato dela, o contrato do nosso endpoint nao quebra.</p>
 */
public record ClimaResponse(
        String cidade,
        String estado,
        String pais,
        Double latitude,
        Double longitude,
        LocalDateTime dataHoraConsulta,
        Double temperatura,
        Double temperaturaMaxima,
        Double temperaturaMinima,
        Integer umidade,
        Double velocidadeVento,
        Integer direcaoVentoGraus,
        String direcaoVento,
        String descricao) {
}
