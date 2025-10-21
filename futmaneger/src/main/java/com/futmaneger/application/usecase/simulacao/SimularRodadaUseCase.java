package com.futmaneger.application.usecase.simulacao;

import static com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity.TipoCampeonato.MATA_MATA;
import static com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity.TipoCampeonato.PONTOS_CORRIDOS;

import com.futmaneger.application.dto.SimulacaoResponseDTO;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.application.usecase.rodadas.GerarRodadasMataMataUseCase;
import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.domain.entity.PartidaSimulavel;
import com.futmaneger.domain.repository.JogadorRepository;
import com.futmaneger.infrastructure.persistence.entity.*;
import com.futmaneger.infrastructure.persistence.jpa.CampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.ClubeParticipanteRepository;
import com.futmaneger.infrastructure.persistence.jpa.EscalacaoRepository;
import com.futmaneger.infrastructure.persistence.jpa.GrupoRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaMataMataRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaRepository;
import com.futmaneger.infrastructure.persistence.jpa.RodadaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TabelaCampeonatoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SimularRodadaUseCase {

    private final RodadaRepository rodadaRepository;
    private final PartidaRepository partidaRepository;
    private final EscalacaoRepository escalacaoRepository;
    private final JogadorRepository jogadorRepository;
    private final TabelaCampeonatoRepository tabelaCampeonato;
    private final CampeonatoRepository campeonatoRepository;
    private final GerarRodadasMataMataUseCase gerarRodadasMataMataUseCase;
    private final ClubeParticipanteRepository clubeParticipanteRepository;
    private final GrupoRepository grupoRepository;
    private final PartidaMataMataRepository partidaMataMataRepository;

    public SimularRodadaUseCase(
            RodadaRepository rodadaRepository,
            PartidaRepository partidaRepository,
            EscalacaoRepository escalacaoRepository,
            JogadorRepository jogadorRepository,
            TabelaCampeonatoRepository tabelaCampeonato,
            CampeonatoRepository campeonatoRepository,
            GerarRodadasMataMataUseCase gerarRodadasMataMataUseCase,
            ClubeParticipanteRepository clubeParticipanteRepository,
            GrupoRepository grupoRepository,
            PartidaMataMataRepository partidaMataMataRepository
    ) {
        this.rodadaRepository = rodadaRepository;
        this.partidaRepository = partidaRepository;
        this.escalacaoRepository = escalacaoRepository;
        this.jogadorRepository = jogadorRepository;
        this.tabelaCampeonato = tabelaCampeonato;
        this.campeonatoRepository = campeonatoRepository;
        this.gerarRodadasMataMataUseCase = gerarRodadasMataMataUseCase;
        this.clubeParticipanteRepository = clubeParticipanteRepository;
        this.grupoRepository = grupoRepository;
        this.partidaMataMataRepository = partidaMataMataRepository;
    }

    @Transactional
    public List<SimulacaoResponseDTO> executar(Long campeonatoId, Long rodadaId) {
        CampeonatoEntity campeonato = campeonatoRepository.findById(campeonatoId)
                .orElseThrow(() -> new IllegalArgumentException("Campeonato não encontrado"));

        RodadaEntity rodada = rodadaRepository.findByIdAndCampeonatoIdWithFetch(rodadaId, campeonatoId)
                .orElseThrow(() -> new IllegalArgumentException("Rodada não encontrada com id: " + rodadaId));

        List<? extends PartidaSimulavel> partidas;
        List<GrupoEntity> grupo = grupoRepository.findByCampeonato(campeonato);

        if (Boolean.TRUE.equals(campeonato.getMataMataIniciado())) {
            partidas = partidaMataMataRepository.findByRodada(rodada);
        } else {
            partidas = rodada.getPartidas();
        }

        if (partidas.isEmpty()) {
            throw new NaoEncontradoException("Nenhuma partida cadastrada");
        }

        List<SimulacaoResponseDTO> resultados = new ArrayList<>();

        for (PartidaSimulavel partida : partidas) {
            if (partida.isFinalizada()) continue;

            EscalacaoEntity escalacaoMandante = buscarOuGerarEscalacao(partida.getMandante());
            EscalacaoEntity escalacaoVisitante = buscarOuGerarEscalacao(partida.getVisitante());

            int golsMandante = calcularGols(escalacaoMandante, escalacaoVisitante);
            int golsVisitante = calcularGols(escalacaoVisitante, escalacaoMandante);

            if (campeonato.getFaseAtualMataMata() == PartidaMataMataEntity.FaseMataMata.FINAL && golsMandante == golsVisitante) {
                if (new Random().nextBoolean()) golsMandante++;
                else golsVisitante++;
            }

            String resultado = determinarResultado(golsMandante, golsVisitante).name();

            partida.aplicarResultado(golsMandante, golsVisitante, resultado);

            if (partida instanceof PartidaEntity partidaNormal) {
                partidaRepository.save(partidaNormal);
            } else if (partida instanceof PartidaMataMataEntity partidaMataMata) {
                partidaMataMataRepository.save(partidaMataMata);
            }

            Long grupoDefinido = null;

            if (campeonato.getTipo() == MATA_MATA){
                grupoDefinido = grupo.get(grupo.size()-1).getId();
            }

            atualizarTabela(campeonato, partida.getMandante(), golsMandante, golsVisitante,
                    PartidaEntity.Resultado.valueOf(resultado), true, grupoDefinido);
            atualizarTabela(campeonato, partida.getVisitante(), golsVisitante, golsMandante,
                    PartidaEntity.Resultado.valueOf(resultado), false, grupoDefinido);

            if(campeonato.getMataMataIniciado() == false){
                salvarPartida(partida.getId(), golsMandante, golsVisitante, resultado);
            }

            resultados.add(new SimulacaoResponseDTO(
                    partida.getMandante().getNome(),
                    golsMandante,
                    partida.getVisitante().getNome(),
                    golsVisitante,
                    resultado
            ));
        }

        rodada.setFinalizada(true);
        rodadaRepository.save(rodada);

        if (isUltimaRodada(rodada, campeonato) && campeonato.getTipo() == PONTOS_CORRIDOS) {
            definirCampeao(campeonato);
            campeonato.setEmAndamento(false);
        } else if (campeonato.getTipo() == MATA_MATA && isUltimaRodada(rodada, campeonato) &&
                campeonato.getFaseAtualMataMata() != PartidaMataMataEntity.FaseMataMata.FINAL) {
            gerarRodadasMataMataUseCase.gerarMataMata(campeonato);
        } else if (campeonato.getFaseAtualMataMata() == PartidaMataMataEntity.FaseMataMata.FINAL){
            definirCampeao(campeonato);
        }

        return resultados;
    }

    private EscalacaoEntity buscarOuGerarEscalacao(ClubeEntity clube) {
        if (clube.getTecnico() != null) {
            return escalacaoRepository.findTopByClubeOrderByDataHoraDesc(clube)
                    .orElseThrow(() -> new NaoEncontradoException("Escalação não encontrada para clube: " + clube.getNome() + ", cadastre uma escalação para seguir"));
        }
        return gerarEscalacaoAutomatica(clube);
    }

    private EscalacaoEntity gerarEscalacaoAutomatica(ClubeEntity clube) {
        EscalacaoEntity escalacao = new EscalacaoEntity();
        escalacao.setClube(clube);
        escalacao.setDataHora(LocalDateTime.now());
        escalacao.setFormacao("4-4-2");

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
            ClubeEntity clube,
            int golsPro,
            int golsContra,
            PartidaEntity.Resultado resultado,
            boolean isMandante,
            Long grupo
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

        if (campeonato.getTipo() == MATA_MATA){

            tabela.setGrupo(grupo);
        }

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
            ClubeEntity campeao = classificacao.get(0).getClube();
            campeonato.setCampeao(campeao);
            campeonato.setEmAndamento(false);
            campeonatoRepository.save(campeonato);
        }
    }

    private boolean isUltimaRodada(RodadaEntity rodada, CampeonatoEntity campeonato) {
        int totalRodadas = campeonato.getRodadas().size();
        return rodada.getNumero() == totalRodadas;
    }

    private void salvarPartida(Long partidaId,
                               int golsMandante,
                               int golsVisitante,
                               String resultado) {

        PartidaEntity partida = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new NaoEncontradoException("Partida não encontrada com id: " + partidaId));

        partida.setGolsMandante(golsMandante);
        partida.setGolsVisitante(golsVisitante);
        partida.setResultado(PartidaEntity.Resultado.valueOf(resultado));
        partida.setDataHora(LocalDateTime.now());

        partidaRepository.save(partida);
    }

}