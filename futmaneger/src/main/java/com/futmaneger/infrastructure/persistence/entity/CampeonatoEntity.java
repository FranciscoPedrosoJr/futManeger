package com.futmaneger.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private int quantidadeClubes;

    private boolean emAndamento;

    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;

    @ManyToOne
    @JoinColumn(name = "campeao_id")
    private ClubeEntity campeao;

    @OneToMany(mappedBy = "campeonato", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RodadaEntity> rodadas = new ArrayList<>();

    public List<RodadaEntity> getRodadas() {
        return rodadas;
    }

    public void setRodadas(List<RodadaEntity> rodadas) {
        this.rodadas = rodadas;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setTipo(TipoCampeonato tipo) {
        this.tipo = tipo;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setEmAndamento(boolean emAndamento) {
        this.emAndamento = emAndamento;
    }

    public void setQuantidadeClubes (int quantidadeClubes){
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

    public <E extends Enum<E>> Enum<E> getTipo() {
        return (Enum<E>) tipo;
    }

    public void setCampeao(ClubeEntity campeao) {
        this.campeao = campeao;
    }

    public enum TipoCampeonato {
        PONTOS_CORRIDOS, MATA_MATA
    }

}
