package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.MensagemResponseDTO;
import com.futmaneger.application.usecase.clube.ExcluirClubeUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/excluir")
public class ExcluirController {
    private final ExcluirClubeUseCase excluirClubeUseCase;

    public ExcluirController(ExcluirClubeUseCase excluirClubeUseCase) {
        this.excluirClubeUseCase = excluirClubeUseCase;
    }

    @DeleteMapping("/clubes/{id}")
    public ResponseEntity<MensagemResponseDTO> excluirClube(@PathVariable Long id) {
        return excluirClubeUseCase.excluirPorId(id)
                .map(nome -> ResponseEntity.ok(new MensagemResponseDTO("O clube " + nome + " foi excluído com sucesso!")))
                .orElse(ResponseEntity.notFound().build());
    }
}
