package com.futmaneger.application.usecase.tecnico;

import com.futmaneger.application.dto.AtualizarTecnicoRequestDTO;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.TecnicoEntity;
import com.futmaneger.infrastructure.persistence.jpa.TecnicoJpaRepository;
import org.springframework.stereotype.Service;

@Service
public class AtualizarTecnicoUseCase {
    private  final TecnicoJpaRepository tecnicoJpaRepository;

    public AtualizarTecnicoUseCase(TecnicoJpaRepository tecnicoJpaRepository) {
        this.tecnicoJpaRepository = tecnicoJpaRepository;
    }

    public TecnicoEntity atualizar (Long tecnicoId, AtualizarTecnicoRequestDTO request){
        TecnicoEntity tecnico = tecnicoJpaRepository.findById(tecnicoId)
                .orElseThrow(() -> new NaoEncontradoException("Tecnico não encontrado"));

        if (request.nome() != null) {
            tecnico.setNome(request.nome());
        }

        if (request.email() != null) {
            tecnico.setEmail(request.email());
        }

        if (request.senha() != null) {
            tecnico.setSenha(request.senha());
        }

        return tecnicoJpaRepository.save(tecnico);

    }
}
