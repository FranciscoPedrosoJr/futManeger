package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.CriarTemporadaRequestDTO;
import com.futmaneger.application.dto.TemporadaResponseDTO;
import com.futmaneger.application.usecase.temporada.AdicionarCampeonatoNaTemporadaUseCase;
import com.futmaneger.application.usecase.temporada.BuscarTemporadasUseCase;
import com.futmaneger.application.usecase.temporada.CriarTemporadaUseCase;
import com.futmaneger.application.usecase.temporada.FinalizarTemporadaUseCase;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/temporadas")
@SecurityRequirement(name = "bearerAuth")
public class TemporadaController {

    private final CriarTemporadaUseCase criarTemporadaUseCase;
    private final BuscarTemporadasUseCase buscarTemporadasUseCase;
    private final AdicionarCampeonatoNaTemporadaUseCase adicionarCampeonatoNaTemporadaUseCase;
    private final FinalizarTemporadaUseCase finalizarTemporadaUseCase;

    public TemporadaController(CriarTemporadaUseCase criarTemporadaUseCase,
                               BuscarTemporadasUseCase buscarTemporadasUseCase,
                               AdicionarCampeonatoNaTemporadaUseCase adicionarCampeonatoNaTemporadaUseCase,
                               FinalizarTemporadaUseCase finalizarTemporadaUseCase) {
        this.criarTemporadaUseCase = criarTemporadaUseCase;
        this.buscarTemporadasUseCase = buscarTemporadasUseCase;
        this.adicionarCampeonatoNaTemporadaUseCase = adicionarCampeonatoNaTemporadaUseCase;
        this.finalizarTemporadaUseCase = finalizarTemporadaUseCase;
    }

    @PostMapping
    public ResponseEntity<TemporadaResponseDTO> criar(@RequestBody(required = false) CriarTemporadaRequestDTO request) {
        TemporadaResponseDTO response = criarTemporadaUseCase.executar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TemporadaResponseDTO>> listar() {
        return ResponseEntity.ok(buscarTemporadasUseCase.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemporadaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(buscarTemporadasUseCase.buscarPorId(id));
    }

    @PostMapping("/{temporadaId}/campeonatos/{campeonatoId}")
    public ResponseEntity<TemporadaResponseDTO> adicionarCampeonato(@PathVariable Long temporadaId,
                                                                    @PathVariable Long campeonatoId) {
        TemporadaResponseDTO response =
                adicionarCampeonatoNaTemporadaUseCase.executar(temporadaId, campeonatoId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<TemporadaResponseDTO> finalizar(@PathVariable Long id) {
        return ResponseEntity.ok(finalizarTemporadaUseCase.executar(id));
    }
}
