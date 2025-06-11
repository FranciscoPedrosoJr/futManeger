package com.futmaneger.infrastructure.persistence.entity;

import com.futmaneger.domain.entity.Clube;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "partidas")
public class PartidaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "clube_mandante_id")
    private Clube clubeMandante;

    @ManyToOne
    @JoinColumn(name = "clube_visitante_id")
    private Clube clubeVisitante;

    private int golsMandante;
    private int golsVisitante;

    @Enumerated(EnumType.STRING)
    private Resultado resultado;

    private LocalDateTime dataHora;

    public Clube setClubeMandante(Clube mandante) {
        return this.clubeMandante = mandante;
    }

    public Clube setClubeVisitante(Clube visitante) {
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

    public enum Resultado {
        VITORIA_MANDANTE, VITORIA_VISITANTE, EMPATE
    }

    // Getters e setters omitidos para brevidade
}
