package com.futmaneger.presentation.request;

import java.math.BigDecimal;

public record CadastrarClubeRequest(String nome, String pais, String estado, boolean ehSelecao,
                                    BigDecimal precoIngresso) {
}
