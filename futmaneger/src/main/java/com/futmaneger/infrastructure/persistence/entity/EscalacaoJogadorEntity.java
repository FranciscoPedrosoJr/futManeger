package com.futmaneger.infrastructure.persistence.entity;

import com.futmaneger.domain.entity.Jogador;
import com.futmaneger.domain.repository.JogadorRepository;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Arrays;

@Entity
@Table(name = "escalacao_jogadores")
public class EscalacaoJogadorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "escalacao_id")
    private EscalacaoEntity escalacao;

    @ManyToOne
    @JoinColumn(name = "jogador_id")
    private Jogador jogador;

    @Enumerated(EnumType.STRING)
    private TipoJogador tipo;

    public Long getId() {
        return id;
    }

    public EscalacaoEntity getEscalacao() {
        return escalacao;
    }

    public void setEscalacao(EscalacaoEntity escalacao) {
        this.escalacao = escalacao;
    }

    public Jogador getJogador() {
        return jogador;
    }

    public void setJogador(Jogador jogador) {
        this.jogador = jogador;
    }

    public TipoJogador getTipo() {
        return tipo;
    }

    public void setTipo(TipoJogador tipo) {
        this.tipo = tipo;
    }

    public enum TipoJogador {
        TITULAR, RESERVA
    }
}
