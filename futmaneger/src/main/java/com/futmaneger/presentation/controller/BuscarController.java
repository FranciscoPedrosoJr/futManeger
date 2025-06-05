package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.JogadorFiltroDTO;
import com.futmaneger.application.dto.JogadorResponseDTO;
import com.futmaneger.application.usecase.clube.BuscarClubesUseCase;
import com.futmaneger.application.usecase.jogador.BuscarJogadoresUseCase;
import com.futmaneger.application.usecase.tecnico.BuscarTecnicoUseCase;
import com.futmaneger.domain.entity.Clube;
import com.futmaneger.infrastructure.persistence.entity.TecnicoEntity;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/buscar")
public class BuscarController {
    private final BuscarClubesUseCase buscarClubesUseCase;

    private final BuscarTecnicoUseCase buscarTecnicoUseCase;

    private final BuscarJogadoresUseCase buscarJogadoresUseCase;

    public BuscarController(BuscarClubesUseCase buscarClubesUseCase, BuscarTecnicoUseCase buscarTecnicoUseCase,
                            BuscarJogadoresUseCase buscarJogadoresUseCase) {
        this.buscarClubesUseCase = buscarClubesUseCase;
        this.buscarTecnicoUseCase = buscarTecnicoUseCase;
        this.buscarJogadoresUseCase = buscarJogadoresUseCase;
    }

    @GetMapping("/clubes")
    public ResponseEntity<List<Clube>> listarClubes() {
        return ResponseEntity.ok(buscarClubesUseCase.buscarTodos());
    }

    @GetMapping("/tecnicos")
    public ResponseEntity<List<TecnicoEntity>> listarTecnicos() {
        return ResponseEntity.ok(buscarTecnicoUseCase.buscarTodos());
    }

    @PostMapping("/jogadores")
    public ResponseEntity<List<JogadorResponseDTO>> buscar(@RequestBody JogadorFiltroDTO filtro) {
        List<JogadorResponseDTO> jogadores = buscarJogadoresUseCase.buscar(filtro);
        return ResponseEntity.ok(jogadores);
    }
}
