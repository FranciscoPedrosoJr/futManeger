package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.ExtratoClubeResponseDTO;
import com.futmaneger.application.usecase.financeiro.ConsultarExtratoUseCase;
import com.futmaneger.application.dto.PainelFinanceiroClubeResponseDTO;
import com.futmaneger.application.usecase.financeiro.ConsultarPainelFinanceiroUseCase;
import org.springframework.web.bind.annotation.RequestParam;
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
    private final ConsultarPainelFinanceiroUseCase consultarPainelFinanceiroUseCase;

    public FinanceiroController(ConsultarExtratoUseCase consultarExtratoUseCase,
                                ConsultarPainelFinanceiroUseCase consultarPainelFinanceiroUseCase) {
        this.consultarExtratoUseCase = consultarExtratoUseCase;
        this.consultarPainelFinanceiroUseCase = consultarPainelFinanceiroUseCase;
    }

    @GetMapping("/clubes/{clubeId}/extrato")
    public ResponseEntity<ExtratoClubeResponseDTO> extrato(@PathVariable Long clubeId) {
        return ResponseEntity.ok(consultarExtratoUseCase.executar(clubeId));
    }

    @GetMapping("/meu-clube")
    public ResponseEntity<PainelFinanceiroClubeResponseDTO> meuClube(
            @RequestParam(defaultValue = "50") int ultimasTransacoes,
            @RequestParam(defaultValue = "12") int mesesProjecao) {
        return ResponseEntity.ok(consultarPainelFinanceiroUseCase.executarParaUsuario(ultimasTransacoes, mesesProjecao));
    }

    @GetMapping("/clubes/{clubeId}/painel")
    public ResponseEntity<PainelFinanceiroClubeResponseDTO> painel(
            @PathVariable Long clubeId,
            @RequestParam(defaultValue = "50") int ultimasTransacoes,
            @RequestParam(defaultValue = "12") int mesesProjecao) {
        return ResponseEntity.ok(consultarPainelFinanceiroUseCase.executar(clubeId, ultimasTransacoes, mesesProjecao));
    }
}
