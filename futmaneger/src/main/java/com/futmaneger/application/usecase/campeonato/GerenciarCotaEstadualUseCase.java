package com.futmaneger.application.usecase.campeonato;

import com.futmaneger.application.dto.CadastrarCotaEstadualRequestDTO;
import com.futmaneger.application.dto.CotaEstadualResponseDTO;
import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.CotaEstadualEntity;
import com.futmaneger.infrastructure.persistence.jpa.CampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.CotaEstadualRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GerenciarCotaEstadualUseCase {

    private final CotaEstadualRepository cotaRepository;
    private final CampeonatoRepository campeonatoRepository;

    public GerenciarCotaEstadualUseCase(CotaEstadualRepository cotaRepository,
                                        CampeonatoRepository campeonatoRepository) {
        this.cotaRepository = cotaRepository;
        this.campeonatoRepository = campeonatoRepository;
    }

    @Transactional
    public CotaEstadualResponseDTO cadastrar(CadastrarCotaEstadualRequestDTO request) {
        if (request.campeonatoId() == null) {
            throw new DadosInvalidosException("O campeonato é obrigatório");
        }
        if (request.estado() == null || request.estado().isBlank()) {
            throw new DadosInvalidosException("O estado é obrigatório");
        }
        if (request.quantidade() <= 0) {
            throw new DadosInvalidosException("A quantidade de vagas deve ser maior que zero");
        }

        CampeonatoEntity campeonato = campeonatoRepository.findById(request.campeonatoId())
                .orElseThrow(() -> new NaoEncontradoException("Campeonato não encontrado"));

        if (cotaRepository.existsByCampeonatoAndEstado(campeonato, request.estado())) {
            throw new DadosInvalidosException(
                    "Já existe uma cota cadastrada para o estado " + request.estado()
                            + " neste campeonato");
        }

        CotaEstadualEntity cota = cotaRepository.save(
                new CotaEstadualEntity(campeonato, request.estado(), request.quantidade(),
                        request.faseDeEntradaOrdem()));

        return toResponse(cota);
    }

    public List<CotaEstadualResponseDTO> listarPorCampeonato(Long campeonatoId) {
        CampeonatoEntity campeonato = campeonatoRepository.findById(campeonatoId)
                .orElseThrow(() -> new NaoEncontradoException("Campeonato não encontrado"));
        return cotaRepository.findByCampeonato(campeonato).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void remover(Long id) {
        if (!cotaRepository.existsById(id)) {
            throw new NaoEncontradoException("Cota estadual não encontrada");
        }
        cotaRepository.deleteById(id);
    }

    private CotaEstadualResponseDTO toResponse(CotaEstadualEntity cota) {
        return new CotaEstadualResponseDTO(
                cota.getId(),
                cota.getCampeonato().getId(),
                cota.getCampeonato().getNome(),
                cota.getEstado(),
                cota.getQuantidade(),
                cota.getFaseDeEntradaOrdem()
        );
    }
}
