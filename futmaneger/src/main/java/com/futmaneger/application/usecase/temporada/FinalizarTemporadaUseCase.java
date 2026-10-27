package com.futmaneger.application.usecase.temporada;

import com.futmaneger.application.dto.TemporadaResponseDTO;
import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.application.usecase.classificacao.GerarPreClassificadosService;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import com.futmaneger.infrastructure.persistence.jpa.TemporadaJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinalizarTemporadaUseCase {

    private final TemporadaJpaRepository temporadaRepository;
    private final GerarPreClassificadosService gerarPreClassificadosService;

    public FinalizarTemporadaUseCase(TemporadaJpaRepository temporadaRepository,
                                     GerarPreClassificadosService gerarPreClassificadosService) {
        this.temporadaRepository = temporadaRepository;
        this.gerarPreClassificadosService = gerarPreClassificadosService;
    }

    /**
     * Finaliza a temporada apenas quando todos os campeonatos previstos foram
     * concluídos. A finalização de um campeonato isolado não finaliza a temporada.
     */
    @Transactional
    public TemporadaResponseDTO executar(Long temporadaId) {
        TemporadaEntity temporada = temporadaRepository.findById(temporadaId)
                .orElseThrow(() -> new NaoEncontradoException("Temporada não encontrada"));

        boolean finalizada = temporada.finalizarSeConcluida();
        if (!finalizada) {
            throw new DadosInvalidosException(
                    "A temporada só pode ser finalizada quando todos os campeonatos estiverem concluídos");
        }

        gerarPreClassificadosService.gerar(temporada);

        TemporadaEntity salva = temporadaRepository.save(temporada);
        return TemporadaMapper.toResponse(salva);
    }
}
