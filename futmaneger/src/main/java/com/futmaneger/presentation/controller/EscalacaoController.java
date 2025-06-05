package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.EscalacaoRequestDTO;
import com.futmaneger.application.dto.EscalacaoResponseDTO;
import com.futmaneger.application.usecase.escalacao.EscalacaoUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/escalacao")
public class EscalacaoController {

    private final EscalacaoUseCase useCase;

    public EscalacaoController(EscalacaoUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<EscalacaoResponseDTO> escalar(@RequestBody EscalacaoRequestDTO request) {
        EscalacaoResponseDTO response = useCase.escalar(request);
        return ResponseEntity.ok(response);
    }
}
