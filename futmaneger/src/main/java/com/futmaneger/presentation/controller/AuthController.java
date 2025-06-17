package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.TecnicoDto;
import com.futmaneger.application.usecase.auth.AutenticarTecnicoUseCase;
import com.futmaneger.application.usecase.tecnico.CadastrarTecnicoUseCase;
import com.futmaneger.domain.entity.Tecnico;
import com.futmaneger.infrastructure.security.JwtService;
import com.futmaneger.presentation.request.CadastrarTecnicoRequest;
import com.futmaneger.presentation.request.LoginRequest;
import com.futmaneger.presentation.response.LoginResponse;
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
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        Tecnico tecnico = autenticarTecnicoUseCase.autenticar(request.email(), request.senha());

        String token = JwtService.gerarTokenTecnico(tecnico);

        LoginResponse response = new LoginResponse(
                tecnico.getId(),
                tecnico.getNome(),
                tecnico.getEmail(),
                token
        );

        return ResponseEntity.ok(response);
    }
}
