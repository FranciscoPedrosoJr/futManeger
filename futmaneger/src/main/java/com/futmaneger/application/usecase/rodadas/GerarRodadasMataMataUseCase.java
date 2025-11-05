package com.futmaneger.application.usecase.rodadas;

import com.futmaneger.application.dto.GerarRodadasResponseDTO;
import com.futmaneger.application.dto.GrupoResponseDTO;
import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeParticipanteEntity;
import com.futmaneger.infrastructure.persistence.entity.GrupoEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaFaseDeGruposEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaMataMataEntity;
import com.futmaneger.infrastructure.persistence.entity.RodadaEntity;
import com.futmaneger.infrastructure.persistence.entity.TabelaCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.jpa.CampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.ClubeParticipanteRepository;
import com.futmaneger.infrastructure.persistence.jpa.GrupoRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaMataMataRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaRepository;
import com.futmaneger.infrastructure.persistence.jpa.RodadaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TabelaCampeonatoRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class GerarRodadasMataMataUseCase {

    private final ClubeParticipanteRepository clubeParticipanteRepository;
    private final GrupoRepository gruposRepository;
    private final PartidaRepository partidaRepository;
    private final PartidaMataMataRepository partidaMataMataRepository;
    private final TabelaCampeonatoRepository tabelaCampeonatoRepository;
    private final RodadaRepository rodadaRepository;
    private final CampeonatoRepository campeonatoRepository;

    public GerarRodadasMataMataUseCase(ClubeParticipanteRepository clubeParticipanteRepository,
                                       GrupoRepository gruposRepository,
                                       PartidaRepository partidaRepository,
                                       PartidaMataMataRepository partidaMataMataRepository,
                                       TabelaCampeonatoRepository tabelaCampeonatoRepository,
                                       RodadaRepository rodadaRepository,
                                       CampeonatoRepository campeonatoRepository) {

        this.clubeParticipanteRepository = clubeParticipanteRepository;
        this.gruposRepository = gruposRepository;
        this.partidaRepository = partidaRepository;
        this.partidaMataMataRepository = partidaMataMataRepository;
        this.tabelaCampeonatoRepository = tabelaCampeonatoRepository;
        this.rodadaRepository = rodadaRepository;
        this.campeonatoRepository = campeonatoRepository;
    }

    public GerarRodadasResponseDTO gerarRodadas(CampeonatoEntity campeonato) {
        if (!campeonato.getTipo().equals(CampeonatoEntity.TipoCampeonato.MATA_MATA)) {
            return null;
        }

        if (gruposRepository.existsByCampeonato(campeonato)) {
            if (todasPartidasFaseDeGruposFinalizadas(campeonato) &&
                    !partidaMataMataRepository.existsByCampeonato(campeonato)) {
                gerarMataMata(campeonato);
            }
        }
        return gerarFaseDeGrupos(campeonato);

    }

    private GerarRodadasResponseDTO gerarFaseDeGrupos(CampeonatoEntity campeonato) {
        List<ClubeParticipanteEntity> clubes = clubeParticipanteRepository.findByCampeonato(campeonato);

        if (clubes.size() < 4) {
            throw new IllegalStateException("Campeonato do tipo mata-mata deve ter pelo menos 4 clubes.");
        }

        List<List<ClubeParticipanteEntity>> grupos = dividirClubesEmGrupos(clubes);
        List<RodadaEntity> rodadas = new ArrayList<>();
        List<PartidaEntity> todasPartidas = new ArrayList<>();
        List<GrupoResponseDTO> gruposDTO = new ArrayList<>();

        int rodadaIndex = 1;

        int grupoIndex = 1;

        for (List<ClubeParticipanteEntity> grupo : grupos) {
            GrupoEntity grupoEntity = new GrupoEntity();
            grupoEntity.setNome("Grupo " + grupoIndex++);
            grupoEntity.setCampeonato(campeonato);

            Set<ClubeEntity> clubesDoGrupo = grupo.stream()
                    .map(ClubeParticipanteEntity::getClube)
                    .collect(Collectors.toSet());

            grupoEntity.setClubes(clubesDoGrupo);
            grupoEntity = gruposRepository.save(grupoEntity);

            List<PartidaEntity> partidas = gerarPartidasFaseDeGrupos(grupo, campeonato, grupoEntity);

            for (PartidaEntity partida : partidas) {
                RodadaEntity rodada = rodadaRepository.findByNumeroAndCampeonato(partida.getRodada(), campeonato)
                        .orElseGet(() -> {
                            RodadaEntity novaRodada = new RodadaEntity();
                            novaRodada.setNumero(partida.getRodada());
                            novaRodada.setCampeonato(campeonato);
                            return rodadaRepository.save(novaRodada);
                        });

                if (!rodadas.contains(rodada)) {
                    rodadas.add(rodada);
                }

                partidaRepository.save(partida);
                todasPartidas.add(partida);
            }

            List<String> nomesClubes = grupo.stream()
                    .map(c -> c.getClube().getNome())
                    .toList();

            gruposDTO.add(new GrupoResponseDTO(grupoEntity.getNome(), nomesClubes));
        }

        return new GerarRodadasResponseDTO(campeonato.getId(), rodadas.size(), todasPartidas.size(), gruposDTO);
    }

    private boolean todasPartidasFaseDeGruposFinalizadas(CampeonatoEntity campeonato) {
        List<PartidaFaseDeGruposEntity> partidas = partidaRepository.findByCampeonato(campeonato);
        return partidas.stream().allMatch(PartidaFaseDeGruposEntity::isFinalizada);
    }

    public void gerarMataMata(CampeonatoEntity campeonato) {
        List<GrupoEntity> grupos = gruposRepository.findByCampeonato(campeonato);
        if (grupos == null || grupos.isEmpty()) {
            throw new NaoEncontradoException("Nenhum grupo encontrado para o campeonato id=" + campeonato.getId());
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
                    .findByClubeAndCampeonato(clubePrimeiro, campeonato)
                    .orElseThrow(() -> new NaoEncontradoException("Participante não encontrado para clube: " + clubePrimeiro.getNome()));

            ClubeParticipanteEntity segundo = clubeParticipanteRepository
                    .findByClubeAndCampeonato(clubeSegundo, campeonato)
                    .orElseThrow(() -> new NaoEncontradoException("Participante não encontrado para clube: " + clubeSegundo.getNome()));

            classificados.add(primeiro);
            classificados.add(segundo);
        }

        if (classificados.isEmpty()) {
            throw new DadosInvalidosException("Nenhum clube classificado encontrado para gerar mata-mata.");
        }

        PartidaMataMataEntity.FaseMataMata faseInicial = campeonato.getFaseAtualMataMata();
        if (faseInicial == null) {
            faseInicial = definirFaseInicial(classificados.size());
        } else {
            faseInicial = determinarProximaFase(faseInicial);
        }

        gerarPartidasMataMataRecursivamente(classificados, campeonato, faseInicial);
    }

    private void gerarPartidasMataMataRecursivamente(
            List<ClubeParticipanteEntity> clubes,
            CampeonatoEntity campeonato,
            PartidaMataMataEntity.FaseMataMata fase) {

        if (clubes == null || clubes.size() < 2) return;

        if (partidaMataMataRepository.existsByCampeonatoAndFase(campeonato, fase)) {
            return;
        }

        campeonato.setMataMataIniciado(true);
        campeonato.setFaseAtualMataMata(fase);
        campeonatoRepository.save(campeonato);

        int ultimaRodada = rodadaRepository.findMaxNumeroByCampeonato(campeonato)
                .orElse(0);
        int novaRodadaNumero = ultimaRodada + 1;

        RodadaEntity rodada = new RodadaEntity();
        rodada.setNumero(novaRodadaNumero);
        rodada.setCampeonato(campeonato);
        rodada.setFinalizada(false);
        rodada = rodadaRepository.save(rodada);

        List<PartidaMataMataEntity> partidas = new ArrayList<>();

        for (int i = 0; i < clubes.size(); i += 2) {
            ClubeParticipanteEntity clubeA = clubes.get(i);
            ClubeParticipanteEntity clubeB = clubes.get(i + 1);

            PartidaMataMataEntity partida = new PartidaMataMataEntity();
            partida.setClubeMandante(clubeA.getClube());
            partida.setClubeVisitante(clubeB.getClube());
            partida.setCampeonato(campeonato);
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
            CampeonatoEntity campeonato,
            GrupoEntity grupoEntity
    ) {
        List<PartidaEntity> partidas = new ArrayList<>();

        int rodadaNumero = 1;
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
                partida.setCampeonato(campeonato);
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
}
