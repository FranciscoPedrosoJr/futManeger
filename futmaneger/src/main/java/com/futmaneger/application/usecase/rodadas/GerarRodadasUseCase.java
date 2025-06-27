package com.futmaneger.application.usecase.rodadas;

import com.futmaneger.application.dto.GerarRodadasRequestDTO;
import com.futmaneger.application.dto.GerarRodadasResponseDTO;
import com.futmaneger.domain.entity.Clube;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeParticipanteEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaEntity;
import com.futmaneger.infrastructure.persistence.entity.RodadaEntity;
import com.futmaneger.infrastructure.persistence.jpa.CampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.ClubeParticipanteRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaRepository;
import com.futmaneger.infrastructure.persistence.jpa.RodadaRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GerarRodadasUseCase {

    private final CampeonatoRepository campeonatoRepository;
    private final ClubeParticipanteRepository participanteRepository;
    private final RodadaRepository rodadaRepository;
    private final PartidaRepository partidaRepository;

    public GerarRodadasUseCase(
            CampeonatoRepository campeonatoRepository,
            ClubeParticipanteRepository participanteRepository,
            RodadaRepository rodadaRepository,
            PartidaRepository partidaRepository
    ) {
        this.campeonatoRepository = campeonatoRepository;
        this.participanteRepository = participanteRepository;
        this.rodadaRepository = rodadaRepository;
        this.partidaRepository = partidaRepository;
    }

    @Transactional
    public GerarRodadasResponseDTO executar(GerarRodadasRequestDTO request) {
        CampeonatoEntity campeonato = campeonatoRepository.findById(request.campeonatoId())
                .orElseThrow(() -> new IllegalArgumentException("Campeonato não encontrado"));

        if (!campeonato.getRodadas().isEmpty()) {
            throw new IllegalStateException("Rodadas já foram geradas para este campeonato");
        }

        if (campeonato.getTipo() == CampeonatoEntity.TipoCampeonato.MATA_MATA) {
            throw new UnsupportedOperationException("Geração de rodadas para mata-mata não implementada ainda");
        }

        List<ClubeParticipanteEntity> participantes = participanteRepository.findByCampeonato(campeonato);
        List<Clube> clubes = participantes.stream().map(ClubeParticipanteEntity::getClube).toList();

        if (clubes.size() < 2) {
            throw new IllegalStateException("Número insuficiente de clubes para gerar rodadas");
        }

        List<PartidaEntity> todasPartidas = new ArrayList<>();
        List<RodadaEntity> rodadas = new ArrayList<>();

        int rodadaIndex = 1;

        // ida
        List<List<PartidaEntity>> partidasPorRodadaIda = gerarPartidasRoundRobin(clubes, false);
        for (List<PartidaEntity> partidas : partidasPorRodadaIda) {
            RodadaEntity rodada = new RodadaEntity();
            rodada.setNumero(rodadaIndex++);
            rodada.setCampeonato(campeonato);
            rodada = rodadaRepository.save(rodada);

            for (PartidaEntity partida : partidas) {
                partida.setRodada(rodada);
                todasPartidas.add(partidaRepository.save(partida));
            }
            rodadas.add(rodada);
        }

        // volta
        List<List<PartidaEntity>> partidasPorRodadaVolta = gerarPartidasRoundRobin(clubes, true);
        for (List<PartidaEntity> partidas : partidasPorRodadaVolta) {
            RodadaEntity rodada = new RodadaEntity();
            rodada.setNumero(rodadaIndex++);
            rodada.setCampeonato(campeonato);
            rodada = rodadaRepository.save(rodada);

            for (PartidaEntity partida : partidas) {
                partida.setRodada(rodada);
                todasPartidas.add(partidaRepository.save(partida));
            }
            rodadas.add(rodada);
        }

        return new GerarRodadasResponseDTO(
                campeonato.getId(),
                rodadas.size(),
                todasPartidas.size()
        );
    }

    private List<List<PartidaEntity>> gerarPartidasRoundRobin(List<Clube> clubes, boolean inverterMandos) {
        List<List<PartidaEntity>> rodadas = new ArrayList<>();

        int n = clubes.size();
        List<Clube> lista = new ArrayList<>(clubes);

        if (n % 2 != 0) {
            lista.add(null);
            n++;
        }

        int rodadasTotais = n - 1;

        for (int rodada = 0; rodada < rodadasTotais; rodada++) {
            List<PartidaEntity> partidasDaRodada = new ArrayList<>();

            for (int i = 0; i < n / 2; i++) {
                Clube casa = lista.get(i);
                Clube fora = lista.get(n - 1 - i);

                if (casa != null && fora != null) {
                    PartidaEntity partida = new PartidaEntity();
                    partida.setClubeMandante(inverterMandos ? fora : casa);
                    partida.setClubeVisitante(inverterMandos ? casa : fora);
                    partidasDaRodada.add(partida);
                }
            }

            rodadas.add(partidasDaRodada);

            List<Clube> novaLista = new ArrayList<>();
            novaLista.add(lista.get(0));
            novaLista.add(lista.get(n - 1));
            novaLista.addAll(lista.subList(1, n - 1));
            lista = novaLista;
        }

        return rodadas;
    }
}
