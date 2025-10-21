package com.futmaneger.infrastructure.persistence.entity;

import com.futmaneger.domain.entity.PartidaSimulavel;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "partidas")
@Inheritance(strategy = InheritanceType.JOINED)
public class PartidaEntity implements PartidaSimulavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "clube_mandante_id")
    private ClubeEntity clubeMandante;

    @ManyToOne
    @JoinColumn(name = "clube_visitante_id")
    private ClubeEntity clubeVisitante;

    private int golsMandante;
    private int golsVisitante;

    @Enumerated(EnumType.STRING)
    private Resultado resultado;

    private LocalDateTime dataHora;

    //@ManyToOne
    @JoinColumn(name = "rodada_id")
    private int rodada;

    @ManyToOne
    private CampeonatoEntity campeonato;


    private boolean finalizada;

    public int getRodada() {
        return rodada;
    }

    public int setRodada(int rodada) {
        return this.rodada = rodada;
    }

    public ClubeEntity setClubeMandante(ClubeEntity mandante) {
        return this.clubeMandante = mandante;
    }

    public ClubeEntity setClubeVisitante(ClubeEntity visitante) {
        return this.clubeVisitante = visitante;
    }

    public int setGolsMandante(int golsMandante) {
        return this.golsMandante = golsMandante;
    }

    public int setGolsVisitante(int golsVisitante) {
        return this.golsVisitante = golsVisitante;
    }

    public Resultado setResultado(Resultado resultado) {
        return this.resultado = resultado;
    }

    public LocalDateTime setDataHora(LocalDateTime now) {
        return this.dataHora = LocalDateTime.now();
    }

    public Resultado getResultado() {
        return resultado;
    }

    public ClubeEntity getClubeMandante() {
        return clubeMandante;
    }

    public ClubeEntity getClubeVisitante() {
        return clubeVisitante;
    }

    public ClubeEntity getMandante() {
        return clubeMandante;
    }

    @Override
    public ClubeEntity getVisitante() {
        return clubeVisitante;
    }

    @Override
    public boolean isFinalizada() {
        return false;
    }

    public CampeonatoEntity setCampeonato(CampeonatoEntity campeonato) {
        return this.campeonato = campeonato;
    }

    public void setFinalizada(boolean finalizada) {
        this.finalizada = finalizada;
    }

    @Override
    public void aplicarResultado(int golsMandante, int golsVisitante, String resultado) {

    }

    @Override
    public Long getId() {
        return id;
    }

    public enum Resultado {
        VITORIA_MANDANTE, VITORIA_VISITANTE, EMPATE
    }

}
