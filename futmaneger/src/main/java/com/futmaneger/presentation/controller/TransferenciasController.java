package com.futmaneger.presentation.controller;

import com.futmaneger.application.usecase.transferencias.ComprarJogadorUseCase;
import com.futmaneger.presentation.request.ComprarJogadorRequest;
import com.futmaneger.presentation.response.ComprarJogadorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transferencias")
public class TransferenciasController {
    private final ComprarJogadorUseCase comprarJogadorUseCase;

    public TransferenciasController(ComprarJogadorUseCase comprarJogadorUseCase) {
        this.comprarJogadorUseCase = comprarJogadorUseCase;
    }

    @PostMapping("/comprar")
    public ResponseEntity<ComprarJogadorResponse> comprar(@RequestBody ComprarJogadorRequest request) {
        ComprarJogadorResponse response = comprarJogadorUseCase.executar(request.clubeId(), request.jogadorId());
        return ResponseEntity.ok(response);
    }
}
