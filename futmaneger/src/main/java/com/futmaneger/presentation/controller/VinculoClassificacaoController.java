package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.CadastrarVinculoClassificacaoRequestDTO;
import com.futmaneger.application.dto.VinculoClassificacaoResponseDTO;
import com.futmaneger.application.usecase.classificacao.GerenciarVinculoClassificacaoUseCase;
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
@RequestMapping("/vinculos-classificacao")
@SecurityRequirement(name = "bearerAuth")
public class VinculoClassificacaoController {

    private final GerenciarVinculoClassificacaoUseCase useCase;

    public VinculoClassificacaoController(GerenciarVinculoClassificacaoUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<VinculoClassificacaoResponseDTO> cadastrar(
            @RequestBody CadastrarVinculoClassificacaoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.cadastrar(request));
    }

    @GetMapping
    public ResponseEntity<List<VinculoClassificacaoResponseDTO>> listar() {
        return ResponseEntity.ok(useCase.listar());
    }

    @GetMapping("/campeonatos/{campeonatoId}")
    public ResponseEntity<List<VinculoClassificacaoResponseDTO>> listarPorCampeonato(
            @PathVariable Long campeonatoId) {
        return ResponseEntity.ok(useCase.listarPorCampeonatoOrigem(campeonatoId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        useCase.remover(id);
        return ResponseEntity.noContent().build();
    }
}
