package com.futmaneger.application.usecase.classificacao;

import com.futmaneger.application.dto.CadastrarVinculoClassificacaoRequestDTO;
import com.futmaneger.application.dto.VinculoClassificacaoResponseDTO;
import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.application.exception.NaoEncontradoException;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.CriterioClassificacao;
import com.futmaneger.infrastructure.persistence.entity.VinculoClassificacaoEntity;
import com.futmaneger.infrastructure.persistence.jpa.CampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.VinculoClassificacaoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GerenciarVinculoClassificacaoUseCase {

    private final VinculoClassificacaoRepository vinculoRepository;
    private final CampeonatoRepository campeonatoRepository;

    public GerenciarVinculoClassificacaoUseCase(VinculoClassificacaoRepository vinculoRepository,
                                                CampeonatoRepository campeonatoRepository) {
        this.vinculoRepository = vinculoRepository;
        this.campeonatoRepository = campeonatoRepository;
    }

    @Transactional
    public VinculoClassificacaoResponseDTO cadastrar(CadastrarVinculoClassificacaoRequestDTO request) {
        if (request.sentido() == null || request.criterio() == null) {
            throw new DadosInvalidosException("Sentido e critério são obrigatórios");
        }
        if (request.quantidade() <= 0) {
            throw new DadosInvalidosException("A quantidade de vagas deve ser maior que zero");
        }
        if (request.campeonatoOrigemId() == null || request.campeonatoDestinoId() == null) {
            throw new DadosInvalidosException("Campeonato de origem e destino são obrigatórios");
        }
        if (request.campeonatoOrigemId().equals(request.campeonatoDestinoId())) {
            throw new DadosInvalidosException("O campeonato de origem deve ser diferente do destino");
        }

        CampeonatoEntity origem = campeonatoRepository.findById(request.campeonatoOrigemId())
                .orElseThrow(() -> new NaoEncontradoException("Campeonato de origem não encontrado"));
        CampeonatoEntity destino = campeonatoRepository.findById(request.campeonatoDestinoId())
                .orElseThrow(() -> new NaoEncontradoException("Campeonato de destino não encontrado"));

        if (origem.getTipo() == CampeonatoEntity.TipoCampeonato.MATA_MATA
                && request.criterio() != CriterioClassificacao.CAMPEAO) {
            throw new DadosInvalidosException(
                    "Campeonato de origem do tipo mata-mata só aceita o critério CAMPEAO");
        }

        if (vinculoRepository.existsByCampeonatoOrigemAndCampeonatoDestinoAndSentido(
                origem, destino, request.sentido())) {
            throw new DadosInvalidosException(
                    "Já existe um vínculo com este sentido entre os campeonatos informados");
        }

        VinculoClassificacaoEntity vinculo = vinculoRepository.save(
                new VinculoClassificacaoEntity(origem, destino, request.quantidade(),
                        request.sentido(), request.criterio(), request.faseDeEntradaOrdem()));

        return toResponse(vinculo);
    }

    public List<VinculoClassificacaoResponseDTO> listar() {
        return vinculoRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<VinculoClassificacaoResponseDTO> listarPorCampeonatoOrigem(Long campeonatoId) {
        CampeonatoEntity origem = campeonatoRepository.findById(campeonatoId)
                .orElseThrow(() -> new NaoEncontradoException("Campeonato não encontrado"));
        return vinculoRepository.findByCampeonatoOrigem(origem).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void remover(Long id) {
        if (!vinculoRepository.existsById(id)) {
            throw new NaoEncontradoException("Vínculo de classificação não encontrado");
        }
        vinculoRepository.deleteById(id);
    }

    private VinculoClassificacaoResponseDTO toResponse(VinculoClassificacaoEntity vinculo) {
        return new VinculoClassificacaoResponseDTO(
                vinculo.getId(),
                vinculo.getCampeonatoOrigem().getId(),
                vinculo.getCampeonatoOrigem().getNome(),
                vinculo.getCampeonatoDestino().getId(),
                vinculo.getCampeonatoDestino().getNome(),
                vinculo.getQuantidade(),
                vinculo.getSentido(),
                vinculo.getCriterio(),
                vinculo.getFaseDeEntradaOrdem()
        );
    }
}
