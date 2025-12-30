package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.MensagemResponseDTO;
import com.futmaneger.application.dto.TecnicoClubeResponseDTO;
import com.futmaneger.application.dto.VinculoTecnicoClubeRequestDTO;
import com.futmaneger.application.usecase.vinculo.BuscarClubeDoTecnicoUseCase;
import com.futmaneger.application.usecase.vinculo.DesvincularJogadorDoClubeUseCase;
import com.futmaneger.application.usecase.vinculo.DesvincularTecnicoDoClubeUseCase;
import com.futmaneger.application.usecase.vinculo.VincularTecnicoAoClubeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vinculo")
public class VinculoController {
    private final VincularTecnicoAoClubeUseCase useCase;
    private final DesvincularTecnicoDoClubeUseCase desvincularTecnicoDoClubeUseCase;
    private final DesvincularJogadorDoClubeUseCase desvincularJogadorDoClubeUseCase;
    private final BuscarClubeDoTecnicoUseCase buscarClubeDoTecnicoUseCase;

    @PostMapping("/tecnico-clube")
    public ResponseEntity<MensagemResponseDTO> vincularTecnicoAoClube(@RequestBody VinculoTecnicoClubeRequestDTO request) {
        useCase.vincular(request);
        return ResponseEntity.ok(new MensagemResponseDTO("Técnico vinculado ao clube com sucesso!"));
    }

    @DeleteMapping("clube/{clubeId}/tecnico/{tecnicoId}/desvincular")
    public ResponseEntity<MensagemResponseDTO> desvincularTecnico(
            @PathVariable Long clubeId,
            @PathVariable Long tecnicoId
    ) {
        desvincularTecnicoDoClubeUseCase.desvincular(clubeId, tecnicoId);
        return ResponseEntity.ok(new MensagemResponseDTO("Técnico desvinculado com sucesso."));
    }

    @DeleteMapping("/clube/{clubeId}/jogador/{jogadorId}/desvincular")
    public ResponseEntity<MensagemResponseDTO> desvincularJogador(
            @PathVariable Long clubeId,
            @PathVariable Long jogadorId) {
        desvincularJogadorDoClubeUseCase.desvincular(clubeId, jogadorId);
        return ResponseEntity.ok(new MensagemResponseDTO("Jogador desvinculado do clube com sucesso!"));
    }

    @GetMapping("/tecnico/{tecnicoId}/clube")
    public ResponseEntity<TecnicoClubeResponseDTO> buscarClube(
            @PathVariable Long tecnicoId
    ) {
        TecnicoClubeResponseDTO response = buscarClubeDoTecnicoUseCase.executar(tecnicoId);
        return ResponseEntity.ok(response);
    }

}
