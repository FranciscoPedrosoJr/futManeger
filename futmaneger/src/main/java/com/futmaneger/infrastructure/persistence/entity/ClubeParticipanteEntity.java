package com.futmaneger.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "clubes_participantes")
public class ClubeParticipanteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "edicao_id")
    private EdicaoCampeonatoEntity edicao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "clube_id")
    private ClubeEntity clube;

    private int pontos;

    public ClubeParticipanteEntity() {
    }

    public Long getId() {
        return id;
    }

    public EdicaoCampeonatoEntity getEdicao() {
        return edicao;
    }

    public void setEdicao(EdicaoCampeonatoEntity edicao) {
        this.edicao = edicao;
    }

    public ClubeEntity getClube() {
        return clube;
    }

    public void setClube(ClubeEntity clube) {
        this.clube = clube;
    }

    public int getPontos() {
        return pontos;
    }

    public void setPontos(int pontos) {
        this.pontos = pontos;
    }
}
