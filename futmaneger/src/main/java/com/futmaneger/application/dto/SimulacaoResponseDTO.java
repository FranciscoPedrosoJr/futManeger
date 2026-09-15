package com.futmaneger.application.dto;

import java.util.List;

public record SimulacaoResponseDTO(
        String nomeMandante,
        int golsMandante,
        String nomeVisitante,
        int golsVisitante,
        String resultado,
        List<String> jogadoresQueFizeramGolsMandante,
        List<String> jogadoresQueFizeramGolsVisitante,
        List<JogadorPartidaResponseDTO> notasJogadores,
        JogadorPartidaResponseDTO melhorJogador,
        List<String> cartoesAmarelos,
        List<String> cartoesVermelhos
) {}