package com.futmaneger.application.dto;

import java.util.List;

public record CampeonatoResponseDTO(Long id, String nome, String tipo, List<String> clubes) {}
