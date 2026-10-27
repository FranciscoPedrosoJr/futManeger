package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.CadastrarFaseCampeonatoRequestDTO;
import com.futmaneger.application.dto.FaseCampeonatoResponseDTO;
import com.futmaneger.application.usecase.campeonato.GerenciarFaseCampeonatoUseCase;
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
@RequestMapping("/fases-campeonato")
@SecurityRequirement(name = "bearerAuth")
public class FaseCampeonatoController {

    private final GerenciarFaseCampeonatoUseCase useCase;

    public FaseCampeonatoController(GerenciarFaseCampeonatoUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<FaseCampeonatoResponseDTO> cadastrar(
            @RequestBody CadastrarFaseCampeonatoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.cadastrar(request));
    }

    @GetMapping("/campeonatos/{campeonatoId}")
    public ResponseEntity<List<FaseCampeonatoResponseDTO>> listarPorCampeonato(
            @PathVariable Long campeonatoId) {
        return ResponseEntity.ok(useCase.listarPorCampeonato(campeonatoId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        useCase.remover(id);
        return ResponseEntity.noContent().build();
    }
}
