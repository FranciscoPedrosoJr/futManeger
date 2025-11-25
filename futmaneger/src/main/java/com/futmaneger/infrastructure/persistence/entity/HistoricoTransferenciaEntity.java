package com.futmaneger.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historico_transferencias")
public class HistoricoTransferenciaEntity {

    @Id
    @GeneratedValue
    private UUID id;

    private Long jogadorId;
    private Long clubeVendedorId;
    private Long clubeCompradorId;

    private BigDecimal valor;
    private LocalDateTime data;

    public void setJogadorId(Long jogadorId) {
        this.jogadorId = jogadorId;
    }

    public Long getJogadorId(Long jogadorId){
        return jogadorId;
    }

    public void setClubeVendedorId(Long clubeVendedorId) {
        this.clubeVendedorId = clubeVendedorId;
    }

    public Long getClubeVendedorId(Long clubeVendedorId){
        return clubeVendedorId;
    }

    public void setClubeCompradorId(Long clubeCompradorId) {
        this.clubeCompradorId = clubeCompradorId;
    }

    public Long getClubeCompradorIdId(Long clubeCompradorId){
        return clubeCompradorId;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public BigDecimal getValor(BigDecimal valor){
        return valor;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public LocalDateTime getData(LocalDateTime data){
        return data;
    }
}
