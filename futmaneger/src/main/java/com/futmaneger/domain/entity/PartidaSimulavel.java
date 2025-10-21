package com.futmaneger.domain.entity;

import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;

public interface PartidaSimulavel {

    ClubeEntity getMandante();
    ClubeEntity getVisitante();

    boolean isFinalizada();
    void setFinalizada(boolean finalizada);

    void aplicarResultado(int golsMandante, int golsVisitante, String resultado);

    Long getId();
}
