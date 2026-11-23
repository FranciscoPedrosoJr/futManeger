package com.futmaneger.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "estatisticas_jogador_partida",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_estatistica_partida_jogador",
                columnNames = {"partida_mata_mata", "partida_id", "jogador_id"}),
        indexes = {
                @Index(
                        name = "idx_estatistica_partida",
                        columnList = "partida_mata_mata,partida_id"),
                @Index(
                        name = "idx_estatistica_jogador",
                        columnList = "jogador_id")
        })
public class EstatisticaJogadorPartidaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long partidaId;
    private boolean partidaMataMata;
    private Long jogadorId;
    private String nomeJogador;
    private double nota;
    private int gols;
    private int cartoesAmarelos;
    private int cartoesVermelhos;

    public Long getPartidaId() {
        return partidaId;
    }

    public void setPartidaId(Long partidaId) {
        this.partidaId = partidaId;
    }

    public boolean isPartidaMataMata() {
        return partidaMataMata;
    }

    public void setPartidaMataMata(boolean partidaMataMata) {
        this.partidaMataMata = partidaMataMata;
    }

    public Long getJogadorId() {
        return jogadorId;
    }

    public void setJogadorId(Long jogadorId) {
        this.jogadorId = jogadorId;
    }

    public String getNomeJogador() {
        return nomeJogador;
    }

    public void setNomeJogador(String nomeJogador) {
        this.nomeJogador = nomeJogador;
    }

    public double getNota() {
        return nota;
    }

    public void setNota(double nota) {
        this.nota = nota;
    }

    public int getGols() {
        return gols;
    }

    public void setGols(int gols) {
        this.gols = gols;
    }

    public int getCartoesAmarelos() {
        return cartoesAmarelos;
    }

    public void setCartoesAmarelos(int cartoesAmarelos) {
        this.cartoesAmarelos = cartoesAmarelos;
    }

    public int getCartoesVermelhos() {
        return cartoesVermelhos;
    }

    public void setCartoesVermelhos(int cartoesVermelhos) {
        this.cartoesVermelhos = cartoesVermelhos;
    }
}
