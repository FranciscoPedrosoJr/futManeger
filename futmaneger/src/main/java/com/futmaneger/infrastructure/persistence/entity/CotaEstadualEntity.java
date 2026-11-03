package com.futmaneger.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "cotas_estaduais",
        uniqueConstraints = @UniqueConstraint(columnNames = {"campeonato_id", "estado"}))
public class CotaEstadualEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "campeonato_id")
    private CampeonatoEntity campeonato;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private int quantidade;

    private Integer faseDeEntradaOrdem;

    public CotaEstadualEntity() {
    }

    public CotaEstadualEntity(CampeonatoEntity campeonato, String estado, int quantidade) {
        this(campeonato, estado, quantidade, null);
    }

    public CotaEstadualEntity(CampeonatoEntity campeonato, String estado, int quantidade, Integer faseDeEntradaOrdem) {
        this.campeonato = campeonato;
        this.estado = estado;
        this.quantidade = quantidade;
        this.faseDeEntradaOrdem = faseDeEntradaOrdem;
    }

    public Long getId() {
        return id;
    }

    public CampeonatoEntity getCampeonato() {
        return campeonato;
    }

    public void setCampeonato(CampeonatoEntity campeonato) {
        this.campeonato = campeonato;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public Integer getFaseDeEntradaOrdem() {
        return faseDeEntradaOrdem;
    }

    public void setFaseDeEntradaOrdem(Integer faseDeEntradaOrdem) {
        this.faseDeEntradaOrdem = faseDeEntradaOrdem;
    }
}
