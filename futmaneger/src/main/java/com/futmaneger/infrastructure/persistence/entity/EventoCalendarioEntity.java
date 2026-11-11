package com.futmaneger.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;

/**
 * Acontecimento do calendário global de uma temporada. Não guarda cópia da
 * partida: apenas referencia a partida oficial, que continua pertencendo à
 * edição do campeonato.
 */
@Entity
@Table(name = "eventos_calendario",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tipo", "partida_id", "partida_mata_mata"}))
public class EventoCalendarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "temporada_id")
    private TemporadaEntity temporada;

    @ManyToOne
    @JoinColumn(name = "edicao_id")
    private EdicaoCampeonatoEntity edicao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEventoCalendario tipo;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private int ordem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEventoCalendario status = StatusEventoCalendario.PENDENTE;

    @Column(nullable = false)
    private boolean obrigatorio = true;

    @Column(name = "partida_id")
    private Long partidaId;

    @Column(name = "partida_mata_mata")
    private boolean partidaMataMata;

    @Column(length = 1000)
    private String mensagemErro;

    public EventoCalendarioEntity() {
    }

    public static EventoCalendarioEntity paraPartida(TemporadaEntity temporada, EdicaoCampeonatoEntity edicao,
                                                     Long partidaId, boolean partidaMataMata,
                                                     LocalDate data, int ordem) {
        EventoCalendarioEntity evento = new EventoCalendarioEntity();
        evento.temporada = temporada;
        evento.edicao = edicao;
        evento.tipo = TipoEventoCalendario.PARTIDA;
        evento.partidaId = partidaId;
        evento.partidaMataMata = partidaMataMata;
        evento.data = data;
        evento.ordem = ordem;
        return evento;
    }

    public boolean estaAberto() {
        return StatusEventoCalendario.abertos().contains(status);
    }

    public void concluir() {
        this.status = StatusEventoCalendario.CONCLUIDO;
        this.mensagemErro = null;
    }

    public void registrarErro(String mensagem) {
        this.status = StatusEventoCalendario.ERRO;
        this.mensagemErro = mensagem == null ? null : mensagem.substring(0, Math.min(mensagem.length(), 1000));
    }

    public Long getId() {
        return id;
    }

    public TemporadaEntity getTemporada() {
        return temporada;
    }

    public EdicaoCampeonatoEntity getEdicao() {
        return edicao;
    }

    public TipoEventoCalendario getTipo() {
        return tipo;
    }

    public LocalDate getData() {
        return data;
    }

    public int getOrdem() {
        return ordem;
    }

    public StatusEventoCalendario getStatus() {
        return status;
    }

    public boolean isObrigatorio() {
        return obrigatorio;
    }

    public Long getPartidaId() {
        return partidaId;
    }

    public boolean isPartidaMataMata() {
        return partidaMataMata;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }
}
