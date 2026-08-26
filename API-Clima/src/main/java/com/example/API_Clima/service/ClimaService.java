package com.example.API_Clima.service;
 
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
 
@Service
public class ClimaService {
 
    @Value("${clima.api.url}")
    private String apiUrl;
 
    // Coordenadas fixas de Belo Horizonte - MG
    private static final double LATITUDE = -19.9167;
    private static final double LONGITUDE = -43.9345;
 
    // Traducao dos codigos de tempo (padrao WMO) que o Open-Meteo devolve
    private static final Map<Integer, String> DESCRICOES = Map.ofEntries(
            Map.entry(0, "Céu limpo"),
            Map.entry(1, "Predominantemente limpo"),
            Map.entry(2, "Parcialmente nublado"),
            Map.entry(3, "Nublado"),
            Map.entry(45, "Nevoeiro"),
            Map.entry(51, "Garoa leve"),
            Map.entry(61, "Chuva fraca"),
            Map.entry(63, "Chuva moderada"),
            Map.entry(65, "Chuva forte"),
            Map.entry(80, "Pancadas de chuva"),
            Map.entry(95, "Trovoada"));
 
    @SuppressWarnings("unchecked")
    public Map<String, Object> preverTempo() {
        String urlFinal = apiUrl
                + "?latitude=" + LATITUDE
                + "&longitude=" + LONGITUDE
                + "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,wind_direction_10m"
                + "&daily=temperature_2m_max,temperature_2m_min"
                + "&timezone=America/Sao_Paulo"
                + "&forecast_days=1";
 
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> responseEntity;
 
        try {
            responseEntity = restTemplate.getForEntity(urlFinal, Map.class);
        } catch (RestClientException e) {
            return erro("Falha na comunicação com a API de clima.");
        }
 
        if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            return erro("Falha ao obter dados meteorológicos. Código: " + responseEntity.getStatusCode());
        }
 
        Map<String, Object> corpo = responseEntity.getBody();
        Map<String, Object> atual = (Map<String, Object>) corpo.get("current");
        Map<String, Object> diario = (Map<String, Object>) corpo.get("daily");
 
        if (atual == null) {
            return erro("Dados de clima indisponíveis no momento.");
        }
 
        Integer codigoTempo = (Integer) atual.get("weather_code");
 
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("localizacao", "Belo Horizonte - MG");
        resposta.put("dataHoraConsulta", LocalDateTime.now());
        resposta.put("temperaturaAtual", atual.get("temperature_2m"));
        resposta.put("temperaturaMaxima", primeiro(diario == null ? null : (List<Double>) diario.get("temperature_2m_max")));
        resposta.put("temperaturaMinima", primeiro(diario == null ? null : (List<Double>) diario.get("temperature_2m_min")));
        resposta.put("umidade", atual.get("relative_humidity_2m"));
        resposta.put("velocidadeVento", atual.get("wind_speed_10m"));
        resposta.put("direcaoVento", atual.get("wind_direction_10m"));
        resposta.put("descricao", DESCRICOES.getOrDefault(codigoTempo, "Condição não informada"));
 
        return resposta;
    }
 
    private Double primeiro(List<Double> lista) {
        return (lista == null || lista.isEmpty()) ? null : lista.get(0);
    }
 
    private Map<String, Object> erro(String mensagem) {
        Map<String, Object> erro = new LinkedHashMap<>();
        erro.put("erro", mensagem);
        return erro;
    }
}