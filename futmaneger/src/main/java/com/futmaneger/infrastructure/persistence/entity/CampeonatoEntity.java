package com.futmaneger.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Molde reutilizável de um campeonato. Descreve a competição em si (nome, país,
 * tipo e quantidade de clubes) e pode ser disputado em várias temporadas.
 *
 * <p>Os dados específicos de cada disputa (temporada, participantes, rodadas,
 * partidas, tabela, grupos, resultados, estado e campeão) ficam em
 * {@link EdicaoCampeonatoEntity}.</p>
 */
@Entity
@Table(name = "campeonatos")
public class CampeonatoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String estado;

    @Column(nullable = false)
    private String pais;

    @Enumerated(EnumType.STRING)
    private TipoCampeonato tipo;

    @Enumerated(EnumType.STRING)
    private AbrangenciaCampeonato abrangencia;

    private int quantidadeClubes;

    public CampeonatoEntity() {
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setTipo(TipoCampeonato tipo) {
        this.tipo = tipo;
    }

    public void setAbrangencia(AbrangenciaCampeonato abrangencia) {
        this.abrangencia = abrangencia;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setQuantidadeClubes(int quantidadeClubes) {
        this.quantidadeClubes = quantidadeClubes;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEstado() {
        return estado;
    }

    public TipoCampeonato getTipo() {
        return tipo;
    }

    public AbrangenciaCampeonato getAbrangencia() {
        return abrangencia;
    }

    public int getQuantidadeClubes() {
        return quantidadeClubes;
    }

    public String getPais() {
        return pais;
    }

    public enum TipoCampeonato {
        PONTOS_CORRIDOS, MATA_MATA
    }
}
