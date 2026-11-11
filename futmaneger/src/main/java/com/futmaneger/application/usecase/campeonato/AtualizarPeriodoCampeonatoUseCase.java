package com.futmaneger.application.usecase.campeonato;

import com.futmaneger.application.dto.AtualizarPeriodoCampeonatoRequestDTO;
import com.futmaneger.application.dto.AtualizarPeriodoCampeonatoResponseDTO;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import com.futmaneger.infrastructure.persistence.jpa.CampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.EdicaoCampeonatoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Altera o período do campeonato (o molde reutilizável).
 *
 * <p>O novo período vale para as próximas edições. As edições de temporadas que
 * ainda não começaram são recalculadas, pois não têm calendário. As edições de
 * temporadas em andamento ou finalizadas não mudam: depois do início o calendário
 * é oficial.</p>
 */
@Service
public class AtualizarPeriodoCampeonatoUseCase {

    private final CampeonatoRepository campeonatoRepository;
    private final EdicaoCampeonatoRepository edicaoRepository;

    public AtualizarPeriodoCampeonatoUseCase(CampeonatoRepository campeonatoRepository,
                                             EdicaoCampeonatoRepository edicaoRepository) {
        this.campeonatoRepository = campeonatoRepository;
        this.edicaoRepository = edicaoRepository;
    }

    @Transactional
    public AtualizarPeriodoCampeonatoResponseDTO executar(Long campeonatoId,
                                                          AtualizarPeriodoCampeonatoRequestDTO request) {
        CampeonatoEntity campeonato = campeonatoRepository.findById(campeonatoId)
                .orElseThrow(() -> new NaoEncontradoException("Campeonato não encontrado"));

        PeriodoCampeonato.validar(request.mesInicio(), request.mesFim());

        campeonato.setMesInicio(request.mesInicio());
        campeonato.setMesFim(request.mesFim());
        campeonatoRepository.save(campeonato);

        List<EdicaoCampeonatoEntity> edicoes = edicaoRepository.findByCampeonato(campeonato);
        int atualizadas = 0;
        for (EdicaoCampeonatoEntity edicao : edicoes) {
            if (edicao.getTemporada().getEstado() == TemporadaEntity.EstadoTemporada.AGUARDANDO_INICIO) {
                PeriodoCampeonato.aplicarNaEdicao(edicao, campeonato, edicao.getTemporada().getAno());
                edicaoRepository.save(edicao);
                atualizadas++;
            }
        }

        return new AtualizarPeriodoCampeonatoResponseDTO(
                campeonato.getId(),
                campeonato.getNome(),
                campeonato.getMesInicio(),
                campeonato.getMesFim(),
                atualizadas,
                edicoes.size() - atualizadas
        );
    }
}
