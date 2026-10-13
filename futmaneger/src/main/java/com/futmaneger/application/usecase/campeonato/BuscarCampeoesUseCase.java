package com.futmaneger.application.usecase.campeonato;

import com.futmaneger.application.dto.CampeaoResponseDTO;
import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.EdicaoCampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.TabelaCampeonatoRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Busca os campeões por edição. Como o mesmo campeonato pode ser disputado em
 * várias temporadas, o campeão é determinado por edição (disputa concreta).
 */
@Service
public class BuscarCampeoesUseCase {
    private final ClubeJpaRepository clubeRepository;
    private final TabelaCampeonatoRepository tabelaCampeonatoRepository;
    private final EdicaoCampeonatoRepository edicaoRepository;

    public BuscarCampeoesUseCase(ClubeJpaRepository clubeRepository, TabelaCampeonatoRepository tabelaCampeonatoRepository,
                                 EdicaoCampeonatoRepository edicaoRepository) {
        this.clubeRepository = clubeRepository;
        this.tabelaCampeonatoRepository = tabelaCampeonatoRepository;
        this.edicaoRepository = edicaoRepository;
    }

    public List<CampeaoResponseDTO> buscarCampeoes(Long edicaoId) {

        if (edicaoId != null) {

            EdicaoCampeonatoEntity edicao = edicaoRepository.findById(edicaoId)
                    .orElseThrow(() -> new NaoEncontradoException("Edição de campeonato não encontrada"));

            if (edicao.getEmAndamento()) {
                throw new DadosInvalidosException("Esta edição ainda não finalizou.");
            }

            var tabela = tabelaCampeonatoRepository.findByEdicaoIdOrderByPontosDesc(edicaoId);

            if (tabela.isEmpty()) {
                throw new DadosInvalidosException("Esta edição não possui tabela registrada.");
            }

            var campeao = tabela.get(0);

            var clube = clubeRepository.findById(campeao.getClube().getId())
                    .orElseThrow(() -> new NaoEncontradoException("Clube não encontrado"));

            return List.of(new CampeaoResponseDTO(
                    edicao.getId(),
                    edicao.getCampeonato().getNome(),
                    clube.getId(),
                    clube.getNome(),
                    campeao.getPontos(),
                    campeao.getVitorias(),
                    campeao.getEmpates(),
                    campeao.getDerrotas(),
                    campeao.getSaldoGols()
            ));
        }

        var edicoes = edicaoRepository.findAll();

        List<CampeaoResponseDTO> response = new ArrayList<>();

        for (var edicao : edicoes) {

            if (edicao.getEmAndamento()) {
                continue;
            }

            var tabela = tabelaCampeonatoRepository.findByEdicaoIdOrderByPontosDesc(edicao.getId());

            if (tabela.isEmpty()) continue;

            var campeao = tabela.get(0);

            var clube = clubeRepository.findById(campeao.getClube().getId())
                    .orElseThrow(() -> new NaoEncontradoException("Clube não encontrado"));

            response.add(new CampeaoResponseDTO(
                    edicao.getId(),
                    edicao.getCampeonato().getNome(),
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
            throw new NaoEncontradoException("Nenhuma edição de campeonato finalizada encontrada.");
        }

        return response;
    }
}
