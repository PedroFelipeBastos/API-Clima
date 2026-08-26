package com.example.API_Clima.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.API_Clima.dto.ClimaResponse;
import com.example.API_Clima.service.ClimaService;

/**
 * Endpoints REST da aplicacao.
 *
 * <p>O controller so recebe a requisicao e devolve a resposta. A chamada a
 * API externa fica toda no service.</p>
 */
@RestController
@RequestMapping("/clima")
public class ClimaController {

    private final ClimaService climaService;

    public ClimaController(ClimaService climaService) {
        this.climaService = climaService;
    }

    /**
     * GET /clima                    -> Belo Horizonte (padrao)
     * GET /clima?cidade=Ouro Preto  -> cidade informada
     */
    @GetMapping
    public ClimaResponse clima(@RequestParam(required = false) String cidade) {
        return climaService.consultarClima(cidade);
    }

    /** GET /clima/belo-horizonte */
    @GetMapping("/belo-horizonte")
    public ClimaResponse climaBeloHorizonte() {
        return climaService.consultarClima(null);
    }

    /**
     * GET /clima/cidade/Ouro Preto
     * Mesma consulta acima, com a cidade na propria URL.
     */
    @GetMapping("/cidade/{nome}")
    public ClimaResponse climaPorCidade(@PathVariable String nome) {
        return climaService.consultarClima(nome);
    }
}
