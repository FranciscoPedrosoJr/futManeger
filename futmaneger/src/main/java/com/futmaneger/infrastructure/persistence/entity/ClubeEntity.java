package com.futmaneger.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "clubes")
public class ClubeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String estado;

    private String pais;
    private BigDecimal saldo = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private TecnicoEntity tecnico;


    public ClubeEntity() {
    }

    public ClubeEntity(String nome, String estado, String pais) {
        this.nome = nome;
        this.estado = estado;
        this.pais = pais;
        this.saldo = new BigDecimal("50000000");
    }

    public ClubeEntity(Long id, String nome, String estado, String pais, BigDecimal saldo, Long tecnicoId) {
        this.id = id;
        this.nome = nome;
        this.estado = estado;
        this.pais = pais;
        this.saldo = saldo;

        if (tecnicoId != null) {
            this.tecnico = new TecnicoEntity();
            this.tecnico.setId(tecnicoId);
        }
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }

    public void setNome(String nome) { this.nome = nome; }

    public String getEstado() { return estado; }

    public void setEstado(String estado) { this.estado = estado; }

    public String getPais() { return pais; }

    public void setPais(String pais) { this.pais = pais; }

    public TecnicoEntity getTecnico() { return tecnico; }

    public void setTecnico(TecnicoEntity tecnico) { this.tecnico = tecnico; }

    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public Long getTecnicoId() {
        return tecnico != null ? tecnico.getId() : null;
    }
}