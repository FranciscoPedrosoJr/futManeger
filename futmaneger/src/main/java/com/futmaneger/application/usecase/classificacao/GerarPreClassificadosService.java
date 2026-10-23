package com.futmaneger.application.usecase.classificacao;

import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.CriterioClassificacao;
import com.futmaneger.infrastructure.persistence.entity.EdicaoCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.PreClassificadoEntity;
import com.futmaneger.infrastructure.persistence.entity.TabelaCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.TemporadaEntity;
import com.futmaneger.infrastructure.persistence.entity.VinculoClassificacaoEntity;
import com.futmaneger.infrastructure.persistence.jpa.PreClassificadoRepository;
import com.futmaneger.infrastructure.persistence.jpa.TabelaCampeonatoRepository;
import com.futmaneger.infrastructure.persistence.jpa.VinculoClassificacaoRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GerarPreClassificadosService {

    private final VinculoClassificacaoRepository vinculoRepository;
    private final TabelaCampeonatoRepository tabelaRepository;
    private final PreClassificadoRepository preClassificadoRepository;

    public GerarPreClassificadosService(VinculoClassificacaoRepository vinculoRepository,
                                        TabelaCampeonatoRepository tabelaRepository,
                                        PreClassificadoRepository preClassificadoRepository) {
        this.vinculoRepository = vinculoRepository;
        this.tabelaRepository = tabelaRepository;
        this.preClassificadoRepository = preClassificadoRepository;
    }

    public void gerar(TemporadaEntity temporada) {
        for (EdicaoCampeonatoEntity edicao : temporada.getEdicoes()) {
            List<VinculoClassificacaoEntity> vinculos =
                    vinculoRepository.findByCampeonatoOrigem(edicao.getCampeonato());

            for (VinculoClassificacaoEntity vinculo : vinculos) {
                List<ClubeEntity> selecionados = selecionarClubes(edicao, vinculo);
                for (ClubeEntity clube : selecionados) {
                    preClassificadoRepository.save(
                            new PreClassificadoEntity(clube, vinculo.getCampeonatoDestino(), temporada));
                }
            }
        }
    }

    private List<ClubeEntity> selecionarClubes(EdicaoCampeonatoEntity edicao, VinculoClassificacaoEntity vinculo) {
        if (vinculo.getCriterio() == CriterioClassificacao.CAMPEAO) {
            ClubeEntity campeao = edicao.getCampeao();
            return campeao == null ? List.of() : List.of(campeao);
        }

        List<TabelaCampeonatoEntity> tabela =
                tabelaRepository.findByEdicaoOrderByPontosDescSaldoGolsDescGolsProDesc(edicao);
        if (tabela.isEmpty()) {
            return List.of();
        }

        int quantidade = Math.min(vinculo.getQuantidade(), tabela.size());
        List<ClubeEntity> resultado = new ArrayList<>();

        if (vinculo.getCriterio() == CriterioClassificacao.MELHORES) {
            for (int i = 0; i < quantidade; i++) {
                resultado.add(tabela.get(i).getClube());
            }
        } else {
            for (int i = 0; i < quantidade; i++) {
                resultado.add(tabela.get(tabela.size() - 1 - i).getClube());
            }
        }

        return resultado;
    }
}
