package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.CadastrarCotaEstadualRequestDTO;
import com.futmaneger.application.dto.CotaEstadualResponseDTO;
import com.futmaneger.application.usecase.campeonato.GerenciarCotaEstadualUseCase;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cotas-estaduais")
@SecurityRequirement(name = "bearerAuth")
public class CotaEstadualController {

    private final GerenciarCotaEstadualUseCase useCase;

    public CotaEstadualController(GerenciarCotaEstadualUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<CotaEstadualResponseDTO> cadastrar(
            @RequestBody CadastrarCotaEstadualRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.cadastrar(request));
    }

    @GetMapping("/campeonatos/{campeonatoId}")
    public ResponseEntity<List<CotaEstadualResponseDTO>> listarPorCampeonato(
            @PathVariable Long campeonatoId) {
        return ResponseEntity.ok(useCase.listarPorCampeonato(campeonatoId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        useCase.remover(id);
        return ResponseEntity.noContent().build();
    }
}
