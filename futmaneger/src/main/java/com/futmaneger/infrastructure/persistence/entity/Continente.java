package com.futmaneger.infrastructure.persistence.entity;

/**
 * Continentes usados para definir a abrangência de campeonatos continentais.
 * O continente de um clube é derivado do seu país através de {@link PaisEntity}.
 */
public enum Continente {
    AMERICA_DO_SUL,
    AMERICA_DO_NORTE,
    AMERICA_CENTRAL,
    EUROPA,
    AFRICA,
    ASIA,
    OCEANIA
}
