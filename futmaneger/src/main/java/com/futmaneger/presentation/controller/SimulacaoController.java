package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.SimulacaoRequestDTO;
import com.futmaneger.application.dto.SimulacaoResponseDTO;
import com.futmaneger.application.usecase.simulacao.SimulacaoUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simulacoes")
public class SimulacaoController {

    private final SimulacaoUseCase simulacaoUseCase;

    public SimulacaoController(SimulacaoUseCase simulacaoUseCase) {
        this.simulacaoUseCase = simulacaoUseCase;
    }

    @PostMapping
    public ResponseEntity<SimulacaoResponseDTO> simular(@RequestBody SimulacaoRequestDTO request) {
        return ResponseEntity.ok(simulacaoUseCase.simular(request));
    }
}
