package com.futmaneger.application.usecase.rodadas;

import com.futmaneger.application.dto.GerarFaseDeGruposResponseDTO;
import com.futmaneger.application.dto.GerarRodadasResponseDTO;
import com.futmaneger.application.dto.GrupoResponseDTO;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeParticipanteEntity;
import com.futmaneger.infrastructure.persistence.entity.GrupoEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaFaseDeGruposEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaMataMataEntity;
import com.futmaneger.infrastructure.persistence.entity.TabelaCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeParticipanteRepository;
import com.futmaneger.infrastructure.persistence.jpa.GrupoRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaFaseDeGruposRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaMataMataRepository;
import com.futmaneger.infrastructure.persistence.jpa.TabelaCampeonatoRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GerarRodadasMataMataUseCase {

    private final ClubeParticipanteRepository clubeParticipanteRepository;
    private final GrupoRepository gruposRepository;
    private final PartidaFaseDeGruposRepository partidaFaseDeGruposRepository;
    private final PartidaMataMataRepository partidaMataMataRepository;
    private final TabelaCampeonatoRepository tabelaCampeonatoRepository;

    public GerarRodadasMataMataUseCase(ClubeParticipanteRepository clubeParticipanteRepository,
                                       GrupoRepository gruposRepository,
                                       PartidaFaseDeGruposRepository partidaFaseDeGruposRepository,
                                       PartidaMataMataRepository partidaMataMataRepository,
                                       TabelaCampeonatoRepository tabelaCampeonatoRepository) {
        this.clubeParticipanteRepository = clubeParticipanteRepository;
        this.gruposRepository = gruposRepository;
        this.partidaFaseDeGruposRepository = partidaFaseDeGruposRepository;
        this.partidaMataMataRepository = partidaMataMataRepository;
        this.tabelaCampeonatoRepository = tabelaCampeonatoRepository;
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

        int grupoIndex = 1;
        List<GrupoResponseDTO> gruposDTO = new ArrayList<>();
        for (List<ClubeParticipanteEntity> grupo : grupos) {
            GrupoEntity grupoEntity = new GrupoEntity();
            grupoEntity.setNome("Grupo " + grupoIndex++);
            grupoEntity.setCampeonato(campeonato);
            grupoEntity = gruposRepository.save(grupoEntity);

            List<PartidaFaseDeGruposEntity> partidas = gerarPartidasFaseDeGrupos(grupo, campeonato, grupoEntity);
            partidaFaseDeGruposRepository.saveAll(partidas);

            List<String> nomesClubes = grupo.stream()
                    .map(c -> c.getClube().getNome())
                    .toList();

            gruposDTO.add(new GrupoResponseDTO(grupoEntity.getNome(), nomesClubes));
        }
        return new GerarRodadasResponseDTO(campeonato.getId(),0,0, gruposDTO);
    }

    private boolean todasPartidasFaseDeGruposFinalizadas(CampeonatoEntity campeonato) {
        List<PartidaFaseDeGruposEntity> partidas = partidaFaseDeGruposRepository.findByCampeonato(campeonato);
        return partidas.stream().allMatch(PartidaFaseDeGruposEntity::isFinalizada);
    }

    public void gerarMataMata(CampeonatoEntity campeonato) {
        List<GrupoEntity> grupos = gruposRepository.findByCampeonato(campeonato);
        List<ClubeParticipanteEntity> classificados = new ArrayList<>();

        for (GrupoEntity grupo : grupos) {
            List<TabelaCampeonatoEntity> tabelaGrupo = tabelaCampeonatoRepository.findByGrupoOrderByPontosDesc(grupo);

            ClubeParticipanteEntity primeiro = (ClubeParticipanteEntity) clubeParticipanteRepository
                    .findByClubeAndCampeonato(tabelaGrupo.get(0).getClube(), campeonato)
                    .orElseThrow(() -> new IllegalStateException("Participante não encontrado"));

            ClubeParticipanteEntity segundo = (ClubeParticipanteEntity) clubeParticipanteRepository
                    .findByClubeAndCampeonato(tabelaGrupo.get(1).getClube(), campeonato)
                    .orElseThrow(() -> new IllegalStateException("Participante não encontrado"));

            classificados.add(primeiro);
            classificados.add(segundo);
        }

        gerarPartidasMataMataRecursivamente(classificados, campeonato, 1);
    }

    private void gerarPartidasMataMataRecursivamente(List<ClubeParticipanteEntity> clubes, CampeonatoEntity campeonato, int fase) {
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

    private List<PartidaFaseDeGruposEntity> gerarPartidasFaseDeGrupos(List<ClubeParticipanteEntity> grupo,
                                                                      CampeonatoEntity campeonato,
                                                                      GrupoEntity grupoEntity) {
        List<PartidaFaseDeGruposEntity> partidas = new ArrayList<>();

        for (int i = 0; i < grupo.size(); i++) {
            for (int j = i + 1; j < grupo.size(); j++) {
                PartidaFaseDeGruposEntity partida = new PartidaFaseDeGruposEntity();
                partida.setClubeMandante(grupo.get(i).getClube());
                partida.setClubeVisitante(grupo.get(j).getClube());
                partida.setCampeonato(campeonato);
                partida.setGrupo(grupoEntity);
                partida.setGolsMandante(0);
                partida.setGolsVisitante(0);
                partida.setRodada(j++);
                partida.setFinalizada(false);

                partidas.add(partida);
            }
        }

        return partidas;
    }

}
