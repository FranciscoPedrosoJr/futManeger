package com.futmaneger.application.usecase.rodadas;

import com.futmaneger.application.dto.GerarRodadasResponseDTO;
import com.futmaneger.application.dto.GrupoResponseDTO;
import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeParticipanteEntity;
import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.GrupoEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaFaseDeGruposEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaMataMataEntity;
import com.futmaneger.infrastructure.persistence.entity.RodadaEntity;
import com.futmaneger.infrastructure.persistence.entity.TabelaCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeParticipanteRepository;
import com.futmaneger.infrastructure.persistence.jpa.EdicaoCampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.GrupoRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaFaseDeGruposRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaMataMataRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaRepository;
import com.futmaneger.infrastructure.persistence.jpa.RodadaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TabelaCampeonatoRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GerarRodadasMataMataUseCase {

    private final ClubeParticipanteRepository clubeParticipanteRepository;
    private final GrupoRepository gruposRepository;
    private final PartidaRepository partidaRepository;
    private final PartidaFaseDeGruposRepository partidaFaseDeGruposRepository;
    private final PartidaMataMataRepository partidaMataMataRepository;
    private final TabelaCampeonatoRepository tabelaCampeonatoRepository;
    private final RodadaRepository rodadaRepository;
    private final EdicaoCampeonatoRepository edicaoRepository;

    public GerarRodadasMataMataUseCase(ClubeParticipanteRepository clubeParticipanteRepository,
                                       GrupoRepository gruposRepository,
                                       PartidaRepository partidaRepository,
                                       PartidaFaseDeGruposRepository partidaFaseDeGruposRepository,
                                       PartidaMataMataRepository partidaMataMataRepository,
                                       TabelaCampeonatoRepository tabelaCampeonatoRepository,
                                       RodadaRepository rodadaRepository,
                                       EdicaoCampeonatoRepository edicaoRepository) {

        this.clubeParticipanteRepository = clubeParticipanteRepository;
        this.gruposRepository = gruposRepository;
        this.partidaRepository = partidaRepository;
        this.partidaFaseDeGruposRepository = partidaFaseDeGruposRepository;
        this.partidaMataMataRepository = partidaMataMataRepository;
        this.tabelaCampeonatoRepository = tabelaCampeonatoRepository;
        this.rodadaRepository = rodadaRepository;
        this.edicaoRepository = edicaoRepository;
    }

    public GerarRodadasResponseDTO gerarRodadas(EdicaoCampeonatoEntity edicao) {
        if (!edicao.getCampeonato().getTipo().equals(com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity.TipoCampeonato.MATA_MATA)) {
            return null;
        }

        if (gruposRepository.existsByEdicao(edicao)) {
            if (todasPartidasFaseDeGruposFinalizadas(edicao) &&
                    !partidaMataMataRepository.existsByEdicao(edicao)) {
                gerarMataMata(edicao);
            }
        }
        return gerarFaseDeGrupos(edicao);

    }

    private GerarRodadasResponseDTO gerarFaseDeGrupos(EdicaoCampeonatoEntity edicao) {
        List<ClubeParticipanteEntity> clubes = clubeParticipanteRepository.findByEdicao(edicao);

        if (clubes.size() < 4) {
            throw new IllegalStateException("Campeonato do tipo mata-mata deve ter pelo menos 4 clubes.");
        }

        List<List<ClubeParticipanteEntity>> grupos = dividirClubesEmGrupos(clubes);
        List<RodadaEntity> rodadas = new ArrayList<>();
        List<PartidaEntity> todasPartidas = new ArrayList<>();
        List<GrupoResponseDTO> gruposDTO = new ArrayList<>();

        int grupoIndex = 1;

        for (List<ClubeParticipanteEntity> grupo : grupos) {
            GrupoEntity grupoEntity = new GrupoEntity();
            grupoEntity.setNome("Grupo " + grupoIndex++);
            grupoEntity.setEdicao(edicao);

            Set<ClubeEntity> clubesDoGrupo = grupo.stream()
                    .map(ClubeParticipanteEntity::getClube)
                    .collect(Collectors.toSet());

            grupoEntity.setClubes(clubesDoGrupo);
            grupoEntity = gruposRepository.save(grupoEntity);

            List<PartidaEntity> partidas = gerarPartidasFaseDeGrupos(grupo, edicao, grupoEntity);

            for (PartidaEntity partida : partidas) {
                RodadaEntity rodada = rodadaRepository.findByNumeroAndEdicao(partida.getRodada(), edicao)
                        .orElseGet(() -> {
                            RodadaEntity novaRodada = new RodadaEntity();
                            novaRodada.setNumero(partida.getRodada());
                            novaRodada.setEdicao(edicao);
                            return rodadaRepository.save(novaRodada);
                        });

                if (!rodadas.contains(rodada)) {
                    rodadas.add(rodada);
                }

                partidaRepository.save(partida);
                todasPartidas.add(partida);
            }

            List<String> nomesClubes;
            try {
                nomesClubes = grupo.stream()
                        .filter(c -> c != null && c.getClube() != null)
                        .map(c -> c.getClube().getNome())
                        .toList();
            } catch (RuntimeException e) {
                throw e;
            }

            gruposDTO.add(new GrupoResponseDTO(grupoEntity.getNome(), nomesClubes));
        }

        return new GerarRodadasResponseDTO(edicao.getId(), rodadas.size(), todasPartidas.size(), gruposDTO);
    }

    private boolean todasPartidasFaseDeGruposFinalizadas(EdicaoCampeonatoEntity edicao) {
        List<PartidaFaseDeGruposEntity> partidas = partidaFaseDeGruposRepository.findByEdicao(edicao);
        return partidas.stream().allMatch(PartidaFaseDeGruposEntity::isFinalizada);
    }

    public void gerarMataMata(EdicaoCampeonatoEntity edicao) {
        List<GrupoEntity> grupos = gruposRepository.findByEdicao(edicao);
        if (grupos == null || grupos.isEmpty()) {
            throw new NaoEncontradoException("Nenhum grupo encontrado para a edição id=" + edicao.getId());
        }

        List<ClubeParticipanteEntity> classificados = new ArrayList<>();

        for (GrupoEntity grupo : grupos) {
            List<TabelaCampeonatoEntity> tabelaGrupo = tabelaCampeonatoRepository.findByGrupoOrderByPontosDesc(grupo.getId());

            if (tabelaGrupo == null || tabelaGrupo.size() < 2) {
                throw new NaoEncontradoException("Não há registros suficientes na tabela para o grupo: " + grupo.getNome() +
                        " (esperado >=2, encontrado=" + (tabelaGrupo == null ? 0 : tabelaGrupo.size()) + ")");
            }

            ClubeEntity clubePrimeiro = tabelaGrupo.get(0).getClube();
            ClubeEntity clubeSegundo = tabelaGrupo.get(1).getClube();

            ClubeParticipanteEntity primeiro = clubeParticipanteRepository
                    .findByClubeAndEdicao(clubePrimeiro, edicao)
                    .orElseThrow(() -> new NaoEncontradoException("Participante não encontrado para clube: " + clubePrimeiro.getNome()));

            ClubeParticipanteEntity segundo = clubeParticipanteRepository
                    .findByClubeAndEdicao(clubeSegundo, edicao)
                    .orElseThrow(() -> new NaoEncontradoException("Participante não encontrado para clube: " + clubeSegundo.getNome()));

            classificados.add(primeiro);
            classificados.add(segundo);
        }

        if (classificados.isEmpty()) {
            throw new DadosInvalidosException("Nenhum clube classificado encontrado para gerar mata-mata.");
        }

        PartidaMataMataEntity.FaseMataMata faseInicial = edicao.getFaseAtualMataMata();
        if (faseInicial == null) {
            faseInicial = definirFaseInicial(classificados.size());
        } else {
            faseInicial = determinarProximaFase(faseInicial);
        }

        gerarPartidasMataMataRecursivamente(classificados, edicao, faseInicial);
    }

    private void gerarPartidasMataMataRecursivamente(
            List<ClubeParticipanteEntity> clubes,
            EdicaoCampeonatoEntity edicao,
            PartidaMataMataEntity.FaseMataMata fase) {

        if (clubes == null || clubes.size() < 2) return;

        if (partidaMataMataRepository.existsByEdicaoAndFase(edicao, fase)) {
            return;
        }

        if (edicao.getFaseAtualMataMata() != null
                && !edicao.getFaseAtualMataMata().equals(fase)) {
            clubes = buscarVencedoresDaRodadaAnterior(edicao);
        }

        if (clubes.size() < 2) return;

        edicao.setMataMataIniciado(true);
        edicao.setFaseAtualMataMata(fase);
        edicaoRepository.save(edicao);

        int ultimaRodada = rodadaRepository.findMaxNumeroByEdicao(edicao)
                .orElse(0);
        int novaRodadaNumero = ultimaRodada + 1;

        RodadaEntity rodada = new RodadaEntity();
        rodada.setNumero(novaRodadaNumero);
        rodada.setEdicao(edicao);
        rodada.setFinalizada(false);
        rodada = rodadaRepository.save(rodada);

        List<PartidaMataMataEntity> partidas = new ArrayList<>();

        for (int i = 0; i < clubes.size(); i += 2) {
            if (i + 1 >= clubes.size()) break;

            ClubeParticipanteEntity clubeA = clubes.get(i);
            ClubeParticipanteEntity clubeB = clubes.get(i + 1);

            PartidaMataMataEntity partida = new PartidaMataMataEntity();
            partida.setClubeMandante(clubeA.getClube());
            partida.setClubeVisitante(clubeB.getClube());
            partida.setEdicao(edicao);
            partida.setFase(fase);
            partida.setFinalizada(false);
            partida.setGolsMandante(0);
            partida.setGolsVisitante(0);
            partida.setRodada(rodada);

            partidas.add(partida);
        }

        partidaMataMataRepository.saveAll(partidas);
    }


    private List<List<ClubeParticipanteEntity>> dividirClubesEmGrupos(List<ClubeParticipanteEntity> clubes) {
        int gruposCount = clubes.size() / 4;
        List<List<ClubeParticipanteEntity>> grupos = new ArrayList<>();

        for (int i = 0; i < gruposCount; i++) {
            grupos.add(new ArrayList<>());
        }

        for (int i = 0; i < clubes.size(); i++) {
            grupos.get(i % gruposCount).add(clubes.get(i));
        }

        return grupos;
    }

    private List<PartidaEntity> gerarPartidasFaseDeGrupos(
            List<ClubeParticipanteEntity> grupo,
            EdicaoCampeonatoEntity edicao,
            GrupoEntity grupoEntity
    ) {
        List<PartidaEntity> partidas = new ArrayList<>();

        int rodadaNumero = 1;
        // Trabalha sobre uma cópia para não poluir a lista recebida do chamador
        // (o 'null' adicionado como "bye" para grupos ímpares vazava para 'gerarFaseDeGrupos').
        grupo = new ArrayList<>(grupo);
        int n = grupo.size();

        if (n % 2 != 0) {
            grupo.add(null);
            n++;
        }

        for (int rodada = 0; rodada < n - 1; rodada++) {
            Set<Long> clubesDaRodada = new HashSet<>();
            for (int i = 0; i < n / 2; i++) {
                ClubeParticipanteEntity mandanteEntity = grupo.get(i);
                ClubeParticipanteEntity visitanteEntity = grupo.get(n - 1 - i);

                if (mandanteEntity == null || visitanteEntity == null) continue;

                ClubeEntity mandante = mandanteEntity.getClube();
                ClubeEntity visitante = visitanteEntity.getClube();

                if (clubesDaRodada.contains(mandante.getId()) || clubesDaRodada.contains(visitante.getId())) {
                    continue;
                }

                PartidaEntity partida = new PartidaEntity();
                partida.setClubeMandante(mandante);
                partida.setClubeVisitante(visitante);
                partida.setEdicao(edicao);
                partida.setRodada(rodadaNumero);
                partida.setGolsMandante(0);
                partida.setGolsVisitante(0);
                partida.setFinalizada(false);

                partidas.add(partida);

                clubesDaRodada.add(mandante.getId());
                clubesDaRodada.add(visitante.getId());
            }

            List<ClubeParticipanteEntity> novaOrdem = new ArrayList<>();
            novaOrdem.add(grupo.get(0));
            novaOrdem.add(grupo.get(n - 1));
            novaOrdem.addAll(grupo.subList(1, n - 1));
            grupo = novaOrdem;

            rodadaNumero++;
        }

        return partidas;
    }

    private PartidaMataMataEntity.FaseMataMata definirFaseInicial(int quantidadeClassificados) {
        return switch (quantidadeClassificados) {
            case 16 -> PartidaMataMataEntity.FaseMataMata.OITAVAS;
            case 8 -> PartidaMataMataEntity.FaseMataMata.QUARTAS;
            case 4 -> PartidaMataMataEntity.FaseMataMata.SEMIFINAL;
            case 2 -> PartidaMataMataEntity.FaseMataMata.FINAL;
            default -> throw new DadosInvalidosException("Quantidade de classificados inválida: " + quantidadeClassificados);
        };

    }

    private PartidaMataMataEntity.FaseMataMata determinarProximaFase(PartidaMataMataEntity.FaseMataMata faseInicial){
        return switch (faseInicial){
            case OITAVAS -> PartidaMataMataEntity.FaseMataMata.QUARTAS;
            case QUARTAS -> PartidaMataMataEntity.FaseMataMata.SEMIFINAL;
            case SEMIFINAL -> PartidaMataMataEntity.FaseMataMata.FINAL;
            case FINAL -> null;
        };
    }

    public void gerarProximaFase(EdicaoCampeonatoEntity edicao) {
        List<ClubeParticipanteEntity> vencedores = buscarVencedoresDaRodadaAnterior(edicao);

        PartidaMataMataEntity.FaseMataMata proximaFase = determinarProximaFase(edicao.getFaseAtualMataMata());

        gerarPartidasMataMataRecursivamente(vencedores, edicao, proximaFase);
    }

    @Transactional(readOnly = true)
    private List<ClubeParticipanteEntity> buscarVencedoresDaRodadaAnterior(EdicaoCampeonatoEntity edicao) {
        Optional<Integer> numeroRodadaAtualOpt = rodadaRepository.findMaxNumeroByEdicao(edicao);

        if (numeroRodadaAtualOpt.isEmpty() || numeroRodadaAtualOpt.get() <= 1) {
            throw new DadosInvalidosException("Não há rodada anterior para buscar vencedores.");
        }

        RodadaEntity rodadaAtual = rodadaRepository
                .findByNumeroAndEdicao(numeroRodadaAtualOpt.get(), edicao)
                .orElseThrow(() -> new NaoEncontradoException("Rodada anterior não encontrada."));

        List<PartidaMataMataEntity> partidasDaRodada =
                partidaMataMataRepository.findByRodadaId(rodadaAtual.getId());

        if (partidasDaRodada.isEmpty()) {
            throw new NaoEncontradoException("A rodada não foi encontrada.");
        }

        List<ClubeParticipanteEntity> vencedores = new ArrayList<>();

        for (PartidaMataMataEntity partida : partidasDaRodada) {
            ClubeEntity vencedor;

            if (partida.getGolsClubeA() > partida.getGolsClubeB()) {
                vencedor = partida.getMandante();
            } else if (partida.getGolsClubeB() > partida.getGolsClubeA()) {
                vencedor = partida.getVisitante();
            } else {
                vencedor = ThreadLocalRandom.current().nextBoolean()
                        ? partida.getMandante()
                        : partida.getVisitante();
            }

            ClubeParticipanteEntity participanteVencedor =
                    clubeParticipanteRepository.findByClubeAndEdicao(vencedor, edicao)
                            .orElseThrow(() -> new DadosInvalidosException("Clube vencedor não encontrado como participante."));

            vencedores.add(participanteVencedor);
        }

        return vencedores;
    }
}
