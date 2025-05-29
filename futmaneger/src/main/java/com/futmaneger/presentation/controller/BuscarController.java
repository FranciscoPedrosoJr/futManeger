package com.futmaneger.presentation.controller;

import com.futmaneger.application.usecase.clube.BuscarClubesUseCase;
import com.futmaneger.application.usecase.tecnico.BuscarTecnicoUseCase;
import com.futmaneger.domain.entity.Clube;
import com.futmaneger.infrastructure.persistence.entity.TecnicoEntity;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/buscar")
public class BuscarController {
    private final BuscarClubesUseCase buscarClubesUseCase;

    private final BuscarTecnicoUseCase buscarTecnicoUseCase;

    public BuscarController(BuscarClubesUseCase buscarClubesUseCase, BuscarTecnicoUseCase buscarTecnicoUseCase) {
        this.buscarClubesUseCase = buscarClubesUseCase;
        this.buscarTecnicoUseCase = buscarTecnicoUseCase;
    }

    @GetMapping("/clubes")
    public ResponseEntity<List<Clube>> listarClubes() {
        return ResponseEntity.ok(buscarClubesUseCase.buscarTodos());
    }

    @GetMapping("/tecnicos")
    public ResponseEntity<List<TecnicoEntity>> listarTecnicos() {
        return ResponseEntity.ok(buscarTecnicoUseCase.buscarTodos());
    }
}
