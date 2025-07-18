package com.futmaneger.application.usecase.rodadas;

import com.futmaneger.infrastructure.persistence.entity.CampeonatoClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaEntity;
import com.futmaneger.infrastructure.persistence.jpa.CampeonatoClubeRepository;
import com.futmaneger.infrastructure.persistence.jpa.CampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GerarRodadasMataMataUseCase {

    private final CampeonatoClubeRepository campeonatoClubeRepository;
    private final PartidaRepository partidaRepository;

    public GerarRodadasMataMataUseCase(CampeonatoClubeRepository campeonatoClubeRepository,
                                       PartidaRepository partidaRepository) {
        this.campeonatoClubeRepository = campeonatoClubeRepository;
        this.partidaRepository = partidaRepository;
    }

    public void gerar(CampeonatoEntity campeonato) {
        List<CampeonatoClubeEntity> clubes = campeonatoClubeRepository.findByCampeonato(campeonato);

        if (clubes.size() < 4) {
            throw new IllegalStateException("Campeonato do tipo mata-mata deve ter pelo menos 4 clubes");
        }

        List<List<CampeonatoClubeEntity>> grupos = dividirEmGrupos(clubes);

        int rodada = 1;
        for (List<CampeonatoClubeEntity> grupo : grupos) {
            List<PartidaEntity> partidas = gerarPartidasGrupo(grupo, campeonato, rodada++);
            partidaRepository.saveAll(partidas);
        }

        // (avançar para o mata-mata): implementar depois que resultados existirem
    }

    private List<List<CampeonatoClubeEntity>> dividirEmGrupos(List<CampeonatoClubeEntity> clubes) {
        List<List<CampeonatoClubeEntity>> grupos = new ArrayList<>();

        int maxPorGrupo = 4;
        for (int i = 0; i < clubes.size(); i += maxPorGrupo) {
            int fim = Math.min(i + maxPorGrupo, clubes.size());
            grupos.add(clubes.subList(i, fim));
        }

        return grupos;
    }

    private List<PartidaEntity> gerarPartidasGrupo(List<CampeonatoClubeEntity> grupo, CampeonatoEntity campeonato, int rodada) {
        List<PartidaEntity> partidas = new ArrayList<>();

        for (int i = 0; i < grupo.size(); i++) {
            for (int j = i + 1; j < grupo.size(); j++) {
                CampeonatoClubeEntity mandante = grupo.get(i);
                CampeonatoClubeEntity visitante = grupo.get(j);

                partidas.add(new PartidaEntity());
            }
        }

        return partidas;
    }
}
