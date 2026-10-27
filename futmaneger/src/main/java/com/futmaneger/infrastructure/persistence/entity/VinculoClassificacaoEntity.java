package com.futmaneger.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "vinculos_classificacao")
public class VinculoClassificacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "campeonato_origem_id")
    private CampeonatoEntity campeonatoOrigem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "campeonato_destino_id")
    private CampeonatoEntity campeonatoDestino;

    @Column(nullable = false)
    private int quantidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SentidoClassificacao sentido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CriterioClassificacao criterio;

    public VinculoClassificacaoEntity() {
    }

    public VinculoClassificacaoEntity(CampeonatoEntity campeonatoOrigem, CampeonatoEntity campeonatoDestino,
                                      int quantidade, SentidoClassificacao sentido, CriterioClassificacao criterio) {
        this.campeonatoOrigem = campeonatoOrigem;
        this.campeonatoDestino = campeonatoDestino;
        this.quantidade = quantidade;
        this.sentido = sentido;
        this.criterio = criterio;
    }

    public Long getId() {
        return id;
    }

    public CampeonatoEntity getCampeonatoOrigem() {
        return campeonatoOrigem;
    }

    public void setCampeonatoOrigem(CampeonatoEntity campeonatoOrigem) {
        this.campeonatoOrigem = campeonatoOrigem;
    }

    public CampeonatoEntity getCampeonatoDestino() {
        return campeonatoDestino;
    }

    public void setCampeonatoDestino(CampeonatoEntity campeonatoDestino) {
        this.campeonatoDestino = campeonatoDestino;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public SentidoClassificacao getSentido() {
        return sentido;
    }

    public void setSentido(SentidoClassificacao sentido) {
        this.sentido = sentido;
    }

    public CriterioClassificacao getCriterio() {
        return criterio;
    }

    public void setCriterio(CriterioClassificacao criterio) {
        this.criterio = criterio;
    }
}
