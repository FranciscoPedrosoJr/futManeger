package com.futmaneger.application.dto;

import java.util.List;

public record EscalacaoResponseDTO(

        String nomeClube,
        String formacao,
        List<JogadorEscaladoDTO> titulares,
        List<JogadorEscaladoDTO> reservas
) {
}
