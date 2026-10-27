package com.futmaneger.application.usecase.financeiro;

import com.futmaneger.application.dto.ExtratoClubeResponseDTO;
import com.futmaneger.application.dto.ExtratoItemResponseDTO;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.TransacaoFinanceiraEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TransacaoFinanceiraRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ConsultarExtratoUseCase {

    private final ClubeJpaRepository clubeRepository;
    private final TransacaoFinanceiraRepository transacaoRepository;

    public ConsultarExtratoUseCase(ClubeJpaRepository clubeRepository,
                                   TransacaoFinanceiraRepository transacaoRepository) {
        this.clubeRepository = clubeRepository;
        this.transacaoRepository = transacaoRepository;
    }

    public ExtratoClubeResponseDTO executar(Long clubeId) {
        ClubeEntity clube = clubeRepository.findById(clubeId)
                .orElseThrow(() -> new NaoEncontradoException("Clube não encontrado"));

        List<ExtratoItemResponseDTO> itens = transacaoRepository
                .findByClubeIdOrderByDataDesc(clubeId).stream()
                .map(this::toItem)
                .toList();

        return new ExtratoClubeResponseDTO(clube.getId(), clube.getNome(), clube.getSaldo(), itens);
    }

    private ExtratoItemResponseDTO toItem(TransacaoFinanceiraEntity t) {
        return new ExtratoItemResponseDTO(
                t.getId(), t.getValor(), t.getCategoria(), t.getDescricao(), t.getJogadorId(), t.getData());
    }
}
