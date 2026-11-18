package com.futmaneger.application.usecase.financeiro;

import com.futmaneger.application.dto.PainelFinanceiroClubeResponseDTO.CicloPadrao;
import com.futmaneger.application.dto.PainelFinanceiroClubeResponseDTO.ItemProjecao;
import com.futmaneger.application.dto.PainelFinanceiroClubeResponseDTO.Premissas;
import com.futmaneger.application.dto.PainelFinanceiroClubeResponseDTO.PremioPotencial;
import com.futmaneger.application.dto.PainelFinanceiroClubeResponseDTO.Projecao;
import com.futmaneger.application.dto.PainelFinanceiroClubeResponseDTO.ProjecaoMensal;
import com.futmaneger.infrastructure.config.FinanceiroProperties;
import com.futmaneger.infrastructure.persistence.entity.CategoriaTransacao;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeParticipanteEntity;
import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.EventoCalendarioEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaEntity;
import com.futmaneger.infrastructure.persistence.entity.PartidaMataMataEntity;
import com.futmaneger.infrastructure.persistence.entity.StatusEventoCalendario;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeParticipanteRepository;
import com.futmaneger.infrastructure.persistence.jpa.EventoCalendarioRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaMataMataRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TemporadaJpaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.springframework.stereotype.Service;

@Service
public class ProjecaoFinanceiraService {

    private final TemporadaJpaRepository temporadaRepository;
    private final EventoCalendarioRepository eventoRepository;
    private final ClubeParticipanteRepository participanteRepository;
    private final PartidaRepository partidaRepository;
    private final PartidaMataMataRepository partidaMataMataRepository;
    private final BilheteriaService bilheteriaService;
    private final SalarioService salarioService;
    private final ReceitaInstitucionalService receitaInstitucionalService;
    private final FinanceiroProperties props;

    public ProjecaoFinanceiraService(TemporadaJpaRepository temporadaRepository,
                                     EventoCalendarioRepository eventoRepository,
                                     ClubeParticipanteRepository participanteRepository,
                                     PartidaRepository partidaRepository,
                                     PartidaMataMataRepository partidaMataMataRepository,
                                     BilheteriaService bilheteriaService,
                                     SalarioService salarioService,
                                     ReceitaInstitucionalService receitaInstitucionalService,
                                     FinanceiroProperties props) {
        this.temporadaRepository = temporadaRepository;
        this.eventoRepository = eventoRepository;
        this.participanteRepository = participanteRepository;
        this.partidaRepository = partidaRepository;
        this.partidaMataMataRepository = partidaMataMataRepository;
        this.bilheteriaService = bilheteriaService;
        this.salarioService = salarioService;
        this.receitaInstitucionalService = receitaInstitucionalService;
        this.props = props;
    }

    public CicloPadrao cicloPadrao(BigDecimal valorElenco, BigDecimal bilheteriaMediaPorJogo) {
        BigDecimal cotaTv = receitaInstitucionalService.calcularCotaTv(valorElenco);
        BigDecimal patrocinio = receitaInstitucionalService.calcularPatrocinio(valorElenco);
        BigDecimal salario = salarioService.calcularFolha(valorElenco);
        BigDecimal mediaJogo = bilheteriaMediaPorJogo == null ? BigDecimal.ZERO : bilheteriaMediaPorJogo;
        BigDecimal jogosEmCasa = BigDecimal.valueOf(Math.max(1, props.getRodadasPorCiclo() / 2));
        BigDecimal bilheteriaCiclo = mediaJogo.multiply(jogosEmCasa);
        BigDecimal resultado = cotaTv.add(patrocinio).add(bilheteriaCiclo).subtract(salario);
        return new CicloPadrao(cotaTv, patrocinio, salario, bilheteriaMediaPorJogo, bilheteriaCiclo, resultado);
    }

