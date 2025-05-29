package com.futmaneger.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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

    @ManyToOne
    @JoinColumn(name = "clube_id", nullable = false)
    private Clube clube;

    public Jogador(String nome, String posicao, int forca, boolean diferenciado, boolean identificacaoComClube, Clube clube) {
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

    public Long getClube() {
        return clube.getId();
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

    public void setClube(Clube clube) {
    }
}
