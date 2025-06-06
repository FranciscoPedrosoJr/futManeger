package com.futmaneger.infrastructure.persistence.entity;

import com.futmaneger.domain.entity.Clube;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "escalacoes")
public class EscalacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "clube_id")
    private Clube clube;

    private String formacao;

    @CreationTimestamp
    private LocalDateTime dataHora;

    public Clube setClube(Clube clube) {
        return this.clube = clube;
    }

    public String setFormacao(String formacao){
        return this.formacao = formacao;
    }

    public LocalDateTime setDataHora(LocalDateTime now) {
        return this.dataHora = LocalDateTime.now();
    }
}
