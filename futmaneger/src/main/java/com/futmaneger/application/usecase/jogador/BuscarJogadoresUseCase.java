package com.futmaneger.application.usecase.jogador;

import com.futmaneger.application.dto.JogadorFiltroDTO;
import com.futmaneger.application.dto.JogadorResponseDTO;
import com.futmaneger.infrastructure.persistence.jpa.JogadorJpaRepository;
import com.futmaneger.infrastructure.specification.JogadorSpecification;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BuscarJogadoresUseCase {
    private final JogadorJpaRepository jogadorRepository;

    public BuscarJogadoresUseCase(JogadorJpaRepository jogadorRepository) {
        this.jogadorRepository = jogadorRepository;
    }

    public List<JogadorResponseDTO> buscar(JogadorFiltroDTO filtro) {
        return jogadorRepository.findAll(JogadorSpecification.comFiltros(filtro)).stream()
                .map(j -> new JogadorResponseDTO(
                        j.getId(),
                        j.getNome(),
                        j.getPosicao(),
                        j.getForca(),
                        j.isDiferenciado(),
                        j.isIdentificacaoComClube(),
                        j.getClubeNome().getNome(),
                        j.getValorMercado()
                ))
                .toList();
    }
}
