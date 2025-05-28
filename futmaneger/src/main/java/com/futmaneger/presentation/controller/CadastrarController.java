package com.futmaneger.presentation.controller;

import com.futmaneger.application.usecase.clube.CadastrarClubeUseCase;
import com.futmaneger.presentation.request.CadastrarClubeRequest;
import com.futmaneger.presentation.response.ClubeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cadastrar")
public class CadastrarController {
    private final CadastrarClubeUseCase cadastrarClubeUseCase;

    public CadastrarController(CadastrarClubeUseCase cadastrarClubeUseCase) {
        this.cadastrarClubeUseCase = cadastrarClubeUseCase;
    }

    @PostMapping("/clubes")
    public ResponseEntity<ClubeResponse> cadastrar(@RequestBody CadastrarClubeRequest request) {
        var clube = cadastrarClubeUseCase.executar(request.nome(), request.cidade());
        return ResponseEntity.ok(new ClubeResponse(clube));
    }
}
