package com.futmaneger.application.usecase.tecnico;

import com.futmaneger.application.dto.TecnicoDto;
import com.futmaneger.application.exception.DadosInvalidosException;
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
        validarCampos(dto);

        Tecnico tecnico = new Tecnico(dto.nome(), dto.email(), dto.senha());
        return tecnicoRepository.salvar(tecnico);
    }

    private void validarCampos(TecnicoDto dto) {
        StringBuilder erros = new StringBuilder();

        if (dto.nome() == null || dto.nome().trim().isEmpty()) {
            erros.append("Nome é obrigatório. ");
        }

        if (dto.email() == null || dto.email().trim().isEmpty()) {
            erros.append("Email é obrigatório. ");
        }

        if (dto.senha() == null || dto.senha().trim().isEmpty()) {
            erros.append("Senha é obrigatória. ");
        }

        if (!erros.isEmpty()) {
            throw new DadosInvalidosException(erros.toString().trim());
        }
    }
}

