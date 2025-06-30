package com.futmaneger.infrastructure.persistence.entity;

import com.futmaneger.domain.entity.Clube;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tabela_campeonato")
public class TabelaCampeonatoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "campeonato_id")
    private CampeonatoEntity campeonato;

    @ManyToOne
    @JoinColumn(name = "clube_id")
    private Clube clube;

    private int pontos;
    private int vitorias;
    private int empates;
    private int derrotas;
    private int golsPro;
    private int golsContra;
    private int saldoGols;
    private int jogos;

    public CampeonatoEntity setCampeonato(CampeonatoEntity campeonato) {
        return this.campeonato = campeonato;
    }

    public Clube setClube(Clube clube) {
        return this.clube = clube;
    }

    public int getJogos() {
        return jogos;
    }

    public int setJogos(int i) {
        return this.jogos = jogos;
    }

    public int getGolsPro() {
        return golsPro;
    }

    public int setGolsPro(int i) {
        return this.golsPro = golsPro;
    }

    public int getGolsContra() {
        return golsContra;
    }

    public int setGolsContra(int i) {
        return this.golsContra = golsContra;
    }

    public int getSaldoGols() {
        return saldoGols;
    }

    public int setSaldoGols(int i) {
        return this.saldoGols = saldoGols;
    }

    public int getVitorias() {
        return vitorias;
    }

    public int setVitorias(int i) {
        return this.vitorias = vitorias;
    }

    public int getPontos() {
        return pontos;
    }

    public int setPontos(int i) {
        return this.pontos = pontos;
    }

    public int getDerrotas() {
        return derrotas;
    }

    public int setDerrotas(int i) {
        return this.derrotas = derrotas;
    }

    public int getEmpates() {
        return empates;
    }

    public int setEmpates(int i) {
        return this.empates = empates;
    }
}
