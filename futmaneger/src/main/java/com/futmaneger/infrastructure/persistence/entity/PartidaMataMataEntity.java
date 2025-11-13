package com.futmaneger.infrastructure.persistence.entity;

import com.futmaneger.domain.entity.PartidaSimulavel;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class PartidaMataMataEntity implements PartidaSimulavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "campeonato_id")
    private CampeonatoEntity campeonato;

    @Enumerated(EnumType.STRING)
    private FaseMataMata fase;

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

    @ManyToOne
    @JoinColumn(name = "rodada_id")
    private RodadaEntity rodada;

    public void setRodada(RodadaEntity rodada) {
        this.rodada = rodada;
    }

    public enum FaseMataMata {
        OITAVAS, QUARTAS, SEMIFINAL, FINAL
    }

    @Override
    public ClubeEntity getMandante() {
        return clubeA;
    }

    @Override
    public ClubeEntity getVisitante() {
        return clubeB;
    }

    @Override
    public boolean isFinalizada() {
        return finalizada;
    }

    @Override
    public void setFinalizada(boolean finalizada) {
        this.finalizada = finalizada;
    }

    @Override
    public void aplicarResultado(int golsMandante, int golsVisitante, String resultado) {
        this.golsClubeA = golsMandante;
        this.golsClubeB = golsVisitante;
        this.finalizada = true;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CampeonatoEntity getCampeonato() {
        return campeonato;
    }

    public CampeonatoEntity setCampeonato(CampeonatoEntity campeonato) {
        this.campeonato = campeonato;
        return campeonato;
    }

    public FaseMataMata getFase() {
        return fase;
    }

    public void setFase(FaseMataMata fase) {
        this.fase = fase;
    }

    public ClubeEntity getClubeA() {
        return clubeA;
    }

    public ClubeEntity setClubeMandante(ClubeEntity clubeA) {
        this.clubeA = clubeA;
        return clubeA;
    }

    public ClubeEntity getClubeB() {
        return clubeB;
    }

    public ClubeEntity setClubeVisitante(ClubeEntity clubeB) {
        this.clubeB = clubeB;
        return clubeB;
    }

    public int getGolsClubeA() {
        return golsClubeA;
    }

    public int setGolsMandante(int golsClubeA) {
        this.golsClubeA = golsClubeA;
        return golsClubeA;
    }

    public int getGolsClubeB() {
        return golsClubeB;
    }

    public int setGolsVisitante(int golsClubeB) {
        this.golsClubeB = golsClubeB;
        return golsClubeB;
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

}
