package com.futmaneger.application.usecase.rodadas;

import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.FaseCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.FormatoFase;
import com.futmaneger.infrastructure.persistence.entity.ParticipanteFaseEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaMataMataEntity;
import com.futmaneger.infrastructure.persistence.entity.RodadaEntity;
import com.futmaneger.infrastructure.persistence.jpa.FaseCampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.ParticipanteFaseRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaMataMataRepository;
import com.futmaneger.infrastructure.persistence.jpa.RodadaRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GerarMataMataConfiguravelUseCase {

    private final FaseCampeonatoRepository faseRepository;
    private final ParticipanteFaseRepository participanteFaseRepository;
    private final PartidaMataMataRepository partidaRepository;
    private final RodadaRepository rodadaRepository;

    public GerarMataMataConfiguravelUseCase(FaseCampeonatoRepository faseRepository,
                                            ParticipanteFaseRepository participanteFaseRepository,
                                            PartidaMataMataRepository partidaRepository,
                                            RodadaRepository rodadaRepository) {
        this.faseRepository = faseRepository;
        this.participanteFaseRepository = participanteFaseRepository;
        this.partidaRepository = partidaRepository;
        this.rodadaRepository = rodadaRepository;
    }

    public boolean possuiFasesConfiguradas(CampeonatoEntity campeonato) {
        return !faseRepository.findByCampeonatoOrderByOrdem(campeonato).isEmpty();
    }

    public boolean existeProximaFase(EdicaoCampeonatoEntity edicao) {
        List<FaseCampeonatoEntity> fases = faseRepository.findByCampeonatoOrderByOrdem(edicao.getCampeonato());
        FaseCampeonatoEntity faseAtual = faseAtualComPartidas(edicao, fases);
        if (faseAtual == null) {
            return !fases.isEmpty();
        }
        return fases.stream().anyMatch(f -> f.getOrdem() > faseAtual.getOrdem());
    }

    public ClubeEntity campeao(EdicaoCampeonatoEntity edicao) {
        List<FaseCampeonatoEntity> fases = faseRepository.findByCampeonatoOrderByOrdem(edicao.getCampeonato());
        FaseCampeonatoEntity ultima = faseAtualComPartidas(edicao, fases);
        if (ultima == null) {
            return null;
        }
        List<ClubeEntity> vencedores = vencedoresDaFase(edicao, ultima);
        return vencedores.isEmpty() ? null : vencedores.get(0);
    }

    private FaseCampeonatoEntity faseAtualComPartidas(EdicaoCampeonatoEntity edicao, List<FaseCampeonatoEntity> fases) {
        FaseCampeonatoEntity faseAtual = null;
        for (FaseCampeonatoEntity fase : fases) {
            if (partidaRepository.existsByEdicaoAndOrdemFase(edicao, fase.getOrdem())) {
                faseAtual = fase;
            }
        }
        return faseAtual;
    }

    @Transactional
    public void gerarPrimeiraFase(EdicaoCampeonatoEntity edicao) {
        List<FaseCampeonatoEntity> fases = faseRepository.findByCampeonatoOrderByOrdem(edicao.getCampeonato());
        if (fases.isEmpty()) {
            throw new DadosInvalidosException("O campeonato não possui fases configuradas");
        }

        FaseCampeonatoEntity primeira = fases.get(0);
        List<ClubeEntity> entrantes = entrantesDaFase(edicao, primeira.getOrdem());
        if (entrantes.size() < 2) {
            throw new DadosInvalidosException(
                    "Não há participantes suficientes para iniciar a fase " + primeira.getNome());
        }

        gerarPartidasDaFase(edicao, primeira, entrantes);
    }

    @Transactional
    public void gerarProximaFase(EdicaoCampeonatoEntity edicao) {
        List<FaseCampeonatoEntity> fases = faseRepository.findByCampeonatoOrderByOrdem(edicao.getCampeonato());
        if (fases.isEmpty()) {
            throw new DadosInvalidosException("O campeonato não possui fases configuradas");
        }

        FaseCampeonatoEntity faseAtual = null;
        for (FaseCampeonatoEntity fase : fases) {
            if (partidaRepository.existsByEdicaoAndOrdemFase(edicao, fase.getOrdem())) {
                faseAtual = fase;
            }
        }

        if (faseAtual == null) {
            gerarPrimeiraFase(edicao);
            return;
        }

        FaseCampeonatoEntity proxima = null;
        for (FaseCampeonatoEntity fase : fases) {
            if (fase.getOrdem() > faseAtual.getOrdem()) {
                proxima = fase;
                break;
            }
        }

        if (proxima == null) {
            return;
        }

        List<ClubeEntity> vencedores = vencedoresDaFase(edicao, faseAtual);
        List<ClubeEntity> entrantes = entrantesDaFase(edicao, proxima.getOrdem());

        List<ClubeEntity> participantes = new ArrayList<>();
        participantes.addAll(vencedores);
        participantes.addAll(entrantes);

        if (participantes.size() < 2) {
            return;
        }

        gerarPartidasDaFase(edicao, proxima, participantes);
    }

    private List<ClubeEntity> entrantesDaFase(EdicaoCampeonatoEntity edicao, int ordem) {
        return participanteFaseRepository.findByEdicaoAndOrdemEntrada(edicao, ordem).stream()
                .map(ParticipanteFaseEntity::getClube)
                .toList();
    }

    private void gerarPartidasDaFase(EdicaoCampeonatoEntity edicao, FaseCampeonatoEntity fase,
                                     List<ClubeEntity> clubes) {
        int ultimaRodada = rodadaRepository.findMaxNumeroByEdicao(edicao).orElse(0);

        RodadaEntity rodada = new RodadaEntity();
        rodada.setNumero(ultimaRodada + 1);
        rodada.setEdicao(edicao);
        rodada.setFinalizada(false);
        rodada = rodadaRepository.save(rodada);

        List<PartidaMataMataEntity> partidas = new ArrayList<>();
        int confronto = 1;

        for (int i = 0; i + 1 < clubes.size(); i += 2) {
            ClubeEntity casa = clubes.get(i);
            ClubeEntity fora = clubes.get(i + 1);

            partidas.add(novaPartida(edicao, fase, rodada, casa, fora, confronto, false));

            if (fase.getFormato() == FormatoFase.IDA_E_VOLTA) {
                partidas.add(novaPartida(edicao, fase, rodada, fora, casa, confronto, true));
            }

            confronto++;
        }

        partidaRepository.saveAll(partidas);
    }

    private PartidaMataMataEntity novaPartida(EdicaoCampeonatoEntity edicao, FaseCampeonatoEntity fase,
                                              RodadaEntity rodada, ClubeEntity casa, ClubeEntity fora,
                                              int confronto, boolean jogoDeVolta) {
        PartidaMataMataEntity partida = new PartidaMataMataEntity();
        partida.setClubeMandante(casa);
        partida.setClubeVisitante(fora);
        partida.setEdicao(edicao);
        partida.setOrdemFase(fase.getOrdem());
        partida.setConfronto(confronto);
        partida.setJogoDeVolta(jogoDeVolta);
        partida.setFinalizada(false);
        partida.setGolsMandante(0);
        partida.setGolsVisitante(0);
        partida.setRodada(rodada);
        return partida;
    }

    private List<ClubeEntity> vencedoresDaFase(EdicaoCampeonatoEntity edicao, FaseCampeonatoEntity fase) {
        List<PartidaMataMataEntity> partidas =
                partidaRepository.findByEdicaoAndOrdemFase(edicao, fase.getOrdem());

        if (partidas.isEmpty()) {
            return List.of();
        }

        int maxConfronto = partidas.stream()
                .map(p -> p.getConfronto() == null ? 0 : p.getConfronto())
                .max(Integer::compareTo)
                .orElse(0);

        List<ClubeEntity> vencedores = new ArrayList<>();

        for (int c = 1; c <= maxConfronto; c++) {
            final int confronto = c;
            List<PartidaMataMataEntity> doConfronto = partidas.stream()
                    .filter(p -> p.getConfronto() != null && p.getConfronto() == confronto)
                    .toList();

            if (doConfronto.isEmpty()) {
                continue;
            }

            vencedores.add(definirVencedorConfronto(doConfronto));
        }

        return vencedores;
    }

    private ClubeEntity definirVencedorConfronto(List<PartidaMataMataEntity> partidas) {
        PartidaMataMataEntity jogoIda = partidas.stream()
                .filter(p -> !p.isJogoDeVolta())
                .findFirst()
                .orElse(partidas.get(0));

        ClubeEntity casa = jogoIda.getClubeA();
        ClubeEntity fora = jogoIda.getClubeB();

        int golsCasa = 0;
        int golsFora = 0;
        boolean algumPenalti = false;
        int penaltisCasa = 0;
        int penaltisFora = 0;

        for (PartidaMataMataEntity partida : partidas) {
            if (!partida.isJogoDeVolta()) {
                golsCasa += partida.getGolsClubeA();
                golsFora += partida.getGolsClubeB();
            } else {
                golsCasa += partida.getGolsClubeB();
                golsFora += partida.getGolsClubeA();
            }

            if (partida.isFoiParaPenaltis()) {
                algumPenalti = true;
                if (!partida.isJogoDeVolta()) {
                    penaltisCasa += partida.getGolsPenaltisA();
                    penaltisFora += partida.getGolsPenaltisB();
                } else {
                    penaltisCasa += partida.getGolsPenaltisB();
                    penaltisFora += partida.getGolsPenaltisA();
                }
            }
        }

        if (golsCasa > golsFora) {
            return casa;
        }
        if (golsFora > golsCasa) {
            return fora;
        }

        if (algumPenalti) {
            if (penaltisCasa > penaltisFora) {
                return casa;
            }
            if (penaltisFora > penaltisCasa) {
                return fora;
            }
        }

        return ThreadLocalRandom.current().nextBoolean() ? casa : fora;
    }
}
