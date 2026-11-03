package com.futmaneger.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "grupos")
public class GrupoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edicao_id", nullable = false)
    private EdicaoCampeonatoEntity edicao;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            name = "grupo_clubes",
            joinColumns = @JoinColumn(name = "grupo_id"),
            inverseJoinColumns = @JoinColumn(name = "clube_id")
    )
    private Set<ClubeEntity> clubes = new HashSet<>();;

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

    public EdicaoCampeonatoEntity getEdicao() {
        return edicao;
    }

    public void setEdicao(EdicaoCampeonatoEntity edicao) {
        this.edicao = edicao;
    }

    public Set<ClubeEntity> getClubes() {
        return clubes;
    }

    public void setClubes(Set<ClubeEntity> clubes) {
        this.clubes = clubes;
    }
}

