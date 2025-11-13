package com.futmaneger.domain.entity;

import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.TecnicoEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

public class Clube {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String estado;

    private String pais;

    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private TecnicoEntity tecnico;

    public Clube() {}

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEstado() {
        return estado;
    }

    @Column(nullable = false)
    public String getPais() {
        return pais;
    }

    public TecnicoEntity getTecnico() {
        return tecnico;
    }

    public ClubeEntity getClube(ClubeEntity clube) {
        return clube;
    }
}
