package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.MensagemResponseDTO;
import com.futmaneger.application.usecase.clube.ExcluirClubeUseCase;
import com.futmaneger.application.usecase.jogador.ExcluirJogadorUseCase;
import com.futmaneger.application.usecase.tecnico.ExcluirTecnicoUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/excluir")
public class ExcluirController {
    private final ExcluirClubeUseCase excluirClubeUseCase;
    private final ExcluirTecnicoUseCase excluirTecnicoUseCase;
    private final ExcluirJogadorUseCase excluirJogador;

    public ExcluirController(ExcluirClubeUseCase excluirClubeUseCase, ExcluirTecnicoUseCase excluirTecnicoUseCase,
                             ExcluirJogadorUseCase excluirJogador) {
        this.excluirClubeUseCase = excluirClubeUseCase;
        this.excluirTecnicoUseCase = excluirTecnicoUseCase;
        this.excluirJogador = excluirJogador;
    }

    @DeleteMapping("/clubes/{id}")
    public ResponseEntity<MensagemResponseDTO> excluirClube(@PathVariable Long id) {
        return excluirClubeUseCase.excluirPorId(id)
                .map(nome -> ResponseEntity.ok(new MensagemResponseDTO("O clube " + nome + " foi excluído com sucesso!")))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/tecnicos/{id}")
    public ResponseEntity<MensagemResponseDTO> excluirTecnico(@PathVariable Long id) {
        return excluirTecnicoUseCase.excluirPorId(id)
                .map(nome -> ResponseEntity.ok(new MensagemResponseDTO("O tecnico " + nome + " foi excluído com sucesso!")))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/jogador/{jogadorId}")
    public ResponseEntity<MensagemResponseDTO> excluir(@PathVariable Long jogadorId) {
        excluirJogador.excluirJogador(jogadorId);
        return ResponseEntity.ok(new MensagemResponseDTO("Jogador excluído!"));
    }
}
