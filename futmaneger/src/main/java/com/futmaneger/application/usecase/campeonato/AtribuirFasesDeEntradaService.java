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
        int[] participantesNaFase = new int[numeroFases];
        participantesNaFase[numeroFases - 1] = 2;
        for (int i = numeroFases - 2; i >= 0; i--) {
            participantesNaFase[i] = participantesNaFase[i + 1] * 2;
        }

        List<Integer> novasEntradas = new ArrayList<>();
        for (int i = 0; i < numeroFases; i++) {
            if (i == 0) {
                novasEntradas.add(participantesNaFase[i]);
            } else {
                int vencedoresDaAnterior = participantesNaFase[i - 1] / 2;
                novasEntradas.add(participantesNaFase[i] - vencedoresDaAnterior);
            }
        }

        int soma = novasEntradas.stream().mapToInt(Integer::intValue).sum();
        if (soma < totalClubes) {
            novasEntradas.set(0, novasEntradas.get(0) + (totalClubes - soma));
        }

        return novasEntradas;
    }

    private double forca(ClubeEntity clube) {
        Double media = clubeRepository.calcularMediaForca(clube.getId());
        return media == null ? 0.0 : media;
    }
}
