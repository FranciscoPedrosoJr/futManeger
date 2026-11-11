package com.futmaneger.application.dto;

import com.futmaneger.infrastructure.persistence.entity.CategoriaTransacao;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PainelFinanceiroClubeResponseDTO(
        ClubeInfo clube,
        LocalDate dataAtualDoJogo,
        Resumo resumoGeral,
        List<PorCategoria> porCategoria,
        List<PorTemporada> porTemporada,
        List<EvolucaoMensal> evolucaoMensal,
        List<Transacao> ultimasTransacoes,
        Elenco elenco,
        Endividamento endividamento,
        Projecao projecao
) {

    public record ClubeInfo(Long id, String nome, BigDecimal saldoAtual, BigDecimal saldoAntesDosRegistros,
                            BigDecimal precoIngresso, BigDecimal precoEfetivo, boolean usaPrecoPadrao,
                            boolean temTecnico) {
    }

    public record Resumo(BigDecimal totalReceitas, BigDecimal totalDespesas, BigDecimal resultado,
                         long quantidade, LocalDate primeiraData, LocalDate ultimaData) {
    }

    public record PorCategoria(CategoriaTransacao categoria, BigDecimal receitas, BigDecimal despesas,
                               BigDecimal liquido, long quantidade) {
    }

    public record PorTemporada(Long temporadaId, Integer ano, BigDecimal receitas, BigDecimal despesas,
                               BigDecimal resultado, long quantidade) {
    }

    public record EvolucaoMensal(String mes, BigDecimal receitas, BigDecimal despesas, BigDecimal resultado,
                                 BigDecimal saldoAcumulado, Map<CategoriaTransacao, BigDecimal> liquidoPorCategoria) {
    }

    public record Transacao(UUID id, BigDecimal valor, CategoriaTransacao categoria, String descricao,
                            Long jogadorId, LocalDateTime data, LocalDate dataJogo, Long temporadaId,
                            BigDecimal saldoApos) {
    }

    public record Elenco(int quantidade, BigDecimal valorTotal, BigDecimal valorMedio, double forcaMedia,
                         BigDecimal folhaPorCiclo, BigDecimal cotaTvPorCiclo, BigDecimal patrocinioPorCiclo,
                         List<JogadorSalario> maioresSalarios) {
    }

    public record JogadorSalario(Long id, String nome, String posicao, Integer forca, BigDecimal valorMercado,
                                 BigDecimal salarioPorCiclo) {
    }

    public record Endividamento(BigDecimal divida, BigDecimal limite, BigDecimal margem,
                                double percentualDoLimiteUsado, String risco, boolean tecnicoPodeSerDemitido) {
    }

    public record Projecao(boolean disponivel, String motivo, Premissas premissas, List<ItemProjecao> itens,
                           List<ProjecaoMensal> porMes, BigDecimal saldoFinalProjetado,
                           BigDecimal totalReceitas, BigDecimal totalDespesas,
                           List<PremioPotencial> premiosPotenciais, CicloPadrao cicloPadrao) {
    }

    public record Premissas(int rodadasPorCiclo, int mesesProjecao, BigDecimal valorElenco, double forcaMedia,
                            BigDecimal percentualSalario, BigDecimal percentualCotaTv,
                            BigDecimal percentualPatrocinio, BigDecimal precoIngressoEfetivo) {
    }

    public record ItemProjecao(LocalDate data, CategoriaTransacao categoria, BigDecimal valor, String descricao,
                               BigDecimal saldoProjetado) {
    }

    public record ProjecaoMensal(String mes, BigDecimal receitas, BigDecimal despesas, BigDecimal resultado,
                                 BigDecimal saldoProjetado) {
    }

    public record PremioPotencial(Long campeonatoId, String campeonato, BigDecimal campeao, BigDecimal vice) {
    }

    public record CicloPadrao(BigDecimal cotaTv, BigDecimal patrocinio, BigDecimal salario,
                              BigDecimal bilheteriaMediaPorJogo, BigDecimal bilheteriaEstimadaPorCiclo,
                              BigDecimal resultadoEstimado) {
    }
}