    public Projecao projetar(ClubeEntity clube, BigDecimal saldoAtual, BigDecimal valorElenco,
                             double forcaMedia, int mesesProjecao, BigDecimal bilheteriaMediaPorJogo) {
        CicloPadrao ciclo = cicloPadrao(valorElenco, bilheteriaMediaPorJogo);
        Premissas premissas = new Premissas(props.getRodadasPorCiclo(), mesesProjecao, valorElenco, forcaMedia,
                props.getPercentualSalario(), props.getPercentualCotaTv(), props.getPercentualPatrocinio(),
                bilheteriaService.precoEfetivo(clube));
        List<ClubeParticipanteEntity> participacoes = participanteRepository.findByClube(clube);
        List<PremioPotencial> premios = premiosPotenciais(participacoes);

        TemporadaEntity temporada = temporadaRepository
                .findFirstByEstadoOrderByAnoDesc(TemporadaEntity.EstadoTemporada.EM_ANDAMENTO).orElse(null);
        if (temporada == null) {
            return indisponivel("Nenhuma temporada em andamento", premissas, saldoAtual, premios, ciclo);
        }
        if (!temporada.isCalendarioGerado()) {
            return indisponivel("A temporada em andamento nao possui calendario gerado", premissas, saldoAtual,
                    premios, ciclo);
        }

        Set<Long> edicoesDoClube = new HashSet<>();
        participacoes.forEach(p -> edicoesDoClube.add(p.getEdicao().getId()));
        List<EventoCalendarioEntity> abertos = eventoRepository
                .findByTemporadaOrderByDataAscOrdemAscIdAsc(temporada).stream()
                .filter(e -> e.getEdicao() != null && edicoesDoClube.contains(e.getEdicao().getId()))
                .filter(e -> e.getStatus() != null && StatusEventoCalendario.abertos().contains(e.getStatus()))
                .toList();

        LocalDate hoje = temporada.getDataAtual() != null ? temporada.getDataAtual() : LocalDate.now();
        LocalDate limite = hoje.plusMonths(mesesProjecao);

        List<ItemProjecao> brutos = new ArrayList<>();
        brutos.addAll(projetarBilheteria(clube, abertos, forcaMedia));
        brutos.addAll(projetarCiclos(abertos, ciclo));

        List<ItemProjecao> ordenados = brutos.stream()
                .filter(i -> !i.data().isAfter(limite))
                .sorted(Comparator.comparing(ItemProjecao::data)
                        .thenComparing(i -> i.valor().signum() < 0))
                .toList();

        List<ItemProjecao> itens = new ArrayList<>();
        BigDecimal saldo = saldoAtual;
        BigDecimal receitas = BigDecimal.ZERO;
        BigDecimal despesas = BigDecimal.ZERO;
        Map<String, BigDecimal[]> meses = new TreeMap<>();
        for (ItemProjecao i : ordenados) {
            saldo = saldo.add(i.valor());
            itens.add(new ItemProjecao(i.data(), i.categoria(), i.valor(), i.descricao(), saldo));
            BigDecimal[] acc = meses.computeIfAbsent(YearMonth.from(i.data()).toString(),
                    k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
            if (i.valor().signum() >= 0) {
                receitas = receitas.add(i.valor());
                acc[0] = acc[0].add(i.valor());
            } else {
                despesas = despesas.add(i.valor().negate());
                acc[1] = acc[1].add(i.valor().negate());
            }
            acc[2] = saldo;
        }
        List<ProjecaoMensal> porMes = meses.entrySet().stream()
                .map(e -> new ProjecaoMensal(e.getKey(), e.getValue()[0], e.getValue()[1],
                        e.getValue()[0].subtract(e.getValue()[1]), e.getValue()[2]))
                .toList();
        String motivo = itens.isEmpty() ? "Nao ha eventos futuros do clube dentro do periodo" : null;
        return new Projecao(true, motivo, premissas, itens, porMes, saldo, receitas, despesas, premios, ciclo);
    }

    private Projecao indisponivel(String motivo, Premissas premissas, BigDecimal saldoAtual,
                                  List<PremioPotencial> premios, CicloPadrao ciclo) {
        return new Projecao(false, motivo, premissas, List.of(), List.of(), saldoAtual, BigDecimal.ZERO,
                BigDecimal.ZERO, premios, ciclo);
    }

    private List<ItemProjecao> projetarBilheteria(ClubeEntity clube, List<EventoCalendarioEntity> abertos,
                                                  double forcaClube) {
        List<Long> idsNormais = new ArrayList<>();
        List<Long> idsMataMata = new ArrayList<>();
        for (EventoCalendarioEntity e : abertos) {
            if (e.getPartidaId() == null) {
                continue;
            }
            (e.isPartidaMataMata() ? idsMataMata : idsNormais).add(e.getPartidaId());
        }
        Map<Long, PartidaEntity> normais = new HashMap<>();
        partidaRepository.findAllById(idsNormais).forEach(p -> normais.put(p.getId(), p));
        Map<Long, PartidaMataMataEntity> mataMata = new HashMap<>();
        partidaMataMataRepository.findAllById(idsMataMata).forEach(p -> mataMata.put(p.getId(), p));

        Map<Long, Double> forcas = new HashMap<>();
        List<ItemProjecao> itens = new ArrayList<>();
        for (EventoCalendarioEntity e : abertos) {
            ClubeEntity mandante;
            ClubeEntity visitante;
            if (e.getPartidaId() == null) {
                continue;
            }
            if (e.isPartidaMataMata()) {
                PartidaMataMataEntity p = mataMata.get(e.getPartidaId());
                if (p == null) {
                    continue;
                }
                mandante = p.getMandante();
                visitante = p.getVisitante();
            } else {
                PartidaEntity p = normais.get(e.getPartidaId());
                if (p == null) {
                    continue;
                }
                mandante = p.getMandante();
                visitante = p.getVisitante();
            }
            if (mandante == null || visitante == null || !clube.getId().equals(mandante.getId())) {
                continue;
            }
            double forcaVisitante = forcas.computeIfAbsent(visitante.getId(),
                    id -> bilheteriaService.forca(visitante));
            BigDecimal receita = bilheteriaService.estimarReceita(clube, forcaClube, forcaVisitante);
            if (receita.signum() > 0) {
                itens.add(new ItemProjecao(e.getData(), CategoriaTransacao.BILHETERIA, receita,
                        "Bilheteria prevista: " + mandante.getNome() + " x " + visitante.getNome(), null));
            }
        }
        return itens;
    }

    private List<ItemProjecao> projetarCiclos(List<EventoCalendarioEntity> abertos, CicloPadrao ciclo) {
        Map<Long, Map<Integer, LocalDate>> ultimaDataPorRodada = new HashMap<>();
        Map<Long, EdicaoCampeonatoEntity> edicoes = new HashMap<>();
        for (EventoCalendarioEntity e : abertos) {
            if (e.getOrdem() <= 0 || e.getOrdem() % props.getRodadasPorCiclo() != 0) {
                continue;
            }
            edicoes.put(e.getEdicao().getId(), e.getEdicao());
            ultimaDataPorRodada.computeIfAbsent(e.getEdicao().getId(), k -> new HashMap<>())
                    .merge(e.getOrdem(), e.getData(), (a, b) -> a.isAfter(b) ? a : b);
        }
        List<ItemProjecao> itens = new ArrayList<>();
        ultimaDataPorRodada.forEach((edicaoId, rodadas) -> {
            String nomeCampeonato = edicoes.get(edicaoId).getCampeonato().getNome();
            rodadas.forEach((rodada, data) -> {
                String ref = " (rodada " + rodada + " - " + nomeCampeonato + ")";
                itens.add(new ItemProjecao(data, CategoriaTransacao.COTA_TV, ciclo.cotaTv(),
                        "Cota de TV prevista" + ref, null));
                itens.add(new ItemProjecao(data, CategoriaTransacao.PATROCINIO, ciclo.patrocinio(),
                        "Patrocinio previsto" + ref, null));
                itens.add(new ItemProjecao(data, CategoriaTransacao.SALARIO, ciclo.salario().negate(),
                        "Folha salarial prevista" + ref, null));
            });
        });
        return itens;
    }

    private List<PremioPotencial> premiosPotenciais(List<ClubeParticipanteEntity> participacoes) {
        List<PremioPotencial> premios = new ArrayList<>();
        for (ClubeParticipanteEntity p : participacoes) {
            EdicaoCampeonatoEntity edicao = p.getEdicao();
            if (!edicao.getEmAndamento()) {
                continue;
            }
            BigDecimal campeao = edicao.getCampeonato().getPremiacaoCampeao();
            BigDecimal vice = edicao.getCampeonato().getPremiacaoVice();
            if ((campeao == null || campeao.signum() <= 0) && (vice == null || vice.signum() <= 0)) {
                continue;
            }
            premios.add(new PremioPotencial(edicao.getCampeonato().getId(), edicao.getCampeonato().getNome(),
                    campeao, vice));
        }
        return premios;
    }
}
