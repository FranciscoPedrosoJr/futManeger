package com.futmaneger.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Tabela de lookup que associa um país ao seu continente. É consultada pelo
 * nome do país (o mesmo valor armazenado em {@link ClubeEntity#getPais()}),
 * mantendo o continente como fonte única da verdade sem duplicar o dado em
 * cada clube.
 */
@Entity
@Table(name = "paises")
public class PaisEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Continente continente;

    public PaisEntity() {
    }

    public PaisEntity(String nome, Continente continente) {
        this.nome = nome;
        this.continente = continente;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Continente getContinente() {
        return continente;
    }

    public void setContinente(Continente continente) {
        this.continente = continente;
    }
}
