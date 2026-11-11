package com.futmaneger.application.usecase.temporada;

import com.futmaneger.application.dto.TemporadaResponseDTO;
import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.application.usecase.classificacao.GerarPreClassificadosService;
import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.StatusEventoCalendario;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import com.futmaneger.infrastructure.persistence.jpa.EventoCalendarioRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaMataMataRepository;
import com.futmaneger.infrastructure.persistence.jpa.PartidaRepository;
import com.futmaneger.infrastructure.persistence.jpa.TemporadaJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinalizarTemporadaUseCase {

    private final TemporadaJpaRepository temporadaRepository;
    private final GerarPreClassificadosService gerarPreClassificadosService;
    private final EventoCalendarioRepository eventoRepository;
    private final PartidaRepository partidaRepository;
    private final PartidaMataMataRepository partidaMataMataRepository;

    public FinalizarTemporadaUseCase(TemporadaJpaRepository temporadaRepository,
                                     GerarPreClassificadosService gerarPreClassificadosService,
                                     EventoCalendarioRepository eventoRepository,
                                     PartidaRepository partidaRepository,
                                     PartidaMataMataRepository partidaMataMataRepository) {
        this.temporadaRepository = temporadaRepository;
        this.gerarPreClassificadosService = gerarPreClassificadosService;
        this.eventoRepository = eventoRepository;
        this.partidaRepository = partidaRepository;
        this.partidaMataMataRepository = partidaMataMataRepository;
    }

    /**
     * Finaliza a temporada apenas quando todos os campeonatos previstos foram
     * concluídos e, havendo calendário, quando não restam acontecimentos
     * obrigatórios nem partidas pendentes. A finalização de um campeonato isolado
     * não finaliza a temporada.
     */
    @Transactional
    public TemporadaResponseDTO executar(Long temporadaId) {
        TemporadaEntity temporada = temporadaRepository.findById(temporadaId)
                .orElseThrow(() -> new NaoEncontradoException("Temporada não encontrada"));

        if (temporada.getEstado() == TemporadaEntity.EstadoTemporada.FINALIZADA) {
            throw new DadosInvalidosException("A temporada já foi finalizada");
        }

        if (temporada.isCalendarioGerado()) {
            validarCalendarioConcluido(temporada);
        }

        boolean finalizada = temporada.finalizarSeConcluida();
        if (!finalizada) {
            throw new DadosInvalidosException(
                    "A temporada só pode ser finalizada quando todos os campeonatos estiverem concluídos");
        }

        gerarPreClassificadosService.gerar(temporada);

        TemporadaEntity salva = temporadaRepository.save(temporada);
        return TemporadaMapper.toResponse(salva);
    }

    private void validarCalendarioConcluido(TemporadaEntity temporada) {
        long eventosPendentes = eventoRepository.countByTemporadaAndObrigatorioTrueAndStatusIn(
                temporada, StatusEventoCalendario.abertos());
        if (eventosPendentes > 0) {
            throw new DadosInvalidosException(
                    "A temporada possui " + eventosPendentes
                            + " acontecimento(s) obrigatório(s) pendente(s) no calendário");
        }

        for (EdicaoCampeonatoEntity edicao : temporada.getEdicoes()) {
            boolean partidaPendente =
                    partidaRepository.findByEdicao(edicao).stream().anyMatch(p -> !p.isFinalizada())
                            || partidaMataMataRepository.findByEdicaoId(edicao.getId()).stream()
                            .anyMatch(p -> !p.isFinalizada());
            if (partidaPendente) {
                throw new DadosInvalidosException(
                        "Existem partidas pendentes no campeonato '" + edicao.getCampeonato().getNome()
                                + "'. Conclua o calendário antes de finalizar a temporada");
            }
        }
    }
}
