package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.CadastrarPaisRequestDTO;
import com.futmaneger.application.dto.PaisResponseDTO;
import com.futmaneger.application.usecase.pais.CadastrarPaisUseCase;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/paises")
@SecurityRequirement(name = "bearerAuth")
public class PaisController {

    private final CadastrarPaisUseCase cadastrarPaisUseCase;

    public PaisController(CadastrarPaisUseCase cadastrarPaisUseCase) {
        this.cadastrarPaisUseCase = cadastrarPaisUseCase;
    }

    @PostMapping
    public ResponseEntity<PaisResponseDTO> cadastrar(@RequestBody CadastrarPaisRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastrarPaisUseCase.cadastrar(request));
    }

    @GetMapping
    public ResponseEntity<List<PaisResponseDTO>> listar() {
        return ResponseEntity.ok(cadastrarPaisUseCase.listar());
    }
}
