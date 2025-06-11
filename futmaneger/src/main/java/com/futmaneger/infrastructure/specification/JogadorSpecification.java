package com.futmaneger.infrastructure.specification;

import com.futmaneger.application.dto.JogadorFiltroDTO;
import com.futmaneger.domain.entity.Jogador;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class JogadorSpecification {
    public static Specification<Jogador> comFiltros(JogadorFiltroDTO filtro) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro.nome() != null && !filtro.nome().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nome")),
                        "%" + filtro.nome().toLowerCase() + "%"
                ));
            }

            if (filtro.forca() != null) {
                predicates.add(criteriaBuilder.equal(root.get("forca"), filtro.forca()));
            }

            if (filtro.posicao() != null && !filtro.posicao().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("posicao"), filtro.posicao()));
            }

            if (predicates.isEmpty())
            {
                return null;
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
