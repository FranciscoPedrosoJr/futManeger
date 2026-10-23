package com.futmaneger.application.usecase.campeonato;

import com.futmaneger.application.dto.TabelaCampeonatoResponseDTO;
import com.futmaneger.application.dto.TabelaItemResponseDTO;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.EdicaoCampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.TabelaCampeonatoRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BuscarTabelaCampeonatoUseCase {
    private final TabelaCampeonatoRepository tabelaRepository;
    private final ClubeJpaRepository clubeRepository;
    private final EdicaoCampeonatoRepository edicaoRepository;

    public BuscarTabelaCampeonatoUseCase(
            TabelaCampeonatoRepository tabelaRepository,
            ClubeJpaRepository clubeRepository,
            EdicaoCampeonatoRepository edicaoRepository) {
        this.tabelaRepository = tabelaRepository;
        this.clubeRepository = clubeRepository;
        this.edicaoRepository = edicaoRepository;
    }

    public List<TabelaCampeonatoResponseDTO> executar(Long edicaoId) {

        EdicaoCampeonatoEntity edicao = edicaoRepository.findById(edicaoId)
                .orElseThrow(() -> new NaoEncontradoException("Edição de campeonato não encontrada"));

        var tabelaOrdenada = tabelaRepository.findByEdicaoIdOrderByPontosDesc(edicaoId);

        List<TabelaItemResponseDTO> itens = new ArrayList<>();

        int posicao = 1;

        for (var linha : tabelaOrdenada) {
            var clube = clubeRepository.findById(linha.getClube().getId())
                    .orElseThrow(() -> new NaoEncontradoException("Clube não encontrado"));

            itens.add(new TabelaItemResponseDTO(
                    posicao++,
                    clube.getId(),
                    clube.getNome(),
                    linha.getPontos(),
                    linha.getVitorias(),
                    linha.getEmpates(),
                    linha.getDerrotas(),
                    linha.getGolsPro(),
                    linha.getGolsContra(),
                    linha.getSaldoGols()
            ));
        }

        return Collections.singletonList(new TabelaCampeonatoResponseDTO(
                edicao.getId(),
                edicao.getCampeonato().getNome(),
                itens
        ));
    }
}
