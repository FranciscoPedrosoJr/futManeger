package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.AvancoTemporadaResponseDTO;
import com.futmaneger.application.dto.EventoCalendarioResponseDTO;
import com.futmaneger.application.dto.SimulacaoResponseDTO;
import com.futmaneger.application.usecase.calendario.AvancarTemporadaUseCase;
import com.futmaneger.application.usecase.calendario.ConsultarCalendarioUseCase;
import com.futmaneger.application.usecase.calendario.SimularPartidaDoUsuarioUseCase;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/temporadas/{temporadaId}")
@SecurityRequirement(name = "bearerAuth")
public class CalendarioController {

    private final ConsultarCalendarioUseCase consultarCalendario;
    private final AvancarTemporadaUseCase avancarTemporada;
    private final SimularPartidaDoUsuarioUseCase simularPartidaDoUsuario;

    public CalendarioController(ConsultarCalendarioUseCase consultarCalendario,
                                AvancarTemporadaUseCase avancarTemporada,
                                SimularPartidaDoUsuarioUseCase simularPartidaDoUsuario) {
        this.consultarCalendario = consultarCalendario;
        this.avancarTemporada = avancarTemporada;
        this.simularPartidaDoUsuario = simularPartidaDoUsuario;
    }

    @GetMapping("/calendario")
    public ResponseEntity<List<EventoCalendarioResponseDTO>> listar(
            @PathVariable Long temporadaId,
            @RequestParam(required = false) Long clubeId) {
        return ResponseEntity.ok(consultarCalendario.listar(temporadaId, clubeId));
    }

    @GetMapping("/calendario/proximo")
    public ResponseEntity<EventoCalendarioResponseDTO> proximo(@PathVariable Long temporadaId) {
        EventoCalendarioResponseDTO proximo = consultarCalendario.proximo(temporadaId);
        return proximo == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(proximo);
    }

    @PostMapping("/avancar")
    public ResponseEntity<AvancoTemporadaResponseDTO> avancar(@PathVariable Long temporadaId) {
        return ResponseEntity.ok(avancarTemporada.executar(temporadaId));
    }

    @PostMapping("/calendario/eventos/{eventoId}/simular")
    public ResponseEntity<SimulacaoResponseDTO> simularPartidaDoUsuario(
            @PathVariable Long temporadaId,
            @PathVariable Long eventoId) {
        return ResponseEntity.ok(simularPartidaDoUsuario.executar(temporadaId, eventoId));
    }
}
