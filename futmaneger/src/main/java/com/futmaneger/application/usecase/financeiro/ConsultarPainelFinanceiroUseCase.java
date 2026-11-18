package com.futmaneger.application.usecase.financeiro;

import com.futmaneger.application.dto.PainelFinanceiroClubeResponseDTO;
import com.futmaneger.application.dto.PainelFinanceiroClubeResponseDTO.ClubeInfo;
import com.futmaneger.application.dto.PainelFinanceiroClubeResponseDTO.Elenco;
import com.futmaneger.application.dto.PainelFinanceiroClubeResponseDTO.Endividamento;
import com.futmaneger.application.dto.PainelFinanceiroClubeResponseDTO.EvolucaoMensal;
import com.futmaneger.application.dto.PainelFinanceiroClubeResponseDTO.JogadorSalario;
import com.futmaneger.application.dto.PainelFinanceiroClubeResponseDTO.PorCategoria;
import com.futmaneger.application.dto.PainelFinanceiroClubeResponseDTO.PorTemporada;
import com.futmaneger.application.dto.PainelFinanceiroClubeResponseDTO.Resumo;
import com.futmaneger.application.dto.PainelFinanceiroClubeResponseDTO.Transacao;
import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.application.usecase.calendario.ClubeDoUsuarioService;
import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.domain.repository.JogadorRepository;
import com.futmaneger.infrastructure.config.FinanceiroProperties;
import com.futmaneger.infrastructure.persistence.entity.CategoriaTransacao;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import com.futmaneger.infrastructure.persistence.entity.TransacaoFinanceiraEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TemporadaJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TransacaoFinanceiraRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultarPainelFinanceiroUseCase {

    private static final int MAX_TRANSACOES = 500;
    private static final int MAX_MESES = 24;

    private final ClubeJpaRepository clubeRepository;
    private final JogadorRepository jogadorRepository;
    private final TransacaoFinanceiraRepository transacaoRepository;
    private final TemporadaJpaRepository temporadaRepository;
    private final ClubeDoUsuarioService clubeDoUsuarioService;
    private final BilheteriaService bilheteriaService;
    private final ProjecaoFinanceiraService projecaoService;
    private final SalarioService salarioService;
    private final ReceitaInstitucionalService receitaService;
    private final FinanceiroProperties props;

    public ConsultarPainelFinanceiroUseCase(ClubeJpaRepository clubeRepository,
                                            JogadorRepository jogadorRepository,
                                            TransacaoFinanceiraRepository transacaoRepository,
                                            TemporadaJpaRepository temporadaRepository,
                                            ClubeDoUsuarioService clubeDoUsuarioService,
                                            BilheteriaService bilheteriaService,
                                            ProjecaoFinanceiraService projecaoService,
                                            SalarioService salarioService,
                                            ReceitaInstitucionalService receitaService,
                                            FinanceiroProperties props) {
        this.clubeRepository = clubeRepository;
        this.jogadorRepository = jogadorRepository;
        this.transacaoRepository = transacaoRepository;
        this.temporadaRepository = temporadaRepository;
        this.clubeDoUsuarioService = clubeDoUsuarioService;
        this.bilheteriaService = bilheteriaService;
        this.projecaoService = projecaoService;
        this.salarioService = salarioService;
        this.receitaService = receitaService;
        this.props = props;
    }

    @Transactional(readOnly = true)
    public PainelFinanceiroClubeResponseDTO executarParaUsuario(int ultimasTransacoes, int mesesProjecao) {
        ClubeEntity clube = clubeDoUsuarioService.obterClubeDoUsuarioAutenticado()
                .orElseThrow(() -> new DadosInvalidosException("O tecnico autenticado nao possui clube vinculado"));
        return montar(clube, ultimasTransacoes, mesesProjecao);
    }

    @Transactional(readOnly = true)
    public PainelFinanceiroClubeResponseDTO executar(Long clubeId, int ultimasTransacoes, int mesesProjecao) {
        ClubeEntity clube = clubeRepository.findById(clubeId)
                .orElseThrow(() -> new NaoEncontradoException("Clube nao encontrado"));
        return montar(clube, ultimasTransacoes, mesesProjecao);
    }

    private PainelFinanceiroClubeResponseDTO montar(ClubeEntity clube, int limiteTransacoes, int meses) {
        int qtdTransacoes = Math.max(0, Math.min(MAX_TRANSACOES, limiteTransacoes));
        int mesesProjecao = Math.max(1, Math.min(MAX_MESES, meses));

        BigDecimal saldo = nz(clube.getSaldo());
        List<TransacaoFinanceiraEntity> transacoes = new ArrayList<>(
                transacaoRepository.findByClubeIdOrderByDataAsc(clube.getId()));
        transacoes.sort(Comparator.comparing(this::dataEfetiva)
                .thenComparing(TransacaoFinanceiraEntity::getData,
                        Comparator.nullsFirst(Comparator.naturalOrder())));

        BigDecimal somaTransacoes = transacoes.stream().map(t -> nz(t.getValor()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldoAntes = saldo.subtract(somaTransacoes);

        TemporadaEntity emAndamento = temporadaRepository
                .findFirstByEstadoOrderByAnoDesc(TemporadaEntity.EstadoTemporada.EM_ANDAMENTO).orElse(null);
        LocalDate dataAtual = emAndamento == null ? null : emAndamento.getDataAtual();

        ClubeInfo info = new ClubeInfo(clube.getId(), clube.getNome(), saldo, saldoAntes,
                clube.getPrecoIngresso(), bilheteriaService.precoEfetivo(clube),
                bilheteriaService.usaPrecoPadrao(clube), clube.getTecnico() != null);

        List<Jogador> jogadores = jogadorRepository.findByClube(clube);
        BigDecimal valorElenco = receitaService.valorElenco(jogadores);
        double forcaMedia = jogadores.stream().mapToInt(Jogador::getForca).average().orElse(0.0);

        BigDecimal bilheteriaMedia = mediaBilheteria(transacoes);

        return new PainelFinanceiroClubeResponseDTO(
                info,
                dataAtual,
                resumo(transacoes),
                porCategoria(transacoes),
                porTemporada(transacoes),
                evolucaoMensal(transacoes, saldoAntes),
                ultimas(transacoes, saldoAntes, qtdTransacoes),
                elenco(jogadores, valorElenco, forcaMedia),
                endividamento(clube, saldo, valorElenco),
                projecaoService.projetar(clube, saldo, valorElenco, forcaMedia, mesesProjecao, bilheteriaMedia));
    }

    private Resumo resumo(List<TransacaoFinanceiraEntity> transacoes) {
        BigDecimal receitas = BigDecimal.ZERO;
        BigDecimal despesas = BigDecimal.ZERO;
        for (TransacaoFinanceiraEntity t : transacoes) {
            if (nz(t.getValor()).signum() >= 0) {
                receitas = receitas.add(nz(t.getValor()));
            } else {
                despesas = despesas.add(nz(t.getValor()).negate());
            }
        }
        LocalDate primeira = transacoes.isEmpty() ? null : dataEfetiva(transacoes.get(0));
        LocalDate ultima = transacoes.isEmpty() ? null : dataEfetiva(transacoes.get(transacoes.size() - 1));
        return new Resumo(receitas, despesas, receitas.subtract(despesas), transacoes.size(), primeira, ultima);
    }

    private List<PorCategoria> porCategoria(List<TransacaoFinanceiraEntity> transacoes) {
        Map<CategoriaTransacao, List<TransacaoFinanceiraEntity>> grupos = transacoes.stream()
                .filter(t -> t.getCategoria() != null)
                .collect(Collectors.groupingBy(TransacaoFinanceiraEntity::getCategoria,
                        () -> new EnumMap<>(CategoriaTransacao.class), Collectors.toList()));
        List<PorCategoria> lista = new ArrayList<>();
        grupos.forEach((cat, ts) -> {
            BigDecimal r = somaPositivos(ts);
            BigDecimal d = somaNegativos(ts);
            lista.add(new PorCategoria(cat, r, d, r.subtract(d), ts.size()));
        });
        return lista;
    }

    private List<PorTemporada> porTemporada(List<TransacaoFinanceiraEntity> transacoes) {
        Map<Long, List<TransacaoFinanceiraEntity>> grupos = new LinkedHashMap<>();
        for (TransacaoFinanceiraEntity t : transacoes) {
            grupos.computeIfAbsent(t.getTemporadaId(), k -> new ArrayList<>()).add(t);
        }
        Map<Long, Integer> anos = new LinkedHashMap<>();
        temporadaRepository.findAllById(grupos.keySet().stream().filter(k -> k != null).toList())
                .forEach(t -> anos.put(t.getId(), t.getAno()));
        List<PorTemporada> lista = new ArrayList<>();
        grupos.forEach((id, ts) -> {
            BigDecimal r = somaPositivos(ts);
            BigDecimal d = somaNegativos(ts);
            lista.add(new PorTemporada(id, id == null ? null : anos.get(id), r, d, r.subtract(d), ts.size()));
        });
        return lista;
    }

    private List<EvolucaoMensal> evolucaoMensal(List<TransacaoFinanceiraEntity> transacoes, BigDecimal saldoAntes) {
        Map<YearMonth, List<TransacaoFinanceiraEntity>> grupos = new TreeMap<>();
        for (TransacaoFinanceiraEntity t : transacoes) {
            grupos.computeIfAbsent(YearMonth.from(dataEfetiva(t)), k -> new ArrayList<>()).add(t);
        }
        List<EvolucaoMensal> lista = new ArrayList<>();
        BigDecimal acumulado = saldoAntes;
        for (Map.Entry<YearMonth, List<TransacaoFinanceiraEntity>> e : grupos.entrySet()) {
            BigDecimal r = somaPositivos(e.getValue());
            BigDecimal d = somaNegativos(e.getValue());
            acumulado = acumulado.add(r).subtract(d);
            Map<CategoriaTransacao, BigDecimal> porCat = new EnumMap<>(CategoriaTransacao.class);
            for (TransacaoFinanceiraEntity t : e.getValue()) {
                if (t.getCategoria() != null) {
                    porCat.merge(t.getCategoria(), nz(t.getValor()), BigDecimal::add);
                }
            }
            lista.add(new EvolucaoMensal(e.getKey().toString(), r, d, r.subtract(d), acumulado, porCat));
        }
        return lista;
    }

    private List<Transacao> ultimas(List<TransacaoFinanceiraEntity> transacoes, BigDecimal saldoAntes, int limite) {
        List<Transacao> todas = new ArrayList<>();
        BigDecimal saldo = saldoAntes;
        for (TransacaoFinanceiraEntity t : transacoes) {
            saldo = saldo.add(nz(t.getValor()));
            todas.add(new Transacao(t.getId(), t.getValor(), t.getCategoria(), t.getDescricao(), t.getJogadorId(),
                    t.getData(), t.getDataJogo(), t.getTemporadaId(), saldo));
        }
        int inicio = Math.max(0, todas.size() - limite);
        List<Transacao> recentes = new ArrayList<>(todas.subList(inicio, todas.size()));
        java.util.Collections.reverse(recentes);
        return recentes;
    }

    private Elenco elenco(List<Jogador> jogadores, BigDecimal valorElenco, double forcaMedia) {
        BigDecimal media = jogadores.isEmpty() ? BigDecimal.ZERO
                : valorElenco.divide(BigDecimal.valueOf(jogadores.size()), 0, RoundingMode.HALF_UP);
        List<JogadorSalario> maiores = jogadores.stream()
                .filter(j -> j.getValorMercado() != null)
                .sorted(Comparator.comparing(Jogador::getValorMercado).reversed())
                .limit(10)
                .map(j -> new JogadorSalario(j.getId(), j.getNome(), j.getPosicao(), j.getForca(),
                        j.getValorMercado(), salarioService.calcularFolha(j.getValorMercado())))
                .toList();
        return new Elenco(jogadores.size(), valorElenco, media, forcaMedia,
                salarioService.calcularFolha(valorElenco), receitaService.calcularCotaTv(valorElenco),
                receitaService.calcularPatrocinio(valorElenco), maiores);
    }

    private Endividamento endividamento(ClubeEntity clube, BigDecimal saldo, BigDecimal valorElenco) {
        BigDecimal divida = saldo.signum() < 0 ? saldo.negate() : BigDecimal.ZERO;
        BigDecimal limite = valorElenco.multiply(props.getLimiteDividaSobreElenco()).setScale(0, RoundingMode.HALF_UP);
        BigDecimal margem = limite.subtract(divida);
        double pct = limite.signum() > 0
                ? divida.multiply(BigDecimal.valueOf(100)).divide(limite, 2, RoundingMode.HALF_UP).doubleValue()
                : (divida.signum() > 0 ? 100.0 : 0.0);
        String risco;
        if (divida.signum() == 0) {
            risco = "SEM_DIVIDA";
        } else if (pct < 25) {
            risco = "BAIXO";
        } else if (pct < 50) {
            risco = "MEDIO";
        } else if (pct < 80) {
            risco = "ALTO";
        } else {
            risco = "CRITICO";
        }
        boolean demissivel = clube.getTecnico() != null && divida.compareTo(limite) > 0;
        return new Endividamento(divida, limite, margem, pct, risco, demissivel);
    }

    private BigDecimal mediaBilheteria(List<TransacaoFinanceiraEntity> transacoes) {
        List<BigDecimal> valores = transacoes.stream()
                .filter(t -> t.getCategoria() == CategoriaTransacao.BILHETERIA)
                .map(t -> nz(t.getValor())).toList();
        if (valores.isEmpty()) {
            return null;
        }
        return valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(valores.size()), 0, RoundingMode.HALF_UP);
    }

    private LocalDate dataEfetiva(TransacaoFinanceiraEntity t) {
        if (t.getDataJogo() != null) {
            return t.getDataJogo();
        }
        return t.getData() == null ? LocalDate.EPOCH : t.getData().toLocalDate();
    }

    private BigDecimal somaPositivos(List<TransacaoFinanceiraEntity> ts) {
        return ts.stream().map(t -> nz(t.getValor())).filter(v -> v.signum() > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal somaNegativos(List<TransacaoFinanceiraEntity> ts) {
        return ts.stream().map(t -> nz(t.getValor())).filter(v -> v.signum() < 0)
                .map(BigDecimal::negate).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
