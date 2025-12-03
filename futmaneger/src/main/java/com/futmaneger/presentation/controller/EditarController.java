package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.AtualizarClubeRequestDTO;
import com.futmaneger.application.dto.AtualizarClubeResponseDTO;
import com.futmaneger.application.dto.AtualizarJogadorRequestDTO;
import com.futmaneger.application.dto.AtualizarSaldoRequestDTO;
import com.futmaneger.application.dto.AtualizarTecnicoRequestDTO;
import com.futmaneger.application.dto.AtualizarTecnicoResponseDTO;
import com.futmaneger.application.dto.JogadorResponseDTO;
import com.futmaneger.application.usecase.clube.AtualizarClubeUseCase;
import com.futmaneger.application.usecase.clube.AtualizarSaldoClubeUseCase;
import com.futmaneger.application.usecase.jogador.AtualizarJogadorUseCase;
import com.futmaneger.application.usecase.tecnico.AtualizarTecnicoUseCase;
import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.TecnicoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/editar")
@RequiredArgsConstructor
public class EditarController {
    private final AtualizarJogadorUseCase atualizarJogadorUseCase;
    private final AtualizarClubeUseCase atualizarClubeUseCase;
    private final AtualizarTecnicoUseCase atualizarTecnicoUseCase;
    private final AtualizarSaldoClubeUseCase atualizarSaldoClubeUseCase;

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

    @PutMapping("/tecnico/{id}")
    public ResponseEntity<AtualizarTecnicoResponseDTO> atualizarJogador(
            @PathVariable Long id,
            @RequestBody AtualizarTecnicoRequestDTO request
    ) {
        TecnicoEntity j = atualizarTecnicoUseCase.atualizar(id, request);

        return ResponseEntity.ok(new AtualizarTecnicoResponseDTO(
                j.getId(),
                j.getNome(),
                j.getEmail(),
                j.getSenha()
        ));
    }

    @PatchMapping("/clube-saldo")
    public ResponseEntity<?> atualizarSaldo(@RequestBody AtualizarSaldoRequestDTO request) {
        var clubeAtualizado = atualizarSaldoClubeUseCase.executar(request);
        return ResponseEntity.ok(clubeAtualizado);
    }
}
