package com.example.API_Clima.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.example.API_Clima.dto.ClimaResponse;
import com.example.API_Clima.dto.GeocodingResponse;
import com.example.API_Clima.dto.OpenMeteoResponse;

/**
 * Consome a API externa de clima e monta o objeto de resposta da aplicacao.
 */
@Service
public class ClimaService {

    /** Cidade e coordenadas usadas na consulta. */
    private record Localizacao(
            String cidade, String estado, String pais,
            double latitude, double longitude, String timezone) {
    }

    /**
     * Belo Horizonte fica com as coordenadas fixas: assim o endpoint padrao
     * do trabalho funciona sem depender da API de geocodificacao.
     */
    private static final Localizacao BELO_HORIZONTE = new Localizacao(
            "Belo Horizonte", "Minas Gerais", "Brasil",
            -19.9167, -43.9345, "America/Sao_Paulo");

    private static final String CAMPOS_ATUAIS =
            "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,wind_direction_10m";
    private static final String CAMPOS_DIARIOS =
            "temperature_2m_max,temperature_2m_min";

    private static final String SEM_DESCRICAO = "Condicao nao informada";

    /**
     * O Open-Meteo nao manda a condicao do tempo em texto: manda um codigo
     * numerico do padrao WMO 4677. Esta tabela faz a traducao.
     */
    private static final Map<Integer, String> DESCRICOES = Map.ofEntries(
            Map.entry(0, "Céu limpo"),
            Map.entry(1, "Predominantemente limpo"),
            Map.entry(2, "Parcialmente nublado"),
            Map.entry(3, "Nublado"),
            Map.entry(45, "Nevoeiro"),
            Map.entry(48, "Nevoeiro com formação de gelo"),
            Map.entry(51, "Garoa leve"),
            Map.entry(53, "Garoa moderada"),
            Map.entry(55, "Garoa intensa"),
            Map.entry(61, "Chuva fraca"),
            Map.entry(63, "Chuva moderada"),
            Map.entry(65, "Chuva forte"),
            Map.entry(71, "Neve fraca"),
            Map.entry(73, "Neve moderada"),
            Map.entry(75, "Neve forte"),
            Map.entry(80, "Pancadas de chuva fracas"),
            Map.entry(81, "Pancadas de chuva moderadas"),
            Map.entry(82, "Pancadas de chuva fortes"),
            Map.entry(95, "Trovoada"),
            Map.entry(96, "Trovoada com granizo leve"),
            Map.entry(99, "Trovoada com granizo forte"));

    /** Rosa dos ventos: cada ponto cobre uma fatia de 22,5 graus. */
    private static final String[] DIRECOES = {
            "Norte", "Nor-nordeste", "Nordeste", "Lés-nordeste",
            "Leste", "Lés-sudeste", "Sudeste", "Su-sudeste",
            "Sul", "Su-sudoeste", "Sudoeste", "Oés-sudoeste",
            "Oeste", "Oés-noroeste", "Noroeste", "Nor-noroeste"
    };

    private final RestClient climaClient;
    private final RestClient geocodingClient;

    public ClimaService(
            @Value("${clima.api.url}") String urlClima,
            @Value("${clima.api.geocoding-url}") String urlGeocoding) {
        this.climaClient = RestClient.builder().baseUrl(urlClima).build();
        this.geocodingClient = RestClient.builder().baseUrl(urlGeocoding).build();
    }

    /**
     * Consulta o clima atual.
     *
     * @param cidade nome da cidade; se vier null ou vazio, usa Belo Horizonte
     */
    public ClimaResponse consultarClima(String cidade) {
        Localizacao local = (cidade == null || cidade.isBlank())
                ? BELO_HORIZONTE
                : buscarCoordenadas(cidade.trim());

        OpenMeteoResponse resposta = buscarClimaNaApiExterna(local);

        OpenMeteoResponse.Current atual = resposta.current();
        OpenMeteoResponse.Daily diario = resposta.daily();

        return new ClimaResponse(
                local.cidade(),
                local.estado(),
                local.pais(),
                local.latitude(),
                local.longitude(),
                agoraNoFuso(local.timezone()),
                atual.temperatura(),
                primeiro(diario == null ? null : diario.temperaturaMaxima()),
                primeiro(diario == null ? null : diario.temperaturaMinima()),
                atual.umidade(),
                atual.velocidadeVento(),
                atual.direcaoVento(),
                direcaoDoVento(atual.direcaoVento()),
                descricaoDoTempo(atual.codigoTempo()));
    }

    /** Descobre latitude/longitude a partir do nome da cidade. */
    private Localizacao buscarCoordenadas(String cidade) {
        GeocodingResponse resposta;

        try {
            resposta = geocodingClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("name", cidade)
                            .queryParam("count", 1)
                            .queryParam("language", "pt")
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .body(GeocodingResponse.class);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Falha na comunicacao com a API de geocodificacao.", e);
        }

        // Sem resultado, o Open-Meteo omite o campo "results" por completo:
        // por isso o teste de null antes do isEmpty().
        if (resposta == null || resposta.results() == null || resposta.results().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Cidade nao encontrada: " + cidade);
        }

        GeocodingResponse.Resultado r = resposta.results().get(0);

        return new Localizacao(
                r.name(),
                r.admin1(),
                r.country(),
                r.latitude(),
                r.longitude(),
                r.timezone() != null ? r.timezone() : "auto");
    }

    /**
     * Faz a chamada HTTP do clima e trata os erros basicos exigidos no
     * enunciado: falha na comunicacao e dados indisponiveis.
     */
    private OpenMeteoResponse buscarClimaNaApiExterna(Localizacao local) {
        OpenMeteoResponse resposta;

        try {
            resposta = climaClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("latitude", local.latitude())
                            .queryParam("longitude", local.longitude())
                            .queryParam("current", CAMPOS_ATUAIS)
                            .queryParam("daily", CAMPOS_DIARIOS)
                            .queryParam("timezone", local.timezone())
                            .queryParam("forecast_days", 1)
                            .build())
                    .retrieve()
                    .body(OpenMeteoResponse.class);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Falha na comunicacao com a API de clima.", e);
        }

        if (resposta == null || resposta.current() == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Dados de clima indisponiveis no momento.");
        }
        return resposta;
    }

    /** Data e hora atuais no fuso da cidade consultada. */
    private LocalDateTime agoraNoFuso(String timezone) {
        try {
            return ZonedDateTime.now(ZoneId.of(timezone)).toLocalDateTime();
        } catch (RuntimeException e) {
            return LocalDateTime.now();
        }
    }

    private Double primeiro(List<Double> lista) {
        return (lista == null || lista.isEmpty()) ? null : lista.get(0);
    }

    private String descricaoDoTempo(Integer codigo) {
        return codigo == null ? SEM_DESCRICAO : DESCRICOES.getOrDefault(codigo, SEM_DESCRICAO);
    }

    private String direcaoDoVento(Integer graus) {
        if (graus == null) {
            return "Indisponível";
        }
        int normalizado = Math.floorMod(graus, 360);
        return DIRECOES[(int) Math.round(normalizado / 22.5) % 16];
    }
}
