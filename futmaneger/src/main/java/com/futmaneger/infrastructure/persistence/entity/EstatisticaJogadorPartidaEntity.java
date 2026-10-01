package com.futmaneger.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "estatisticas_jogador_partida")
public class EstatisticaJogadorPartidaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long partidaId;
    private boolean partidaMataMata;
    private Long jogadorId;
    private String nomeJogador;
    private double nota;
    private int gols;
    private int cartoesAmarelos;
    private int cartoesVermelhos;
    public Long getPartidaId() { return partidaId; }
    public void setPartidaId(Long partidaId) { this.partidaId = partidaId; }
    public boolean isPartidaMataMata() { return partidaMataMata; }
    public void setPartidaMataMata(boolean partidaMataMata) { this.partidaMataMata = partidaMataMata; }
    public Long getJogadorId() { return jogadorId; }
    public void setJogadorId(Long jogadorId) { this.jogadorId = jogadorId; }
    public String getNomeJogador() { return nomeJogador; }
    public void setNomeJogador(String nomeJogador) { this.nomeJogador = nomeJogador; }
    public double getNota() { return nota; }
    public void setNota(double nota) { this.nota = nota; }
    public int getGols() { return gols; }
    public void setGols(int gols) { this.gols = gols; }
    public int getCartoesAmarelos() { return cartoesAmarelos; }
    public void setCartoesAmarelos(int cartoesAmarelos) { this.cartoesAmarelos = cartoesAmarelos; }
    public int getCartoesVermelhos() { return cartoesVermelhos; }
    public void setCartoesVermelhos(int cartoesVermelhos) { this.cartoesVermelhos = cartoesVermelhos; }
}