package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.TecnicoDto;
import com.futmaneger.application.usecase.auth.AutenticarTecnicoUseCase;
import com.futmaneger.application.usecase.tecnico.CadastrarTecnicoUseCase;
import com.futmaneger.domain.entity.Tecnico;
import com.futmaneger.presentation.request.CadastrarTecnicoRequest;
import com.futmaneger.presentation.request.LoginRequest;
import com.futmaneger.presentation.response.TecnicoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CadastrarTecnicoUseCase cadastrarTecnicoUseCase;
    private final AutenticarTecnicoUseCase autenticarTecnicoUseCase;

    public AuthController(
            CadastrarTecnicoUseCase cadastrarTecnicoUseCase,
            AutenticarTecnicoUseCase autenticarTecnicoUseCase
    ) {
        this.cadastrarTecnicoUseCase = cadastrarTecnicoUseCase;
        this.autenticarTecnicoUseCase = autenticarTecnicoUseCase;
    }

    @PostMapping("/registrar")
    public ResponseEntity<TecnicoResponse> registrar(@RequestBody CadastrarTecnicoRequest request) {
        TecnicoDto dto = new TecnicoDto(request.getNome(), request.getEmail(), request.getSenha());
        Tecnico tecnico = cadastrarTecnicoUseCase.executar(dto);
        return ResponseEntity.ok(new TecnicoResponse(tecnico));
    }

    @PostMapping("/login")
    public ResponseEntity<TecnicoResponse> login(@RequestBody LoginRequest request) {
        return autenticarTecnicoUseCase.autenticar(request.email(), request.senha())
                .map(TecnicoResponse::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }
}
