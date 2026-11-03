package com.futmaneger.application.usecase.clube;

import com.futmaneger.application.dto.AtualizarSaldoRequestDTO;
import com.futmaneger.application.dto.AtualizarSaldoResponseDTO;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.application.usecase.financeiro.MovimentacaoFinanceiraService;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AtualizarSaldoClubeUseCase {

    private final ClubeJpaRepository clubeRepository;
    private final MovimentacaoFinanceiraService movimentacaoFinanceira;

    @Transactional
    public AtualizarSaldoResponseDTO executar(AtualizarSaldoRequestDTO request) {
        var clube = clubeRepository.findById(request.clubeId())
                .orElseThrow(() -> new NaoEncontradoException("Clube não encontrado"));

        movimentacaoFinanceira.ajustar(clube, request.valor(), "Ajuste manual de saldo");

        var tecnicoDTO =
                clube.getTecnico() != null
                        ? new AtualizarSaldoResponseDTO.TecnicoDTO(clube.getTecnico().getNome())
                        : null;

        return new AtualizarSaldoResponseDTO(
                clube.getId(),
                clube.getNome(),
                clube.getEstado(),
                clube.getPais(),
                clube.getSaldo(),
                tecnicoDTO
        );
    }
}
