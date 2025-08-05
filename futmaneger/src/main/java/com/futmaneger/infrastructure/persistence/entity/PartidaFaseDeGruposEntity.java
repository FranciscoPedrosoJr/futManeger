package com.futmaneger.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class PartidaFaseDeGruposEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private CampeonatoEntity campeonato;

    private int rodada;

    @ManyToOne
    private ClubeEntity mandante;

    @ManyToOne
    private ClubeEntity visitante;

    private int golsMandante;
    private int golsVisitante;

    private boolean finalizada;

    @ManyToOne(optional = false)
    private GrupoEntity grupo;

    public boolean isFinalizada() {
        return finalizada;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCampeonato(CampeonatoEntity campeonato) {
        this.campeonato = campeonato;
    }

    public void setRodada(int rodada) {
        this.rodada = rodada;
    }

    public void setClubeMandante(ClubeEntity mandante) {
        this.mandante = mandante;
    }

    public void setClubeVisitante(ClubeEntity visitante) {
        this.visitante = visitante;
    }

    public void setGolsMandante(int golsMandante) {
        this.golsMandante = golsMandante;
    }

    public void setGolsVisitante(int golsVisitante) {
        this.golsVisitante = golsVisitante;
    }

    public void setFinalizada(boolean finalizada) {
        this.finalizada = finalizada;
    }

    public void setGrupo(GrupoEntity grupo) {
        this.grupo = grupo;
    }
}
