package com.futmaneger.application.dto;

import com.futmaneger.infrastructure.persistence.entity.Continente;

/**
 * Request para cadastrar um país e seu continente na tabela de lookup usada
 * para resolver a abrangência continental dos campeonatos.
 */
public record CadastrarPaisRequestDTO(
        String nome,
        Continente continente
) {}
