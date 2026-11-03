package com.futmaneger.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "participantes_fase")
public class ParticipanteFaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "edicao_id")
    private EdicaoCampeonatoEntity edicao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "clube_id")
    private ClubeEntity clube;

    @Column(nullable = false)
    private int ordemEntrada;

    public ParticipanteFaseEntity() {
    }

    public ParticipanteFaseEntity(EdicaoCampeonatoEntity edicao, ClubeEntity clube, int ordemEntrada) {
        this.edicao = edicao;
        this.clube = clube;
        this.ordemEntrada = ordemEntrada;
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

    public int getOrdemEntrada() {
        return ordemEntrada;
    }

    public void setOrdemEntrada(int ordemEntrada) {
        this.ordemEntrada = ordemEntrada;
    }
}
