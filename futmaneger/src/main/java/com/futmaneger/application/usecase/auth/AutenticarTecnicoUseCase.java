package com.futmaneger.application.usecase.auth;

import com.futmaneger.application.exception.AutenticacaoException;
import com.futmaneger.domain.entity.Tecnico;
import com.futmaneger.domain.repository.TecnicoRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AutenticarTecnicoUseCase {

    private final TecnicoRepository tecnicoRepository;

    public AutenticarTecnicoUseCase(TecnicoRepository tecnicoRepository) {
        this.tecnicoRepository = tecnicoRepository;
    }

    public Tecnico autenticar(String email, String senha) {
        return tecnicoRepository.buscarPorEmail(email)
                .filter(tecnico -> tecnico.getSenha().equals(senha))
                .orElseThrow(() -> new AutenticacaoException("Não foi possível realizar o login. Tente novamente."));
    }
}
