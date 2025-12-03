package com.futmaneger.application.usecase.campeonato;

import com.futmaneger.application.dto.CampeaoResponseDTO;
import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.jpa.CampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TabelaCampeonatoRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BuscarCampeoesUseCase {
    private final ClubeJpaRepository clubeRepository;
    private final TabelaCampeonatoRepository tabelaCampeonatoRepository;
    private final CampeonatoRepository campeonatoRepository;

    public BuscarCampeoesUseCase(ClubeJpaRepository clubeRepository, TabelaCampeonatoRepository tabelaCampeonatoRepository,
                                 CampeonatoRepository campeonatoRepository) {
        this.clubeRepository = clubeRepository;
        this.tabelaCampeonatoRepository = tabelaCampeonatoRepository;
        this.campeonatoRepository = campeonatoRepository;
    }

    public List<CampeaoResponseDTO> buscarCampeoes(Long campeonatoId) {

        if (campeonatoId != null) {

            var campeonato = campeonatoRepository.findById(campeonatoId)
                    .orElseThrow(() -> new NaoEncontradoException("Campeonato não encontrado"));

            if (Boolean.TRUE.equals(campeonato.getEmAndamento())) {
                throw new DadosInvalidosException("Este campeonato ainda não finalizou.");
            }

            var tabela = tabelaCampeonatoRepository.findByCampeonatoIdOrderByPontosDesc(campeonatoId);

            if (tabela.isEmpty()) {
                throw new DadosInvalidosException("Este campeonato não possui tabela registrada.");
            }

            var campeao = tabela.get(0);

            var clube = clubeRepository.findById(campeao.getClube().getId())
                    .orElseThrow(() -> new NaoEncontradoException("Clube não encontrado"));

            return List.of(new CampeaoResponseDTO(
                    campeonato.getId(),
                    campeonato.getNome(),
                    clube.getId(),
                    clube.getNome(),
                    campeao.getPontos(),
                    campeao.getVitorias(),
                    campeao.getEmpates(),
                    campeao.getDerrotas(),
                    campeao.getSaldoGols()
            ));
        }

        var campeonatos = campeonatoRepository.findAll();

        List<CampeaoResponseDTO> response = new ArrayList<>();

        for (var camp : campeonatos) {

            if (Boolean.TRUE.equals(camp.getEmAndamento())) {
                continue;
            }

            var tabela = tabelaCampeonatoRepository.findByCampeonatoIdOrderByPontosDesc(camp.getId());

            if (tabela.isEmpty()) continue;

            var campeao = tabela.get(0);

            var clube = clubeRepository.findById(campeao.getClube().getId())
                    .orElseThrow(() -> new NaoEncontradoException("Clube não encontrado"));

            response.add(new CampeaoResponseDTO(
                    camp.getId(),
                    camp.getNome(),
                    clube.getId(),
                    clube.getNome(),
                    campeao.getPontos(),
                    campeao.getVitorias(),
                    campeao.getEmpates(),
                    campeao.getDerrotas(),
                    campeao.getSaldoGols()
            ));
        }

        if (response.isEmpty()) {
            throw new NaoEncontradoException("Nenhum campeonato finalizado encontrado.");
        }

        return response;
    }
}
