package com.futmaneger.application.usecase.campeonato;

import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.CotaEstadualEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.CotaEstadualRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ResolverParticipantesPorCotasService {

    private final CotaEstadualRepository cotaRepository;
    private final ClubeJpaRepository clubeRepository;

    public ResolverParticipantesPorCotasService(CotaEstadualRepository cotaRepository,
                                                 ClubeJpaRepository clubeRepository) {
        this.cotaRepository = cotaRepository;
        this.clubeRepository = clubeRepository;
    }

    public boolean possuiCotas(CampeonatoEntity campeonato) {
        return !cotaRepository.findByCampeonato(campeonato).isEmpty();
    }

    public List<ClubeEntity> resolver(CampeonatoEntity campeonato, Set<Long> jaClassificadosIds) {
        List<CotaEstadualEntity> cotas = cotaRepository.findByCampeonato(campeonato);
        if (cotas.isEmpty()) {
            return List.of();
        }

        if (campeonato.getPais() == null || campeonato.getPais().isBlank()) {
            throw new DadosInvalidosException(
                    "Campeonato com cotas estaduais precisa ter o país definido.");
        }

        int totalVagas = cotas.stream().mapToInt(CotaEstadualEntity::getQuantidade).sum();
        LinkedHashSet<ClubeEntity> selecionados = new LinkedHashSet<>();

        for (CotaEstadualEntity cota : cotas) {
            List<ClubeEntity> candidatos = clubeRepository
                    .findByPaisAndEstado(campeonato.getPais(), cota.getEstado())
                    .stream()
                    .filter(clube -> !clube.isEhSelecao())
                    .filter(clube -> !jaClassificadosIds.contains(clube.getId()))
                    .filter(clube -> !selecionados.contains(clube))
                    .sorted(Comparator.comparingDouble(this::forca).reversed()
                            .thenComparing(ClubeEntity::getId))
                    .toList();

            int preenchidas = 0;
            for (ClubeEntity clube : candidatos) {
                if (preenchidas >= cota.getQuantidade()) {
                    break;
                }
                selecionados.add(clube);
                preenchidas++;
            }
        }

        if (selecionados.size() < totalVagas) {
            List<ClubeEntity> complemento = clubeRepository.findByPais(campeonato.getPais()).stream()
                    .filter(clube -> !clube.isEhSelecao())
                    .filter(clube -> !jaClassificadosIds.contains(clube.getId()))
                    .filter(clube -> !selecionados.contains(clube))
                    .sorted(Comparator.comparingDouble(this::forca).reversed()
                            .thenComparing(ClubeEntity::getId))
                    .toList();

            for (ClubeEntity clube : complemento) {
                if (selecionados.size() >= totalVagas) {
                    break;
                }
                selecionados.add(clube);
            }
        }

        return new ArrayList<>(selecionados);
    }

    private double forca(ClubeEntity clube) {
        Double media = clubeRepository.calcularMediaForca(clube.getId());
        return media == null ? 0.0 : media;
    }
}
