package com.futmaneger.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transacoes_financeiras")
public class TransacaoFinanceiraEntity {
    @Id
    @GeneratedValue
    private UUID id;
    private Long clubeId;
    private Long jogadorId;
    private BigDecimal valor;
    private String descricao;
    private LocalDateTime data;

    public void setClubeId(Long clubeId) {
        this.clubeId = clubeId;
    }
    public void setJogadorId(Long jogadorId) {
        this.jogadorId = jogadorId;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }
    public UUID getId() {
        return id;
    }
    public Long getClubeId() {
        return clubeId;
    }
    public Long getJogadorId() {
        return jogadorId;
    }
    public BigDecimal getValor() {
        return valor;
    }
    public String getDescricao() {
        return descricao;
    }
    public LocalDateTime getData() {
        return data;
    }
}
