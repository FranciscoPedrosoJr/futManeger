package com.futmaneger.domain.entity;

import com.futmaneger.infrastructure.persistence.entity.TecnicoEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "clubes")
public class Clube {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String cidade;

    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private TecnicoEntity tecnico;

    public Clube() {}

    public Clube(String nome, String cidade) {
        this.nome = nome;
        this.cidade = cidade;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCidade() {
        return cidade;
    }

    public void setTecnico(TecnicoEntity tecnico) {
        this.tecnico = tecnico;
    }

    public TecnicoEntity getTecnico() {
        return tecnico;
    }

    public Clube getClube(Clube clube) {
        return clube;
    }
}
