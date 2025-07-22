package com.futmaneger.application.usecase.rodadas;

import com.futmaneger.domain.entity.Clube;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeParticipanteEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaEntity;
import com.futmaneger.infrastructure.persistence.jpa.CampeonatoClubeRepository;
import com.futmaneger.infrastructure.persistence.jpa.ClubeParticipanteRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class GerarRodadasMataMataUseCase {

    private final CampeonatoClubeRepository campeonatoClubeRepository;
    private final PartidaRepository partidaRepository;
    private final ClubeParticipanteRepository clubeParticipanteRepository;

    public GerarRodadasMataMataUseCase(CampeonatoClubeRepository campeonatoClubeRepository,
                                       PartidaRepository partidaRepository,
                                       ClubeParticipanteRepository clubeParticipanteRepository) {
        this.campeonatoClubeRepository = campeonatoClubeRepository;
        this.partidaRepository = partidaRepository;
        this.clubeParticipanteRepository = clubeParticipanteRepository;
    }

    public void gerar(CampeonatoEntity campeonato) {
        List<ClubeParticipanteEntity> clubes = clubeParticipanteRepository.findByCampeonato(campeonato);

        if (clubes.size() < 4) {
            throw new IllegalStateException("Campeonato do tipo mata-mata deve ter pelo menos 4 clubes");
        }

        List<List<ClubeParticipanteEntity>> grupos = dividirEmGrupos(clubes);

        int rodada = 1;
        for (List<ClubeParticipanteEntity> grupo : grupos) {
            List<PartidaEntity> partidas = gerarPartidasGrupo(grupo, campeonato, rodada++);
            partidaRepository.saveAll(partidas);
        }

        List<ClubeParticipanteEntity> classificados = obterClassificadosDosGrupos(grupos);
        gerarMataMata(classificados, campeonato, rodada);
    }

    private List<List<ClubeParticipanteEntity>> dividirEmGrupos(List<ClubeParticipanteEntity> clubes) {
        List<List<ClubeParticipanteEntity>> grupos = new ArrayList<>();

        int maxPorGrupo = 4;
        for (int i = 0; i < clubes.size(); i += maxPorGrupo) {
            int fim = Math.min(i + maxPorGrupo, clubes.size());
            grupos.add(clubes.subList(i, fim));
        }

        return grupos;
    }

    private List<PartidaEntity> gerarPartidasGrupo(List<ClubeParticipanteEntity> grupo, CampeonatoEntity campeonato, int rodada) {
        List<PartidaEntity> partidas = new ArrayList<>();

        for (int i = 0; i < grupo.size(); i++) {
            for (int j = i + 1; j < grupo.size(); j++) {
                ClubeParticipanteEntity mandante = grupo.get(i);
                ClubeParticipanteEntity visitante = grupo.get(j);

                partidas.add(new PartidaEntity());
            }
        }

        return partidas;
    }

    private List<ClubeParticipanteEntity> obterClassificadosDosGrupos(List<List<ClubeParticipanteEntity>> grupos) {
        List<ClubeParticipanteEntity> classificados = new ArrayList<>();

        for (List<ClubeParticipanteEntity> grupo : grupos) {
            classificados.add(grupo.get(0));
            if (grupo.size() > 1) {
                classificados.add(grupo.get(1));
            }
        }

        return classificados;
    }

    private void gerarMataMata(List<ClubeParticipanteEntity> classificados, CampeonatoEntity campeonato, int rodadaInicial) {
        List<Clube> clubesAtuais = classificados.stream()
                .map(ClubeParticipanteEntity::getClube)
                .collect(Collectors.toList());

        int rodada = rodadaInicial;

        int totalClassificados = classificados.size();


        while (clubesAtuais.size() > 1) {
            List<PartidaEntity> partidasRodada = new ArrayList<>();

            for (int i = 0; i < clubesAtuais.size(); i += 2) {

                Clube mandante = clubesAtuais.get(i);
                Clube visitante = clubesAtuais.get(i + 1);

                partidasRodada.add(new PartidaEntity());
            }

            partidaRepository.saveAll(partidasRodada);

            clubesAtuais = partidasRodada.stream()
                    .map(PartidaEntity::getMandante)
                    .collect(Collectors.toList());

            rodada++;
        }
    }
}
