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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "fases_campeonato",
        uniqueConstraints = @UniqueConstraint(columnNames = {"campeonato_id", "ordem"}))
public class FaseCampeonatoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "campeonato_id")
    private CampeonatoEntity campeonato;

    @Column(nullable = false)
    private int ordem;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormatoFase formato;

    public FaseCampeonatoEntity() {
    }

    public FaseCampeonatoEntity(CampeonatoEntity campeonato, int ordem, String nome, FormatoFase formato) {
        this.campeonato = campeonato;
        this.ordem = ordem;
        this.nome = nome;
        this.formato = formato;
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

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public FormatoFase getFormato() {
        return formato;
    }

    public void setFormato(FormatoFase formato) {
        this.formato = formato;
    }
}
