package com.futmaneger.application.usecase.simulacao;

import com.futmaneger.application.dto.SimulacaoRequestDTO;
import com.futmaneger.application.dto.SimulacaoResponseDTO;
import com.futmaneger.domain.entity.Clube;
import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.domain.repository.JogadorRepository;
import com.futmaneger.infrastructure.persistence.entity.EscalacaoEntity;
import com.futmaneger.infrastructure.persistence.entity.EscalacaoJogadorEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.EscalacaoRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class SimulacaoUseCase {

    private final ClubeJpaRepository clubeRepository;
    private final EscalacaoRepository escalacaoRepository;
    private final PartidaRepository partidaRepository;
    private final JogadorRepository jogadorRepository;

    public SimulacaoUseCase(
            ClubeJpaRepository clubeRepository,
            EscalacaoRepository escalacaoRepository,
            PartidaRepository partidaRepository,
            JogadorRepository jogadorRepository) {
        this.clubeRepository = clubeRepository;
        this.escalacaoRepository = escalacaoRepository;
        this.partidaRepository = partidaRepository;
        this.jogadorRepository = jogadorRepository;
    }

    public SimulacaoResponseDTO simular(SimulacaoRequestDTO request) {
        Clube mandante = buscarClube(request.clubeMandanteId());
        Clube visitante = buscarClube(request.clubeVisitanteId());

        EscalacaoEntity escalacaoMandante = buscarEscalacaoMandante(request.escalacaoMandanteId());
        EscalacaoEntity escalacaoVisitante = buscarOuGerarEscalacaoVisitante(visitante);

        int golsMandante = calcularGols(escalacaoMandante, escalacaoVisitante);
        int golsVisitante = calcularGols(escalacaoVisitante, escalacaoMandante);
        PartidaEntity.Resultado resultado = determinarResultado(golsMandante, golsVisitante);

        salvarPartida(mandante, visitante, golsMandante, golsVisitante, resultado);

        return new SimulacaoResponseDTO(
                mandante.getNome(),
                golsMandante,
                visitante.getNome(),
                golsVisitante,
                resultado.name()
        );
    }

    private Clube buscarClube(Long id) {
        return clubeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clube não encontrado: " + id));
    }

    private EscalacaoEntity buscarEscalacaoMandante(Long id) {
        return escalacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Escalação do mandante não encontrada"));
    }

    private EscalacaoEntity buscarOuGerarEscalacaoVisitante(Clube visitante) {
        if (visitante.getTecnico() != null) {
            return (EscalacaoEntity) escalacaoRepository.findTopByClubeOrderByDataHoraDesc(visitante)
                    .orElseThrow(() -> new RuntimeException("Escalação do visitante não encontrada"));
        }
        return gerarEscalacaoAutomatica(visitante);
    }

    private EscalacaoEntity gerarEscalacaoAutomatica(Clube clube) {
        EscalacaoEntity escalacao = new EscalacaoEntity();
        escalacao.setClube(clube);
        escalacao.setDataHora(LocalDateTime.now());
        escalacao.setFormacao("4-4-2");
        escalacao = escalacaoRepository.save(escalacao);

        List<Jogador> jogadores = jogadorRepository.findByClube(clube);

        List<Jogador> goleiros = filtrarPorPosicao(jogadores, Collections.singletonList("GOLEIRO"), 1);
        List<Jogador> defensores = filtrarPorPosicao(jogadores, List.of("ZAGUEIRO", "LATERAL"), 4);
        List<Jogador> meioCampo = filtrarPorPosicao(jogadores, Collections.singletonList("MEIO_CAMPO"), 4);
        List<Jogador> atacantes = filtrarPorPosicao(jogadores, Collections.singletonList("ATACANTE"), 2);

        List<Jogador> titulares = new ArrayList<>();
        titulares.addAll(goleiros);
        titulares.addAll(defensores);
        titulares.addAll(meioCampo);
        titulares.addAll(atacantes);

        for (Jogador jogador : titulares) {
            EscalacaoJogadorEntity ej = new EscalacaoJogadorEntity();
            ej.setEscalacao(escalacao);
            ej.setJogador(jogador);
            ej.setTipo(EscalacaoJogadorEntity.TipoJogador.TITULAR);
            escalacao.getJogadores().add(ej);
        }

        return escalacaoRepository.save(escalacao);
    }

    private List<Jogador> filtrarPorPosicao(Optional<Jogador> jogadores, String posicao, int limite) {
        return jogadores.stream()
                .filter(j -> posicao.equals(j.getPosicao()))
                .sorted(Comparator.comparingInt(Jogador::getForca).reversed())
                .limit(limite)
                .toList();
    }

    private List<Jogador> filtrarPorPosicao(List<Jogador> jogadores, List<String> posicoes, int limite) {
        return jogadores.stream()
                .filter(j -> posicoes.contains(j.getPosicao()))
                .sorted(Comparator.comparingInt(Jogador::getForca).reversed())
                .limit(limite)
                .toList();
    }

    private double calcularMedia(EscalacaoEntity escalacao) {
        return escalacao.getJogadores().stream()
                .filter(j -> j.getTipo() == EscalacaoJogadorEntity.TipoJogador.TITULAR)
                .mapToInt(j -> j.getJogador().getForca())
                .average()
                .orElse(0);
    }

    private int calcularGols(EscalacaoEntity ataque, EscalacaoEntity defesa) {
        double mediaAtaque = calcularMedia(ataque);
        double mediaDefesa = calcularMedia(defesa);
        double diff = mediaAtaque - mediaDefesa;

        if (diff > 10) return new Random().nextInt(3, 6); // goleada
        if (diff > 5) return new Random().nextInt(2, 4);
        return new Random().nextInt(0, 2); // equilibrado
    }

    private PartidaEntity.Resultado determinarResultado(int golsMandante, int golsVisitante) {
        if (golsMandante > golsVisitante) return PartidaEntity.Resultado.VITORIA_MANDANTE;
        if (golsMandante < golsVisitante) return PartidaEntity.Resultado.VITORIA_VISITANTE;
        return PartidaEntity.Resultado.EMPATE;
    }

    private void salvarPartida(Clube mandante, Clube visitante, int golsMandante, int golsVisitante, PartidaEntity.Resultado resultado) {
        PartidaEntity partida = new PartidaEntity();
        partida.setClubeMandante(mandante);
        partida.setClubeVisitante(visitante);
        partida.setGolsMandante(golsMandante);
        partida.setGolsVisitante(golsVisitante);
        partida.setResultado(resultado);
        partida.setDataHora(LocalDateTime.now());
        partidaRepository.save(partida);
    }
}
