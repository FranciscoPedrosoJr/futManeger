package com.futmaneger.application.usecase.rodadas;

import com.futmaneger.application.dto.GerarRodadasRequestDTO;
import com.futmaneger.application.dto.GerarRodadasResponseDTO;
import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeParticipanteEntity;
import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaEntity;
import com.futmaneger.infrastructure.persistence.entity.RodadaEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeParticipanteRepository;
import com.futmaneger.infrastructure.persistence.jpa.EdicaoCampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaRepository;
import com.futmaneger.infrastructure.persistence.jpa.RodadaRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GerarRodadasUseCase {

    private final EdicaoCampeonatoRepository edicaoRepository;
    private final ClubeParticipanteRepository participanteRepository;
    private final RodadaRepository rodadaRepository;
    private final PartidaRepository partidaRepository;
    private final GerarRodadasMataMataUseCase gerarRodadasMataMataUseCase;
    private final GerarMataMataConfiguravelUseCase gerarMataMataConfiguravelUseCase;

    public GerarRodadasUseCase(
            EdicaoCampeonatoRepository edicaoRepository,
            ClubeParticipanteRepository participanteRepository,
            RodadaRepository rodadaRepository,
            PartidaRepository partidaRepository,
            GerarRodadasMataMataUseCase gerarRodadasMataMataUseCase,
            GerarMataMataConfiguravelUseCase gerarMataMataConfiguravelUseCase
    ) {
        this.edicaoRepository = edicaoRepository;
        this.participanteRepository = participanteRepository;
        this.rodadaRepository = rodadaRepository;
        this.partidaRepository = partidaRepository;
        this.gerarRodadasMataMataUseCase = gerarRodadasMataMataUseCase;
        this.gerarMataMataConfiguravelUseCase = gerarMataMataConfiguravelUseCase;
    }

    @Transactional
    public GerarRodadasResponseDTO executar(GerarRodadasRequestDTO request) {
        EdicaoCampeonatoEntity edicao = edicaoRepository.findById(request.edicaoId())
                .orElseThrow(() -> new NaoEncontradoException("Edição de campeonato não encontrada"));

        CampeonatoEntity campeonato = edicao.getCampeonato();

        if (!edicao.getRodadas().isEmpty()) {
            throw new DadosInvalidosException("Rodadas já foram geradas para esta edição do campeonato");
        }

        if (gerarMataMataConfiguravelUseCase.possuiFasesConfiguradas(campeonato)) {
            gerarMataMataConfiguravelUseCase.gerarPrimeiraFase(edicao);
            return new GerarRodadasResponseDTO(edicao.getId(), 1, 0, null);
        }

        if (campeonato.getTipo() == null) {
            throw new DadosInvalidosException("O campeonato não possui um tipo definido");
        }

        if (CampeonatoEntity.TipoCampeonato.MATA_MATA.equals(campeonato.getTipo())) {
            return gerarRodadasMataMataUseCase.gerarRodadas(edicao);
        }

        List<ClubeParticipanteEntity> participantes = participanteRepository.findByEdicao(edicao);
        List<ClubeEntity> clubes = participantes.stream().map(ClubeParticipanteEntity::getClube).toList();

        if (clubes.size() < 2) {
            throw new DadosInvalidosException("Número insuficiente de clubes para gerar rodadas");
        }

        List<PartidaEntity> todasPartidas = new ArrayList<>();
        List<RodadaEntity> rodadas = new ArrayList<>();

        int rodadaIndex = 1;

        // ida
        List<List<PartidaEntity>> partidasPorRodadaIda = gerarPartidasRoundRobin(clubes, false);
        for (List<PartidaEntity> partidas : partidasPorRodadaIda) {
            RodadaEntity rodada = new RodadaEntity();
            rodada.setNumero(rodadaIndex++);
            rodada.setEdicao(edicao);
            rodada = rodadaRepository.save(rodada);

            for (PartidaEntity partida : partidas) {
                partida.setRodada(rodada.getNumero());
                partida.setEdicao(edicao);
                todasPartidas.add(partidaRepository.save(partida));
            }
            rodadas.add(rodada);
        }

        // volta
        List<List<PartidaEntity>> partidasPorRodadaVolta = gerarPartidasRoundRobin(clubes, true);
        for (List<PartidaEntity> partidas : partidasPorRodadaVolta) {
            RodadaEntity rodada = new RodadaEntity();
            rodada.setNumero(rodadaIndex++);
            rodada.setEdicao(edicao);
            rodada = rodadaRepository.save(rodada);

            for (PartidaEntity partida : partidas) {
                partida.setRodada(rodada.getNumero());
                partida.setEdicao(edicao);
                todasPartidas.add(partidaRepository.save(partida));
            }
            rodadas.add(rodada);
        }

        return new GerarRodadasResponseDTO(
                edicao.getId(),
                rodadas.size(),
                todasPartidas.size(),
                null
        );
    }

    private List<List<PartidaEntity>> gerarPartidasRoundRobin(List<ClubeEntity> clubes, boolean inverterMandos) {
        List<List<PartidaEntity>> rodadas = new ArrayList<>();

        int n = clubes.size();
        List<ClubeEntity> lista = new ArrayList<>(clubes);

        if (n % 2 != 0) {
            lista.add(null);
            n++;
        }

        int rodadasTotais = n - 1;
        int metade = n / 2;

        for (int rodada = 0; rodada < rodadasTotais; rodada++) {
            List<PartidaEntity> partidasDaRodada = new ArrayList<>();

            for (int i = 0; i < metade; i++) {
                ClubeEntity mandante = lista.get(i);
                ClubeEntity visitante = lista.get(n - 1 - i);

                if (mandante != null && visitante != null) {
                    PartidaEntity partida = new PartidaEntity();
                    partida.setClubeMandante(inverterMandos ? visitante : mandante);
                    partida.setClubeVisitante(inverterMandos ? mandante : visitante);
                    partidasDaRodada.add(partida);
                }
            }

            rodadas.add(partidasDaRodada);

            List<ClubeEntity> novaLista = new ArrayList<>();
            novaLista.add(lista.get(0));
            novaLista.add(lista.get(n - 1));
            novaLista.addAll(lista.subList(1, n - 1));
            lista = novaLista;
        }

        return rodadas;
    }
}
