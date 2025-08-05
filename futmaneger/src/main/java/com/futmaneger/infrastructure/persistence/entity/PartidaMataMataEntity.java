package com.futmaneger.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class PartidaMataMataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "campeonato_id")
    private CampeonatoEntity campeonato;

    private int fase;

    @ManyToOne
    @JoinColumn(name = "clube_a_id")
    private ClubeEntity clubeA;

    @ManyToOne
    @JoinColumn(name = "clube_b_id")
    private ClubeEntity clubeB;

    private int golsClubeA;
    private int golsClubeB;

    private boolean jogoDeVolta;
    private boolean foiParaPenaltis;
    private int golsPenaltisA;
    private int golsPenaltisB;

    private boolean finalizada;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CampeonatoEntity getCampeonato() {
        return campeonato;
    }

    public void setCampeonato(CampeonatoEntity campeonato) {
        this.campeonato = campeonato;
    }

    public int getFase() {
        return fase;
    }

    public void setFase(int fase) {
        this.fase = fase;
    }

    public ClubeEntity getClubeA() {
        return clubeA;
    }

    public void setClubeMandante(ClubeEntity clubeA) {
        this.clubeA = clubeA;
    }

    public ClubeEntity getClubeB() {
        return clubeB;
    }

    public void setClubeVisitante(ClubeEntity clubeB) {
        this.clubeB = clubeB;
    }

    public int getGolsClubeA() {
        return golsClubeA;
    }

    public void setGolsMandante(int golsClubeA) {
        this.golsClubeA = golsClubeA;
    }

    public int getGolsClubeB() {
        return golsClubeB;
    }

    public void setGolsVisitante(int golsClubeB) {
        this.golsClubeB = golsClubeB;
    }

    public boolean isJogoDeVolta() {
        return jogoDeVolta;
    }

    public void setJogoDeVolta(boolean jogoDeVolta) {
        this.jogoDeVolta = jogoDeVolta;
    }

    public boolean isFoiParaPenaltis() {
        return foiParaPenaltis;
    }

    public void setFoiParaPenaltis(boolean foiParaPenaltis) {
        this.foiParaPenaltis = foiParaPenaltis;
    }

    public int getGolsPenaltisA() {
        return golsPenaltisA;
    }

    public void setGolsPenaltisA(int golsPenaltisA) {
        this.golsPenaltisA = golsPenaltisA;
    }

    public int getGolsPenaltisB() {
        return golsPenaltisB;
    }

    public void setGolsPenaltisB(int golsPenaltisB) {
        this.golsPenaltisB = golsPenaltisB;
    }

    public boolean isFinalizada() {
        return finalizada;
    }

    public void setFinalizada(boolean finalizada) {
        this.finalizada = finalizada;
    }
}
