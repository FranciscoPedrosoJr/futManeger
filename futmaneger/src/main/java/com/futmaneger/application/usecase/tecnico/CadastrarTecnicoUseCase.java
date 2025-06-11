package com.futmaneger.application.usecase.tecnico;

import com.futmaneger.application.dto.TecnicoDto;
import com.futmaneger.domain.entity.Tecnico;
import com.futmaneger.domain.repository.TecnicoRepository;
import org.springframework.stereotype.Service;

@Service
public class CadastrarTecnicoUseCase {

    private final TecnicoRepository tecnicoRepository;

    public CadastrarTecnicoUseCase(TecnicoRepository tecnicoRepository) {
        this.tecnicoRepository = tecnicoRepository;
    }

    public Tecnico executar(TecnicoDto dto) {
        Tecnico tecnico = new Tecnico(dto.nome(), dto.email(), dto.senha());
        return tecnicoRepository.salvar(tecnico);
    }
}
