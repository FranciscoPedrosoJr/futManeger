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
@Table(name = "pre_classificados")
public class PreClassificadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "clube_id")
    private ClubeEntity clube;

    @ManyToOne(optional = false)
    @JoinColumn(name = "campeonato_destino_id")
    private CampeonatoEntity campeonatoDestino;

    @ManyToOne(optional = false)
    @JoinColumn(name = "temporada_origem_id")
    private TemporadaEntity temporadaOrigem;

    @Column(nullable = false)
    private boolean consumido;

    public PreClassificadoEntity() {
    }

    public PreClassificadoEntity(ClubeEntity clube, CampeonatoEntity campeonatoDestino,
                                 TemporadaEntity temporadaOrigem) {
        this.clube = clube;
        this.campeonatoDestino = campeonatoDestino;
        this.temporadaOrigem = temporadaOrigem;
        this.consumido = false;
    }

    public Long getId() {
        return id;
    }

    public ClubeEntity getClube() {
        return clube;
    }

    public void setClube(ClubeEntity clube) {
        this.clube = clube;
    }

    public CampeonatoEntity getCampeonatoDestino() {
        return campeonatoDestino;
    }

    public void setCampeonatoDestino(CampeonatoEntity campeonatoDestino) {
        this.campeonatoDestino = campeonatoDestino;
    }

    public TemporadaEntity getTemporadaOrigem() {
        return temporadaOrigem;
    }

    public void setTemporadaOrigem(TemporadaEntity temporadaOrigem) {
        this.temporadaOrigem = temporadaOrigem;
    }

    public boolean isConsumido() {
        return consumido;
    }

    public void setConsumido(boolean consumido) {
        this.consumido = consumido;
    }
}
