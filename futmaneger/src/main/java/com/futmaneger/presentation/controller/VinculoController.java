package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.MensagemResponseDTO;
import com.futmaneger.application.dto.VinculoTecnicoClubeRequestDTO;
import com.futmaneger.application.usecase.vinculo.DesvincularTecnicoDoClubeUseCase;
import com.futmaneger.application.usecase.vinculo.VincularTecnicoAoClubeUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vinculo")
public class VinculoController {
    private final VincularTecnicoAoClubeUseCase useCase;
    private final DesvincularTecnicoDoClubeUseCase desvincularTecnicoDoClubeUseCase;

    public VinculoController(VincularTecnicoAoClubeUseCase useCase, DesvincularTecnicoDoClubeUseCase desvincularTecnicoDoClubeUseCase) {
        this.useCase = useCase;
        this.desvincularTecnicoDoClubeUseCase = desvincularTecnicoDoClubeUseCase;
    }

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
}
