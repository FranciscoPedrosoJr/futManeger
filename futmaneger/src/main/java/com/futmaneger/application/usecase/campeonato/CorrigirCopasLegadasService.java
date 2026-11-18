package com.futmaneger.application.usecase.campeonato;

import com.futmaneger.application.usecase.rodadas.GerarMataMataConfiguravelUseCase;
import com.futmaneger.infrastructure.persistence.entity.AbrangenciaCampeonato;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import com.futmaneger.infrastructure.persistence.jpa.CampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.ClubeParticipanteRepository;
import com.futmaneger.infrastructure.persistence.jpa.EdicaoCampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.EventoCalendarioRepository;
import com.futmaneger.infrastructure.persistence.jpa.GrupoRepository;
import com.futmaneger.infrastructure.persistence.jpa.ParticipanteFaseRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaFaseDeGruposRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaMataMataRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaRepository;
import com.futmaneger.infrastructure.persistence.jpa.RodadaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TabelaCampeonatoRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CorrigirCopasLegadasService {

    private static final List<AbrangenciaCampeonato> ABRANGENCIAS_COPA = List.of(
            AbrangenciaCampeonato.COPA_NACIONAL,
            AbrangenciaCampeonato.COPA_INTERNACIONAL,
            AbrangenciaCampeonato.COPA_DO_MUNDO);

    private final CampeonatoRepository campeonatoRepository;
    private final EdicaoCampeonatoRepository edicaoRepository;
    private final ClubeParticipanteRepository participanteRepository;
    private final EventoCalendarioRepository eventoRepository;
    private final PartidaRepository partidaRepository;
    private final PartidaMataMataRepository partidaMataMataRepository;
    private final PartidaFaseDeGruposRepository partidaFaseDeGruposRepository;
    private final GrupoRepository grupoRepository;
    private final TabelaCampeonatoRepository tabelaRepository;
    private final RodadaRepository rodadaRepository;
    private final ParticipanteFaseRepository participanteFaseRepository;
    private final ConfiguradorFasesCopaService configuradorFases;
    private final AtribuirFasesDeEntradaService atribuirFases;
    private final GerarMataMataConfiguravelUseCase gerarMataMata;
    private final EntityManager entityManager;

    public CorrigirCopasLegadasService(CampeonatoRepository campeonatoRepository,
                                       EdicaoCampeonatoRepository edicaoRepository,
                                       ClubeParticipanteRepository participanteRepository,
                                       EventoCalendarioRepository eventoRepository,
                                       PartidaRepository partidaRepository,
                                       PartidaMataMataRepository partidaMataMataRepository,
                                       PartidaFaseDeGruposRepository partidaFaseDeGruposRepository,
                                       GrupoRepository grupoRepository,
                                       TabelaCampeonatoRepository tabelaRepository,
                                       RodadaRepository rodadaRepository,
                                       ParticipanteFaseRepository participanteFaseRepository,
                                       ConfiguradorFasesCopaService configuradorFases,
                                       AtribuirFasesDeEntradaService atribuirFases,
                                       GerarMataMataConfiguravelUseCase gerarMataMata,
                                       EntityManager entityManager) {
        this.campeonatoRepository = campeonatoRepository;
        this.edicaoRepository = edicaoRepository;
        this.participanteRepository = participanteRepository;
        this.eventoRepository = eventoRepository;
        this.partidaRepository = partidaRepository;
        this.partidaMataMataRepository = partidaMataMataRepository;
        this.partidaFaseDeGruposRepository = partidaFaseDeGruposRepository;
        this.grupoRepository = grupoRepository;
        this.tabelaRepository = tabelaRepository;
        this.rodadaRepository = rodadaRepository;
        this.participanteFaseRepository = participanteFaseRepository;
        this.configuradorFases = configuradorFases;
        this.atribuirFases = atribuirFases;
        this.gerarMataMata = gerarMataMata;
        this.entityManager = entityManager;
    }

    /**
     * Corrige somente copas antigas ainda não iniciadas e sem resultados. Dados
     * históricos ou calendários já oficializados nunca são alterados.
     */
    @Transactional
    public int executar() {
        List<CampeonatoEntity> copas = campeonatoRepository.findByTipoAndAbrangenciaIn(
                CampeonatoEntity.TipoCampeonato.PONTOS_CORRIDOS, ABRANGENCIAS_COPA);
        int corrigidas = 0;
        for (CampeonatoEntity copa : copas) {
            List<EdicaoCampeonatoEntity> edicoes = edicaoRepository.findByCampeonato(copa);
            if (!podeCorrigir(edicoes)) {
                continue;
            }

            copa.setTipo(CampeonatoEntity.TipoCampeonato.MATA_MATA);
            campeonatoRepository.save(copa);
            configuradorFases.configurarSeNecessario(copa);

            for (EdicaoCampeonatoEntity edicao : edicoes) {
                corrigirEdicao(edicao);
            }
            corrigidas++;
        }
        return corrigidas;
    }

    private boolean podeCorrigir(List<EdicaoCampeonatoEntity> edicoes) {
        for (EdicaoCampeonatoEntity edicao : edicoes) {
            if (edicao.getTemporada().getEstado() != TemporadaEntity.EstadoTemporada.AGUARDANDO_INICIO
                    || eventoRepository.countByEdicao(edicao) > 0
                    || partidaRepository.existsByEdicaoAndFinalizadaTrue(edicao)
                    || partidaMataMataRepository.existsByEdicaoAndFinalizadaTrue(edicao)
                    || partidaFaseDeGruposRepository.findByEdicao(edicao).stream().anyMatch(p -> p.isFinalizada())) {
                return false;
            }
        }
        return true;
    }

    private void corrigirEdicao(EdicaoCampeonatoEntity edicao) {
        boolean tinhaRodadas = !rodadaRepository.findByEdicao(edicao).isEmpty();

        partidaFaseDeGruposRepository.deleteByEdicao(edicao);
        tabelaRepository.deleteByEdicao(edicao);
        partidaMataMataRepository.deleteByEdicao(edicao);
        partidaRepository.deleteByEdicao(edicao);
        entityManager.flush();
        grupoRepository.deleteByEdicao(edicao);
        rodadaRepository.deleteByEdicao(edicao);
        participanteFaseRepository.deleteByEdicao(edicao);
        entityManager.flush();
        entityManager.clear();

        EdicaoCampeonatoEntity atualizada = edicaoRepository.findById(edicao.getId()).orElseThrow();
        atualizada.setMataMataIniciado(false);
        atualizada.setFaseAtualMataMata(null);
        edicaoRepository.save(atualizada);

        List<ClubeEntity> clubes = participanteRepository.findByEdicao(atualizada).stream()
                .map(participante -> participante.getClube())
                .toList();
        atribuirFases.atribuir(atualizada, clubes);
        if (tinhaRodadas) {
            gerarMataMata.gerarPrimeiraFase(atualizada);
        }
    }
}
