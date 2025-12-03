package com.futmaneger.application.usecase.clube;

import com.futmaneger.application.dto.AtualizarSaldoRequestDTO;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.jpa.ClubeJpaRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AtualizarSaldoClubeUseCase {

    private final ClubeJpaRepository clubeRepository;

    public ClubeEntity executar(AtualizarSaldoRequestDTO request) {
        var clube = clubeRepository.findById(request.clubeId())
                .orElseThrow(() -> new NaoEncontradoException("Clube não encontrado"));

        var novoSaldo = clube.getSaldo().add(request.valor());
        clube.setSaldo(novoSaldo);

        return clubeRepository.save(clube);
    }
}
