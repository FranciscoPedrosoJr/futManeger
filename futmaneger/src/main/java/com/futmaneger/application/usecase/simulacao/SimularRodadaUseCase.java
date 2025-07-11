package com.futmaneger.application.usecase.simulacao;

import com.futmaneger.application.dto.SimulacaoResponseDTO;
import com.futmaneger.domain.entity.Clube;
import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.domain.repository.JogadorRepository;
import com.futmaneger.infrastructure.persistence.entity.*;
import com.futmaneger.infrastructure.persistence.jpa.CampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.EscalacaoRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaRepository;
import com.futmaneger.infrastructure.persistence.jpa.RodadaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TabelaCampeonatoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SimularRodadaUseCase {

    private final RodadaRepository rodadaRepository;
    private final PartidaRepository partidaRepository;
    private final EscalacaoRepository escalacaoRepository;
    private final JogadorRepository jogadorRepository;
    private final TabelaCampeonatoRepository tabelaCampeonato;
    private final CampeonatoRepository campeonatoRepository;

    public SimularRodadaUseCase(
            RodadaRepository rodadaRepository,
            PartidaRepository partidaRepository,
            EscalacaoRepository escalacaoRepository,
            JogadorRepository jogadorRepository,
            TabelaCampeonatoRepository tabelaCampeonato,
            CampeonatoRepository campeonatoRepository
    ) {
        this.rodadaRepository = rodadaRepository;
        this.partidaRepository = partidaRepository;
        this.escalacaoRepository = escalacaoRepository;
        this.jogadorRepository = jogadorRepository;
        this.tabelaCampeonato = tabelaCampeonato;
        this.campeonatoRepository = campeonatoRepository;
    }

    @Transactional
    public List<SimulacaoResponseDTO> executar(Long rodadaId) {
        RodadaEntity rodada = rodadaRepository.findById(rodadaId)
                .orElseThrow(() -> new IllegalArgumentException("Rodada não encontrada com id: " + rodadaId));

        CampeonatoEntity campeonato = rodada.getCampeonato();

        List<PartidaEntity> partidas = rodada.getPartidas();
        if (partidas.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma partida cadastrada para a rodada");
        }

        List<SimulacaoResponseDTO> resultados = new ArrayList<>();

        for (PartidaEntity partida : partidas) {
            if (partida.getResultado() != null) {
                continue;
            }

            EscalacaoEntity escalacaoMandante = buscarOuGerarEscalacao(partida.getClubeMandante());
            EscalacaoEntity escalacaoVisitante = buscarOuGerarEscalacao(partida.getClubeVisitante());

            int golsMandante = calcularGols(escalacaoMandante, escalacaoVisitante);
            int golsVisitante = calcularGols(escalacaoVisitante, escalacaoMandante);

            PartidaEntity.Resultado resultado = determinarResultado(golsMandante, golsVisitante);

            partida.setGolsMandante(golsMandante);
            partida.setGolsVisitante(golsVisitante);
            partida.setResultado(resultado);
            partida.setDataHora(LocalDateTime.now());
            partidaRepository.save(partida);

            atualizarTabela(campeonato, partida.getClubeMandante(), golsMandante, golsVisitante, resultado, true);
            atualizarTabela(campeonato, partida.getClubeVisitante(), golsVisitante, golsMandante, resultado, false);

            resultados.add(new SimulacaoResponseDTO(
                    partida.getClubeMandante().getNome(),
                    golsMandante,
                    partida.getClubeVisitante().getNome(),
                    golsVisitante,
                    resultado.name()
            ));


        }

        rodada.setFinalizada(true);

        if (isUltimaRodada(rodada, campeonato)) {
            definirCampeao(campeonato);
        }

        return resultados;
    }

    private EscalacaoEntity buscarOuGerarEscalacao(Clube clube) {
        if (clube.getTecnico() != null) {
            return escalacaoRepository.findTopByClubeOrderByDataHoraDesc(clube)
                    .orElseThrow(() -> new RuntimeException("Escalação não encontrada para clube com técnico: " + clube.getNome()));
        }
        return gerarEscalacaoAutomatica(clube);
    }

    private EscalacaoEntity gerarEscalacaoAutomatica(Clube clube) {
        EscalacaoEntity escalacao = new EscalacaoEntity();
        escalacao.setClube(clube);
        escalacao.setDataHora(LocalDateTime.now());
        escalacao.setFormacao("4-4-2");
        //escalacao = escalacaoRepository.save(escalacao);

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

        List<Jogador> reservasPossiveis = jogadores.stream()
                .filter(j -> !titulares.contains(j))
                .sorted(Comparator.comparingInt(Jogador::getForca).reversed())
                .limit(6)
                .toList();

        for (Jogador jogador : reservasPossiveis) {
            EscalacaoJogadorEntity ej = new EscalacaoJogadorEntity();
            ej.setEscalacao(escalacao);
            ej.setJogador(jogador);
            ej.setTipo(EscalacaoJogadorEntity.TipoJogador.RESERVA);
            escalacao.getJogadores().add(ej);
        }

        return escalacaoRepository.save(escalacao);
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

        Random random = new Random();
        if (diff > 10) return random.nextInt(3, 6);
        if (diff > 5) return random.nextInt(2, 4);
        return random.nextInt(0, 2);
    }

    private PartidaEntity.Resultado determinarResultado(int golsMandante, int golsVisitante) {
        if (golsMandante > golsVisitante) return PartidaEntity.Resultado.VITORIA_MANDANTE;
        if (golsMandante < golsVisitante) return PartidaEntity.Resultado.VITORIA_VISITANTE;
        return PartidaEntity.Resultado.EMPATE;
    }

    private void atualizarTabela(
            CampeonatoEntity campeonato,
            Clube clube,
            int golsPro,
            int golsContra,
            PartidaEntity.Resultado resultado,
            boolean isMandante
    ) {
        TabelaCampeonatoEntity tabela = tabelaCampeonato
                .findByCampeonatoIdAndClubeId(campeonato.getId(), clube.getId())
                .orElse(null);

        if (tabela == null) {
            tabela = new TabelaCampeonatoEntity();
            tabela.setCampeonato(campeonato);
            tabela.setClube(clube);
            tabela.setJogos(0);
            tabela.setPontos(0);
            tabela.setVitorias(0);
            tabela.setEmpates(0);
            tabela.setDerrotas(0);
            tabela.setGolsPro(0);
            tabela.setGolsContra(0);
            tabela.setSaldoGols(0);
        }

        tabela.setJogos(tabela.getJogos() + 1);
        tabela.setGolsPro(tabela.getGolsPro() + golsPro);
        tabela.setGolsContra(tabela.getGolsContra() + golsContra);
        tabela.setSaldoGols(tabela.getSaldoGols() + (golsPro - golsContra));

        switch (resultado)
        {
            case VITORIA_MANDANTE -> {
                if (isMandante) {
                    tabela.setVitorias(tabela.getVitorias() + 1);
                    tabela.setPontos(tabela.getPontos() + 3);
                } else {
                    tabela.setDerrotas(tabela.getDerrotas() + 1);
                }
            }
            case VITORIA_VISITANTE -> {
                if (!isMandante) {
                    tabela.setVitorias(tabela.getVitorias() + 1);
                    tabela.setPontos(tabela.getPontos() + 3);
                } else {
                    tabela.setDerrotas(tabela.getDerrotas() + 1);
                }
            }
            case EMPATE -> {
                tabela.setEmpates(tabela.getEmpates() + 1);
                tabela.setPontos(tabela.getPontos() + 1);
            }
        }

        tabelaCampeonato.save(tabela);
    }

    private void definirCampeao(CampeonatoEntity campeonato) {
        List<TabelaCampeonatoEntity> classificacao = tabelaCampeonato
                .findByCampeonatoOrderByPontosDescSaldoGolsDescGolsProDesc(campeonato);

        if (!classificacao.isEmpty()) {
            Clube campeao = classificacao.get(0).getClube();
            campeonato.setCampeao(campeao);
            campeonatoRepository.save(campeonato);
        }
    }

    private boolean isUltimaRodada(RodadaEntity rodada, CampeonatoEntity campeonato) {
        int totalRodadas = campeonato.getRodadas().size();
        return rodada.getNumero() == totalRodadas;
    }
}