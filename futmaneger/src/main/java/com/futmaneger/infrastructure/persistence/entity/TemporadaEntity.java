package com.futmaneger.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "temporadas")
public class TemporadaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificação da temporada, por exemplo "Temporada 2026".
     */
    @Column(nullable = false)
    private String nome;

    /**
     * Ano de referência da temporada. Usado para gerar o nome e a linha do tempo.
     */
    @Column(nullable = false)
    private Integer ano;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTemporada estado = EstadoTemporada.AGUARDANDO_INICIO;

    /**
     * Início da linha do tempo: primeiro dia do ano da temporada (janeiro).
     */
    private LocalDate dataInicio;

    /**
     * Encerramento da linha do tempo: último dia do ano da temporada (dezembro).
     */
    private LocalDate dataEncerramento;

    /**
     * Edições de campeonato disputadas na temporada. Uma temporada pode possuir
     * várias edições (cada uma é um campeonato/molde disputado nesta temporada),
     * e o mesmo campeonato pode ser reutilizado em temporadas diferentes.
     */
    @OneToMany(mappedBy = "temporada", cascade = CascadeType.ALL)
    private List<EdicaoCampeonatoEntity> edicoes = new ArrayList<>();

    public TemporadaEntity() {
    }

    private TemporadaEntity(int ano) {
        this.ano = ano;
        this.nome = "Temporada " + ano;
        this.estado = EstadoTemporada.AGUARDANDO_INICIO;
        this.dataInicio = LocalDate.of(ano, 1, 1);
        this.dataEncerramento = LocalDate.of(ano, 12, 31);
    }

    /**
     * Cria uma temporada usando o ano atual (ex.: "Temporada 2026").
     */
    public static TemporadaEntity paraAnoAtual() {
        return new TemporadaEntity(Year.now().getValue());
    }

    /**
     * Cria a próxima temporada a partir desta, somando +1 ao ano automaticamente.
     */
    public TemporadaEntity proximaTemporada() {
        return new TemporadaEntity(this.ano + 1);
    }

    public void iniciar() {
        this.estado = EstadoTemporada.EM_ANDAMENTO;
    }

    /**
     * Finaliza a temporada somente quando todos os campeonatos previstos estiverem
     * concluídos. A finalização de um campeonato não finaliza automaticamente a
     * temporada; esta verificação precisa ser chamada explicitamente.
     *
     * @return true se a temporada foi finalizada, false caso ainda existam
     *         campeonatos em andamento.
     */
    public boolean finalizarSeConcluida() {
        if (edicoes.isEmpty()) {
            return false;
        }
        boolean todosConcluidos = edicoes.stream()
                .noneMatch(EdicaoCampeonatoEntity::getEmAndamento);
        if (todosConcluidos) {
            this.estado = EstadoTemporada.FINALIZADA;
        }
        return todosConcluidos;
    }

    public void adicionarEdicao(EdicaoCampeonatoEntity edicao) {
        edicao.setTemporada(this);
        this.edicoes.add(edicao);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public EstadoTemporada getEstado() {
        return estado;
    }

    public void setEstado(EstadoTemporada estado) {
        this.estado = estado;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataEncerramento() {
        return dataEncerramento;
    }

    public void setDataEncerramento(LocalDate dataEncerramento) {
        this.dataEncerramento = dataEncerramento;
    }

    public List<EdicaoCampeonatoEntity> getEdicoes() {
        return edicoes;
    }

    public void setEdicoes(List<EdicaoCampeonatoEntity> edicoes) {
        this.edicoes = edicoes;
    }

    public enum EstadoTemporada {
        AGUARDANDO_INICIO,
        EM_ANDAMENTO,
        FINALIZADA
    }
}
