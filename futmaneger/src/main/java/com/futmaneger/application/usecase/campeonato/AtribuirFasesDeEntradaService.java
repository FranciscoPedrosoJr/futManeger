package com.futmaneger.application.usecase.campeonato;

import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.FaseCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.ParticipanteFaseEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.FaseCampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.ParticipanteFaseRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AtribuirFasesDeEntradaService {

    private final FaseCampeonatoRepository faseRepository;
    private final ParticipanteFaseRepository participanteFaseRepository;
    private final ClubeJpaRepository clubeRepository;

    public AtribuirFasesDeEntradaService(FaseCampeonatoRepository faseRepository,
                                         ParticipanteFaseRepository participanteFaseRepository,
                                         ClubeJpaRepository clubeRepository) {
        this.faseRepository = faseRepository;
        this.participanteFaseRepository = participanteFaseRepository;
        this.clubeRepository = clubeRepository;
    }

    public void atribuir(EdicaoCampeonatoEntity edicao, List<ClubeEntity> clubes) {
        CampeonatoEntity campeonato = edicao.getCampeonato();
        List<FaseCampeonatoEntity> fases = faseRepository.findByCampeonatoOrderByOrdem(campeonato);
        if (fases.isEmpty()) {
            throw new DadosInvalidosException("O campeonato não possui fases configuradas");
        }

        List<Integer> capacidades = capacidadesPorFase(fases.size(), clubes.size());

        List<ClubeEntity> ordenados = new ArrayList<>(clubes);
        ordenados.sort(Comparator.comparingDouble(this::forca).reversed()
                .thenComparing(ClubeEntity::getId));

        int indice = 0;
        for (int i = fases.size() - 1; i >= 0; i--) {
            int ordem = fases.get(i).getOrdem();
            int capacidade = capacidades.get(i);
            for (int j = 0; j < capacidade && indice < ordenados.size(); j++) {
                participanteFaseRepository.save(
                        new ParticipanteFaseEntity(edicao, ordenados.get(indice++), ordem));
            }
        }

        while (indice < ordenados.size()) {
            int ordemPrimeira = fases.get(0).getOrdem();
            participanteFaseRepository.save(
                    new ParticipanteFaseEntity(edicao, ordenados.get(indice++), ordemPrimeira));
        }
    }

    private List<Integer> capacidadesPorFase(int numeroFases, int totalClubes) {
        if (numeroFases <= 0 || numeroFases >= 31) {
            throw new DadosInvalidosException("Quantidade de fases inválida para o mata-mata");
        }
        int capacidade = 1 << numeroFases;
        int classificadosDiretos = capacidade / 2;
        if (totalClubes <= classificadosDiretos || totalClubes > capacidade) {
            throw new DadosInvalidosException(
                    "A quantidade de fases não comporta " + totalClubes
                            + " clubes. Configure exatamente " + quantidadeFases(totalClubes) + " fase(s)");
        }

        List<Integer> novasEntradas = new ArrayList<>();
        for (int i = 0; i < numeroFases; i++) {
            novasEntradas.add(0);
        }
        if (totalClubes == capacidade) {
            novasEntradas.set(0, totalClubes);
            return novasEntradas;
        }

        int participantesPreliminar = 2 * (totalClubes - classificadosDiretos);
        int entramNaFaseSeguinte = capacidade - totalClubes;
        novasEntradas.set(0, participantesPreliminar);
        novasEntradas.set(1, entramNaFaseSeguinte);
        return novasEntradas;
    }

    private int quantidadeFases(int totalClubes) {
        int fases = 0;
        int capacidade = 1;
        while (capacidade < totalClubes) {
            capacidade <<= 1;
            fases++;
        }
        return fases;
    }

    private double forca(ClubeEntity clube) {
        Double media = clubeRepository.calcularMediaForca(clube.getId());
        return media == null ? 0.0 : media;
    }
}
