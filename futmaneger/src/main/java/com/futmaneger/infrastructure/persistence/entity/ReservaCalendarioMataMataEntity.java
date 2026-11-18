package com.futmaneger.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;

@Entity
@Table(name = "reservas_calendario_mata_mata",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reserva_mata_mata_edicao_ordem",
                columnNames = {"edicao_id", "ordem"}))
public class ReservaCalendarioMataMataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "edicao_id")
    private EdicaoCampeonatoEntity edicao;

    @Column(nullable = false)
    private int ordem;

    @Column(nullable = false)
    private LocalDate data;

    public ReservaCalendarioMataMataEntity() {
    }

    public ReservaCalendarioMataMataEntity(EdicaoCampeonatoEntity edicao, int ordem, LocalDate data) {
        this.edicao = edicao;
        this.ordem = ordem;
        this.data = data;
    }

    public Long getId() {
        return id;
    }

    public EdicaoCampeonatoEntity getEdicao() {
        return edicao;
    }

    public int getOrdem() {
        return ordem;
    }

    public LocalDate getData() {
        return data;
    }
}
