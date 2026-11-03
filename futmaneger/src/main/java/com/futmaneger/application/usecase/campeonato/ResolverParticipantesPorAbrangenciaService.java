package com.futmaneger.application.usecase.campeonato;

import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.infrastructure.persistence.entity.AbrangenciaCampeonato;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.Continente;
import com.futmaneger.infrastructure.persistence.entity.PaisEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.PaisRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Resolve a lista de clubes elegíveis para um campeonato a partir da sua
 * {@link AbrangenciaCampeonato}. Centraliza as regras de filtro (país, estado,
 * continente, seleção) para que o cadastro automático de participantes seja
 * consistente em todos os pontos do sistema.
 */
@Service
public class ResolverParticipantesPorAbrangenciaService {

    private final ClubeJpaRepository clubeRepository;
    private final PaisRepository paisRepository;

    public ResolverParticipantesPorAbrangenciaService(ClubeJpaRepository clubeRepository,
                                                      PaisRepository paisRepository) {
        this.clubeRepository = clubeRepository;
        this.paisRepository = paisRepository;
    }

    /**
     * Retorna todos os clubes elegíveis para o campeonato de acordo com a sua
     * abrangência. A limitação pela quantidade de clubes do campeonato é feita
     * pelo chamador.
     */
    public List<ClubeEntity> resolver(CampeonatoEntity campeonato) {
        AbrangenciaCampeonato abrangencia = campeonato.getAbrangencia();
        if (abrangencia == null) {
            throw new DadosInvalidosException(
                    "Campeonato não possui abrangência definida para resolver os participantes.");
        }

        if (abrangencia.isApenasSelecoes()) {
            return clubeRepository.findByEhSelecao(true);
        }

        return switch (abrangencia) {
            case ESTADUAL -> resolverEstadual(campeonato);
            case NACIONAL, COPA_NACIONAL -> resolverNacional(campeonato);
            case CONTINENTAL -> resolverContinental(campeonato);
            case COPA_INTERNACIONAL -> clubeRepository.findByEhSelecao(false);
            case MUNDIAL -> List.of();
            default -> throw new DadosInvalidosException(
                    "Abrangência não suportada para resolução automática: " + abrangencia);
        };
    }

    private List<ClubeEntity> resolverEstadual(CampeonatoEntity campeonato) {
        exigir(campeonato.getPais(), "país");
        exigir(campeonato.getEstado(), "estado");
        return clubeRepository.findByPaisAndEstado(campeonato.getPais(), campeonato.getEstado())
                .stream()
                .filter(clube -> !clube.isEhSelecao())
                .toList();
    }

    private List<ClubeEntity> resolverNacional(CampeonatoEntity campeonato) {
        exigir(campeonato.getPais(), "país");
        return clubeRepository.findByPais(campeonato.getPais())
                .stream()
                .filter(clube -> !clube.isEhSelecao())
                .toList();
    }

    private List<ClubeEntity> resolverContinental(CampeonatoEntity campeonato) {
        exigir(campeonato.getPais(), "país");

        Continente continente = paisRepository.findByNomeIgnoreCase(campeonato.getPais())
                .map(PaisEntity::getContinente)
                .orElseThrow(() -> new DadosInvalidosException(
                        "País '" + campeonato.getPais() + "' não está cadastrado na tabela de países/continentes."));

        List<String> paisesDoContinente = paisRepository.findAll().stream()
                .filter(pais -> pais.getContinente() == continente)
                .map(PaisEntity::getNome)
                .toList();

        if (paisesDoContinente.isEmpty()) {
            throw new DadosInvalidosException(
                    "Nenhum país cadastrado para o continente " + continente + ".");
        }

        return clubeRepository.findByPaisInAndEhSelecao(paisesDoContinente, false);
    }

    private void exigir(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DadosInvalidosException(
                    "Campeonato não possui " + campo + " definido para resolver os participantes.");
        }
    }
}
