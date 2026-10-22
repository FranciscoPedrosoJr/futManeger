package com.futmaneger.infrastructure.persistence.entity;

/**
 * Abrangência (categoria) de um campeonato. Define quais clubes são elegíveis
 * para participar, de forma independente do formato de disputa
 * ({@link CampeonatoEntity.TipoCampeonato}).
 *
 * <p>Cada valor declara como os participantes devem ser filtrados:</p>
 * <ul>
 *   <li>{@code filtraPais}: só clubes do mesmo país do campeonato.</li>
 *   <li>{@code filtraEstado}: só clubes do mesmo estado (implica país).</li>
 *   <li>{@code filtraContinente}: só clubes cujo país pertence ao mesmo continente.</li>
 *   <li>{@code apenasSelecoes}: só participantes marcados como seleção.</li>
 *   <li>{@code apenasClubes}: exclui seleções (apenas clubes comuns).</li>
 * </ul>
 */
public enum AbrangenciaCampeonato {

    NACIONAL(true, false, false, false, true),
    ESTADUAL(true, true, false, false, true),
    CONTINENTAL(false, false, true, false, true),
    MUNDIAL(false, false, false, false, true),
    COPA_NACIONAL(true, false, false, false, true),
    COPA_INTERNACIONAL(false, false, false, false, true),
    COPA_DO_MUNDO(false, false, false, true, false),
    CAMPEONATO_SELECOES(false, false, false, true, false);

    private final boolean filtraPais;
    private final boolean filtraEstado;
    private final boolean filtraContinente;
    private final boolean apenasSelecoes;
    private final boolean apenasClubes;

    AbrangenciaCampeonato(boolean filtraPais, boolean filtraEstado, boolean filtraContinente,
                          boolean apenasSelecoes, boolean apenasClubes) {
        this.filtraPais = filtraPais;
        this.filtraEstado = filtraEstado;
        this.filtraContinente = filtraContinente;
        this.apenasSelecoes = apenasSelecoes;
        this.apenasClubes = apenasClubes;
    }

    public boolean isFiltraPais() {
        return filtraPais;
    }

    public boolean isFiltraEstado() {
        return filtraEstado;
    }

    public boolean isFiltraContinente() {
        return filtraContinente;
    }

    public boolean isApenasSelecoes() {
        return apenasSelecoes;
    }

    public boolean isApenasClubes() {
        return apenasClubes;
    }
}
