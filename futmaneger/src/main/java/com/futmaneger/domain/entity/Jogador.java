package com.futmaneger.domain.entity;

import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "jogadores")
public class Jogador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String posicao;
    private int forca;
    private boolean diferenciado;
    private boolean identificacaoComClube;
    private BigDecimal valorMercado;

    @ManyToOne
    @JoinColumn(name = "clube_id", nullable = false)
    private ClubeEntity clube;

    public Jogador(String nome, String posicao, int forca, boolean diferenciado, boolean identificacaoComClube, ClubeEntity clube) {
        this.nome = nome;
        this.posicao = posicao;
        this.forca = forca;
        this.diferenciado = diferenciado;
        this.identificacaoComClube = identificacaoComClube;
        this.clube = clube;
    }

    public Jogador() {

    }


    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getPosicao() {
        return posicao;
    }

    public int getForca() {
        return forca;
    }

    public boolean isDiferenciado() {
        return diferenciado;
    }

    public boolean isIdentificacaoComClube() {
        return identificacaoComClube;
    }

    public Long getClubeId() {
        return clube.getId();
    }

    public ClubeEntity getClube() {
        return clube;
    }

    public ClubeEntity getClubeNome() {
        return clube;
    }

    public void setNome(String nome) {
    }

    public void setPosicao(String posicao) {
    }

    public void setForca(int forca) {
    }

    public void setDiferenciado(boolean diferenciado) {
    }

    public void setIdentificacaoComClube(boolean identificacaoComClube) {
    }

    public void setClube(ClubeEntity clube) {
        this.clube = clube;
    }

    public void setValorMercado(BigDecimal valorMercado) { this.valorMercado = valorMercado; }

    public BigDecimal getValorMercado() {
        return valorMercado;
    }
}
