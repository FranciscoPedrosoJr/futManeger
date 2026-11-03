package com.futmaneger.infrastructure.config;

import com.futmaneger.infrastructure.persistence.entity.Continente;
import com.futmaneger.infrastructure.persistence.entity.PaisEntity;
import com.futmaneger.infrastructure.persistence.jpa.PaisRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Popula a tabela de países/continentes com um conjunto inicial na primeira
 * inicialização (apenas quando a tabela está vazia). Novos países podem ser
 * cadastrados pela rota POST /paises.
 */
@Configuration
public class PaisSeedConfig {

    @Bean
    public CommandLineRunner seedPaises(PaisRepository paisRepository) {
        return args -> {
            if (paisRepository.count() > 0) {
                return;
            }

            List<PaisEntity> paises = List.of(
                    // América do Sul
                    new PaisEntity("Brasil", Continente.AMERICA_DO_SUL),
                    new PaisEntity("Argentina", Continente.AMERICA_DO_SUL),
                    new PaisEntity("Uruguai", Continente.AMERICA_DO_SUL),
                    new PaisEntity("Chile", Continente.AMERICA_DO_SUL),
                    new PaisEntity("Paraguai", Continente.AMERICA_DO_SUL),
                    new PaisEntity("Colômbia", Continente.AMERICA_DO_SUL),
                    new PaisEntity("Peru", Continente.AMERICA_DO_SUL),
                    new PaisEntity("Equador", Continente.AMERICA_DO_SUL),
                    new PaisEntity("Bolívia", Continente.AMERICA_DO_SUL),
                    new PaisEntity("Venezuela", Continente.AMERICA_DO_SUL),

                    // Europa
                    new PaisEntity("Espanha", Continente.EUROPA),
                    new PaisEntity("Inglaterra", Continente.EUROPA),
                    new PaisEntity("Itália", Continente.EUROPA),
                    new PaisEntity("Alemanha", Continente.EUROPA),
                    new PaisEntity("França", Continente.EUROPA),
                    new PaisEntity("Portugal", Continente.EUROPA),
                    new PaisEntity("Holanda", Continente.EUROPA),
                    new PaisEntity("Bélgica", Continente.EUROPA),

                    // Outros
                    new PaisEntity("Estados Unidos", Continente.AMERICA_DO_NORTE),
                    new PaisEntity("México", Continente.AMERICA_DO_NORTE),
                    new PaisEntity("Japão", Continente.ASIA),
                    new PaisEntity("Coreia do Sul", Continente.ASIA),
                    new PaisEntity("Austrália", Continente.OCEANIA),
                    new PaisEntity("Egito", Continente.AFRICA),
                    new PaisEntity("Marrocos", Continente.AFRICA)
            );

            paisRepository.saveAll(paises);
        };
    }
}
