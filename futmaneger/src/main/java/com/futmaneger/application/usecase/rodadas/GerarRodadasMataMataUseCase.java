package com.futmaneger.application.usecase.rodadas;

import com.futmaneger.application.dto.GerarFaseDeGruposResponseDTO;
import com.futmaneger.application.dto.GerarRodadasResponseDTO;
import com.futmaneger.application.dto.GrupoResponseDTO;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeParticipanteEntity;
import com.futmaneger.infrastructure.persistence.entity.GrupoEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaFaseDeGruposEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaMataMataEntity;
import com.futmaneger.infrastructure.persistence.entity.RodadaEntity;
import com.futmaneger.infrastructure.persistence.entity.TabelaCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeParticipanteRepository;
import com.futmaneger.infrastructure.persistence.jpa.GrupoRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaFaseDeGruposRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaMataMataRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaRepository;
import com.futmaneger.infrastructure.persistence.jpa.RodadaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TabelaCampeonatoRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GerarRodadasMataMataUseCase {

    private final ClubeParticipanteRepository clubeParticipanteRepository;
    private final GrupoRepository gruposRepository;
    private final PartidaRepository partidaRepository;
    private final PartidaMataMataRepository partidaMataMataRepository;
    private final TabelaCampeonatoRepository tabelaCampeonatoRepository;
    private final RodadaRepository rodadaRepository;

    public GerarRodadasMataMataUseCase(ClubeParticipanteRepository clubeParticipanteRepository,
                                       GrupoRepository gruposRepository,
                                       PartidaRepository partidaRepository,
                                       PartidaMataMataRepository partidaMataMataRepository,
                                       TabelaCampeonatoRepository tabelaCampeonatoRepository,
                                       RodadaRepository rodadaRepository) {

        this.clubeParticipanteRepository = clubeParticipanteRepository;
        this.gruposRepository = gruposRepository;
        this.partidaRepository = partidaRepository;
        this.partidaMataMataRepository = partidaMataMataRepository;
        this.tabelaCampeonatoRepository = tabelaCampeonatoRepository;
        this.rodadaRepository = rodadaRepository;
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
        List<ClubeParticipanteEntity> classificados = new ArrayList<>();

        for (GrupoEntity grupo : grupos) {
            List<TabelaCampeonatoEntity> tabelaGrupo = tabelaCampeonatoRepository.findByGrupoOrderByPontosDesc(grupo.getId());

            ClubeParticipanteEntity primeiro = (ClubeParticipanteEntity) clubeParticipanteRepository
                    .findByClubeAndCampeonato(tabelaGrupo.get(0).getClube(), campeonato)
                    .orElseThrow(() -> new IllegalStateException("Participante não encontrado"));

            ClubeParticipanteEntity segundo = (ClubeParticipanteEntity) clubeParticipanteRepository
                    .findByClubeAndCampeonato(tabelaGrupo.get(1).getClube(), campeonato)
                    .orElseThrow(() -> new IllegalStateException("Participante não encontrado"));

            classificados.add(primeiro);
            classificados.add(segundo);
        }

        PartidaMataMataEntity.FaseMataMata faseInicial = definirFaseInicial(classificados.size());

        gerarPartidasMataMataRecursivamente(classificados, campeonato, faseInicial);
    }

    private void gerarPartidasMataMataRecursivamente(List<ClubeParticipanteEntity> clubes, CampeonatoEntity campeonato,
                                                     PartidaMataMataEntity.FaseMataMata fase) {
        if (clubes.size() < 2) return;

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

        // Round-robin simples
        for (int rodada = 0; rodada < n - 1; rodada++) {
            for (int i = 0; i < n / 2; i++) {
                ClubeEntity mandante = grupo.get(i).getClube();
                ClubeEntity visitante = grupo.get(n - 1 - i).getClube();

                PartidaEntity partida = new PartidaEntity();
                partida.setClubeMandante(mandante);
                partida.setClubeVisitante(visitante);
                partida.setCampeonato(campeonato);
                partida.setGrupo(grupoEntity);
                partida.setRodada(rodadaNumero);
                partida.setGolsMandante(0);
                partida.setGolsVisitante(0);
                partida.setFinalizada(false);

                partidas.add(partida);
            }

            // Rotaciona os times (exceto o primeiro)
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
            default -> throw new IllegalArgumentException("Quantidade de classificados inválida: " + quantidadeClassificados);
        };

    }
}
