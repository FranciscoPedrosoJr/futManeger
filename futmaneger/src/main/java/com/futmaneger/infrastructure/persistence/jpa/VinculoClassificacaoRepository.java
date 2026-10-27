package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.SentidoClassificacao;
import com.futmaneger.infrastructure.persistence.entity.VinculoClassificacaoEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VinculoClassificacaoRepository extends JpaRepository<VinculoClassificacaoEntity, Long> {

    List<VinculoClassificacaoEntity> findByCampeonatoOrigem(CampeonatoEntity campeonatoOrigem);

    boolean existsByCampeonatoOrigemAndCampeonatoDestinoAndSentido(
            CampeonatoEntity campeonatoOrigem, CampeonatoEntity campeonatoDestino, SentidoClassificacao sentido);
}
