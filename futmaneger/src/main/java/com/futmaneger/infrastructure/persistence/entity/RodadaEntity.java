package com.futmaneger.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rodadas")
public class RodadaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int numero;

    private boolean finalizada;

    @ManyToOne
    @JoinColumn(name = "campeonato_id")
    private CampeonatoEntity campeonato;

    @OneToMany(mappedBy = "rodada", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PartidaEntity> partidas = new ArrayList<>();

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public void setCampeonato(CampeonatoEntity campeonato) {
        this.campeonato = campeonato;
    }

    public void setFinalizada(boolean finalizada) {
        this.finalizada = finalizada;
    }

    public List<PartidaEntity> getPartidas() { return partidas; }

    public CampeonatoEntity getCampeonato() {
        return campeonato;
    }

    public int getNumero() { return numero;}
}
