package com.futmaneger.infrastructure.config;

import com.futmaneger.application.usecase.campeonato.CorrigirCopasLegadasService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CorrecaoCopasLegadasMigration implements ApplicationRunner {

    private final CorrigirCopasLegadasService corrigirCopasLegadas;

    public CorrecaoCopasLegadasMigration(CorrigirCopasLegadasService corrigirCopasLegadas) {
        this.corrigirCopasLegadas = corrigirCopasLegadas;
    }

    @Override
    public void run(ApplicationArguments args) {
        corrigirCopasLegadas.executar();
    }
}
