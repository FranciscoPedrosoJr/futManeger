package com.futmaneger.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa a "edição" de um campeonato dentro de uma temporada, ou seja, a
 * disputa concreta de um {@link CampeonatoEntity} (molde reutilizável) em uma
 * {@link TemporadaEntity} específica.
 *
 * <p>Cada edição possui seus próprios participantes, rodadas, partidas, tabela,
 * grupos, resultados e estado de disputa, permitindo que o mesmo campeonato seja
 * reutilizado em temporadas diferentes sem compartilhar dados.</p>
 */
@Entity
@Table(name = "edicoes_campeonato",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_edicao_temporada_campeonato",
                columnNames = {"temporada_id", "campeonato_id"}))
public class EdicaoCampeonatoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "campeonato_id")
    private CampeonatoEntity campeonato;

    @ManyToOne(optional = false)
    @JoinColumn(name = "temporada_id")
    private TemporadaEntity temporada;

    private boolean emAndamento;

    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;

    @ManyToOne
    @JoinColumn(name = "campeao_id")
    private ClubeEntity campeao;

    private boolean mataMataIniciado;

    @Enumerated(EnumType.STRING)
    private PartidaMataMataEntity.FaseMataMata faseAtualMataMata;

    @OneToMany(mappedBy = "edicao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RodadaEntity> rodadas = new ArrayList<>();

    public EdicaoCampeonatoEntity() {
    }

    public EdicaoCampeonatoEntity(CampeonatoEntity campeonato, TemporadaEntity temporada) {
        this.campeonato = campeonato;
        this.temporada = temporada;
        this.emAndamento = true;
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

    public void setCampeonato(CampeonatoEntity campeonato) {
        this.campeonato = campeonato;
    }

    public TemporadaEntity getTemporada() {
        return temporada;
    }

    public void setTemporada(TemporadaEntity temporada) {
        this.temporada = temporada;
    }

    public boolean getEmAndamento() {
        return emAndamento;
    }

    public void setEmAndamento(boolean emAndamento) {
        this.emAndamento = emAndamento;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public ClubeEntity getCampeao() {
        return campeao;
    }

    public void setCampeao(ClubeEntity campeao) {
        this.campeao = campeao;
    }

    public boolean getMataMataIniciado() {
        return mataMataIniciado;
    }

    public void setMataMataIniciado(boolean mataMataIniciado) {
        this.mataMataIniciado = mataMataIniciado;
    }

    public PartidaMataMataEntity.FaseMataMata getFaseAtualMataMata() {
        return faseAtualMataMata;
    }

    public void setFaseAtualMataMata(PartidaMataMataEntity.FaseMataMata faseAtualMataMata) {
        this.faseAtualMataMata = faseAtualMataMata;
    }

    public List<RodadaEntity> getRodadas() {
        return rodadas;
    }

    public void setRodadas(List<RodadaEntity> rodadas) {
        this.rodadas = rodadas;
    }
}
