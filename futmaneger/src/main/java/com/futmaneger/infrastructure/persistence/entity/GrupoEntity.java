package com.futmaneger.infrastructure.persistence.entity;

import com.futmaneger.domain.entity.Clube;
import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "grupos")
public class GrupoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campeonato_id", nullable = false)
    private CampeonatoEntity campeonato;

    @ManyToMany
    @JoinTable(
            name = "grupo_clubes",
            joinColumns = @JoinColumn(name = "grupo_id"),
            inverseJoinColumns = @JoinColumn(name = "clube_id")
    )
    private Set<ClubeEntity> clubes;

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

    public CampeonatoEntity getCampeonato() {
        return campeonato;
    }

    public void setCampeonato(CampeonatoEntity campeonato) {
        this.campeonato = campeonato;
    }

    public Set<ClubeEntity> getClubes() {
        return clubes;
    }

    public void setClubes(Set<ClubeEntity> clubes) {
        this.clubes = clubes;
    }
}

