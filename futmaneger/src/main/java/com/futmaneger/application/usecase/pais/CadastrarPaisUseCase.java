package com.futmaneger.application.usecase.pais;

import com.futmaneger.application.dto.CadastrarPaisRequestDTO;
import com.futmaneger.application.dto.PaisResponseDTO;
import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.infrastructure.persistence.entity.PaisEntity;
import com.futmaneger.infrastructure.persistence.jpa.PaisRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CadastrarPaisUseCase {

    private final PaisRepository paisRepository;

    public CadastrarPaisUseCase(PaisRepository paisRepository) {
        this.paisRepository = paisRepository;
    }

    public PaisResponseDTO cadastrar(CadastrarPaisRequestDTO request) {
        if (request.nome() == null || request.nome().isBlank()) {
            throw new DadosInvalidosException("O nome do país é obrigatório");
        }
        if (request.continente() == null) {
            throw new DadosInvalidosException("O continente é obrigatório");
        }
        if (paisRepository.existsByNomeIgnoreCase(request.nome())) {
            throw new DadosInvalidosException("Já existe um país cadastrado com o nome: " + request.nome());
        }

        PaisEntity pais = paisRepository.save(new PaisEntity(request.nome(), request.continente()));
        return toResponse(pais);
    }

    public List<PaisResponseDTO> listar() {
        return paisRepository.findAll().stream().map(this::toResponse).toList();
    }

    private PaisResponseDTO toResponse(PaisEntity pais) {
        return new PaisResponseDTO(pais.getId(), pais.getNome(), pais.getContinente());
    }
}
