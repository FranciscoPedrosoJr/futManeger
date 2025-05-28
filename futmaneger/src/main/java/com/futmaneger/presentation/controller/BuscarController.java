package com.futmaneger.presentation.controller;

import com.futmaneger.application.usecase.clube.BuscarClubesUseCase;
import com.futmaneger.domain.entity.Clube;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/buscar")
public class BuscarController {
    private final BuscarClubesUseCase buscarClubesUseCase;

    public BuscarController(BuscarClubesUseCase buscarClubesUseCase) {
        this.buscarClubesUseCase = buscarClubesUseCase;
    }

    @GetMapping("/clubes")
    public ResponseEntity<List<Clube>> listarClubes() {
        return ResponseEntity.ok(buscarClubesUseCase.buscarTodos());
    }
}
