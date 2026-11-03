package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.ExtratoClubeResponseDTO;
import com.futmaneger.application.usecase.financeiro.ConsultarExtratoUseCase;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/financeiro")
@SecurityRequirement(name = "bearerAuth")
public class FinanceiroController {

    private final ConsultarExtratoUseCase consultarExtratoUseCase;

    public FinanceiroController(ConsultarExtratoUseCase consultarExtratoUseCase) {
        this.consultarExtratoUseCase = consultarExtratoUseCase;
    }

    @GetMapping("/clubes/{clubeId}/extrato")
    public ResponseEntity<ExtratoClubeResponseDTO> extrato(@PathVariable Long clubeId) {
        return ResponseEntity.ok(consultarExtratoUseCase.executar(clubeId));
    }
}
