package com.futmaneger.application.usecase.calendario;

import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaMataMataEntity;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import com.futmaneger.infrastructure.persistence.jpa.PartidaMataMataRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Distribui as datas das partidas de todas as edições de uma temporada em um
 * planejamento global, respeitando o descanso mínimo entre jogos de um mesmo
 * clube e evitando que um clube tenha dois compromissos incompatíveis.
 *
 * <p>Agenda as partidas já geradas (pontos corridos e a primeira fase de
 * mata-mata). As fases seguintes de mata-mata, que dependem dos classificados,
 * são agendadas dinamicamente quando geradas.</p>
 */
@Service
public class AgendamentoCalendarioService {

    private static final int DIAS_DESCANSO = 2;

    private final PartidaRepository partidaRepository;
    private final PartidaMataMataRepository partidaMataMataRepository;

    public AgendamentoCalendarioService(PartidaRepository partidaRepository,
                                        PartidaMataMataRepository partidaMataMataRepository) {
        this.partidaRepository = partidaRepository;
        this.partidaMataMataRepository = partidaMataMataRepository;
    }

    @Transactional
    public void gerarCalendario(TemporadaEntity temporada) {
        LocalDate dataBase = temporada.getDataInicio();
        LocalDate limite = temporada.getDataEncerramento();
        if (dataBase == null) {
            throw new DadosInvalidosException("A temporada não possui data de início para gerar o calendário");
        }

        Map<Long, LocalDate> ultimaDataPorClube = new HashMap<>();

        List<CompromissoAgendavel> compromissos = coletarCompromissos(temporada);

        for (CompromissoAgendavel compromisso : compromissos) {
            LocalDate data = encontrarDataDisponivel(compromisso, ultimaDataPorClube, dataBase, limite);
            compromisso.definirData(data);
            registrarOcupacao(compromisso, data, ultimaDataPorClube);
            compromisso.salvar();
        }
    }

    private List<CompromissoAgendavel> coletarCompromissos(TemporadaEntity temporada) {
        List<CompromissoAgendavel> compromissos = new ArrayList<>();

        for (EdicaoCampeonatoEntity edicao : temporada.getEdicoes()) {
            for (PartidaEntity partida : partidaRepository.findByEdicao(edicao)) {
                compromissos.add(new CompromissoPontosCorridos(partida, partidaRepository));
            }
            for (PartidaMataMataEntity partida : partidaMataMataRepository.findByEdicaoId(edicao.getId())) {
                compromissos.add(new CompromissoMataMata(partida, partidaMataMataRepository));
            }
        }

        compromissos.sort(Comparator.comparingInt(CompromissoAgendavel::ordem));
        return compromissos;
    }

    private LocalDate encontrarDataDisponivel(CompromissoAgendavel compromisso,
                                              Map<Long, LocalDate> ultimaDataPorClube,
                                              LocalDate dataBase, LocalDate limite) {
        Long mandante = compromisso.mandanteId();
        Long visitante = compromisso.visitanteId();

        LocalDate candidata = dataBase;
        candidata = maisTarde(candidata, proximaDataLivre(ultimaDataPorClube.get(mandante)));
        candidata = maisTarde(candidata, proximaDataLivre(ultimaDataPorClube.get(visitante)));

        if (limite != null && candidata.isAfter(limite)) {
            throw new DadosInvalidosException(
                    "Não foi possível agendar todas as partidas dentro do período da temporada. "
                            + "Partida sem data: clube " + mandante + " x clube " + visitante);
        }

        return candidata;
    }

    private LocalDate proximaDataLivre(LocalDate ultimaData) {
        if (ultimaData == null) {
            return null;
        }
        return ultimaData.plusDays(DIAS_DESCANSO + 1);
    }

    private LocalDate maisTarde(LocalDate a, LocalDate b) {
        if (b == null) {
            return a;
        }
        return b.isAfter(a) ? b : a;
    }

    private void registrarOcupacao(CompromissoAgendavel compromisso, LocalDate data,
                                   Map<Long, LocalDate> ultimaDataPorClube) {
        ultimaDataPorClube.put(compromisso.mandanteId(), data);
        ultimaDataPorClube.put(compromisso.visitanteId(), data);
    }

    private interface CompromissoAgendavel {
        Long mandanteId();

        Long visitanteId();

        int ordem();

        void definirData(LocalDate data);

        void salvar();
    }

    private static final class CompromissoPontosCorridos implements CompromissoAgendavel {
        private final PartidaEntity partida;
        private final PartidaRepository repository;

        CompromissoPontosCorridos(PartidaEntity partida, PartidaRepository repository) {
            this.partida = partida;
            this.repository = repository;
        }

        @Override
        public Long mandanteId() {
            return partida.getMandante().getId();
        }

        @Override
        public Long visitanteId() {
            return partida.getVisitante().getId();
        }

        @Override
        public int ordem() {
            return partida.getRodada();
        }

        @Override
        public void definirData(LocalDate data) {
            partida.setDataProgramada(data);
        }

        @Override
        public void salvar() {
            repository.save(partida);
        }
    }

    private static final class CompromissoMataMata implements CompromissoAgendavel {
        private final PartidaMataMataEntity partida;
        private final PartidaMataMataRepository repository;

        CompromissoMataMata(PartidaMataMataEntity partida, PartidaMataMataRepository repository) {
            this.partida = partida;
            this.repository = repository;
        }

        @Override
        public Long mandanteId() {
            return partida.getMandante().getId();
        }

        @Override
        public Long visitanteId() {
            return partida.getVisitante().getId();
        }

        @Override
        public int ordem() {
            if (partida.getRodada() != null) {
                return partida.getRodada().getNumero();
            }
            return partida.getOrdemFase() != null ? partida.getOrdemFase() : 0;
        }

        @Override
        public void definirData(LocalDate data) {
            partida.setDataProgramada(data);
        }

        @Override
        public void salvar() {
            repository.save(partida);
        }
    }
}
