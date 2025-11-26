package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.AtualizarJogadorRequestDTO;
import com.futmaneger.application.dto.JogadorResponseDTO;
import com.futmaneger.application.usecase.jogador.AtualizarJogadorUseCase;
import com.futmaneger.domain.entity.Jogador;
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

    public EditarController(AtualizarJogadorUseCase atualizarJogadorUseCase) {
        this.atualizarJogadorUseCase = atualizarJogadorUseCase;
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
}
