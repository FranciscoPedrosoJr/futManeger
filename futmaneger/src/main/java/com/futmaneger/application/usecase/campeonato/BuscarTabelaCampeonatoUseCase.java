package com.futmaneger.application.usecase.campeonato;

import com.futmaneger.application.dto.TabelaCampeonatoResponseDTO;
import com.futmaneger.application.dto.TabelaItemResponseDTO;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.entity.TabelaCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.jpa.CampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TabelaCampeonatoRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BuscarTabelaCampeonatoUseCase {
    private final TabelaCampeonatoRepository tabelaRepository;
    private final ClubeJpaRepository clubeRepository;
    private final CampeonatoRepository campeonatoRepository;

    public BuscarTabelaCampeonatoUseCase(
            TabelaCampeonatoRepository tabelaRepository,
            ClubeJpaRepository clubeRepository,
            CampeonatoRepository campeonatoRepository) {
        this.tabelaRepository = tabelaRepository;
        this.clubeRepository = clubeRepository;
        this.campeonatoRepository = campeonatoRepository;
    }

    public List<TabelaCampeonatoResponseDTO> executar(Long campeonatoId) {

        var campeonato = campeonatoRepository.findById(campeonatoId)
                .orElseThrow(() -> new NaoEncontradoException("Campeonato não encontrado"));

        var tabelaOrdenada = tabelaRepository.findByCampeonatoIdOrderByPontosDesc(campeonatoId);

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
                campeonato.getId(),
                campeonato.getNome(),
                itens
        ));
    }
}
