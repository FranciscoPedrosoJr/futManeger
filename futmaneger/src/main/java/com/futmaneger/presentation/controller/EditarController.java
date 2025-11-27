package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.AtualizarClubeRequestDTO;
import com.futmaneger.application.dto.AtualizarClubeResponseDTO;
import com.futmaneger.application.dto.AtualizarJogadorRequestDTO;
import com.futmaneger.application.dto.JogadorResponseDTO;
import com.futmaneger.application.usecase.clube.AtualizarClubeUseCase;
import com.futmaneger.application.usecase.jogador.AtualizarJogadorUseCase;
import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/editar")
public class EditarController {
    private final AtualizarJogadorUseCase atualizarJogadorUseCase;
    private final AtualizarClubeUseCase atualizarClubeUseCase;

    public EditarController(AtualizarJogadorUseCase atualizarJogadorUseCase, AtualizarClubeUseCase atualizarClubeUseCase) {
        this.atualizarJogadorUseCase = atualizarJogadorUseCase;
        this.atualizarClubeUseCase = atualizarClubeUseCase;
    }

    @PutMapping("/jogador/{id}")
    public ResponseEntity<JogadorResponseDTO> atualizarJogador(
            @PathVariable Long id,
            @RequestBody AtualizarJogadorRequestDTO request
    ) {
        Jogador j = atualizarJogadorUseCase.atualizar(id, request);

        return ResponseEntity.ok(new JogadorResponseDTO(
                j.getId(),
                j.getNome(),
                j.getPosicao(),
                j.getForca(),
                j.isDiferenciado(),
                j.isIdentificacaoComClube(),
                j.getClubeId().toString(),
                j.getValorMercado()
        ));
    }

    @PutMapping("/clube/{id}")
    public ResponseEntity<AtualizarClubeResponseDTO> atualizarJogador(
            @PathVariable Long id,
            @RequestBody AtualizarClubeRequestDTO request
    ) {
        ClubeEntity j = atualizarClubeUseCase.atualizar(id, request);

        return ResponseEntity.ok(new AtualizarClubeResponseDTO(
                j.getId(),
                j.getNome(),
                j.getEstado(),
                j.getPais(),
                j.getSaldo(),
                j.getTecnico()
        ));
    }
}
