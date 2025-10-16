package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.SimulacaoRequestDTO;
import com.futmaneger.application.dto.SimulacaoResponseDTO;
import com.futmaneger.application.usecase.simulacao.SimulacaoUseCase;
import com.futmaneger.application.usecase.simulacao.SimularRodadaUseCase;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simulacoes")
public class SimulacaoController {

    private final SimulacaoUseCase simulacaoUseCase;

    private final SimularRodadaUseCase simularRodadaUseCase;

    public SimulacaoController(SimulacaoUseCase simulacaoUseCase, SimularRodadaUseCase simularRodadaUseCase) {
        this.simulacaoUseCase = simulacaoUseCase;
        this.simularRodadaUseCase = simularRodadaUseCase;
    }

    @PostMapping
    public ResponseEntity<SimulacaoResponseDTO> simular(@RequestBody SimulacaoRequestDTO request) {
        return ResponseEntity.ok(simulacaoUseCase.simular(request));
    }

    @PostMapping("/{campeonatoId}/rodadas/{rodadaId}/simulacoes")
    public ResponseEntity<List<SimulacaoResponseDTO>> simularRodada(
            @PathVariable Long campeonatoId,
            @PathVariable Long rodadaId
    ) {
        List<SimulacaoResponseDTO> resultados = simularRodadaUseCase.executar(campeonatoId, rodadaId);
        return ResponseEntity.ok(resultados);
    }
}
