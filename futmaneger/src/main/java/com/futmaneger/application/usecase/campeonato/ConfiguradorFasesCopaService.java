package com.futmaneger.application.usecase.campeonato;

import com.futmaneger.application.exception.DadosInvalidosException;
import com.futmaneger.infrastructure.persistence.entity.CampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.FaseCampeonatoEntity;
import com.futmaneger.infrastructure.persistence.entity.FormatoFase;
import com.futmaneger.infrastructure.persistence.jpa.FaseCampeonatoRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ConfiguradorFasesCopaService {

    private final FaseCampeonatoRepository faseRepository;

    public ConfiguradorFasesCopaService(FaseCampeonatoRepository faseRepository) {
        this.faseRepository = faseRepository;
    }

    /**
     * O formato antigo com grupos suporta 4, 8, 16 ou 32 clubes. Para outras
     * quantidades, cria uma eliminatoria direta com fase preliminar e folgas.
     */
    public List<FaseCampeonatoEntity> configurarSeNecessario(CampeonatoEntity campeonato) {
        List<FaseCampeonatoEntity> existentes = faseRepository.findByCampeonatoOrderByOrdem(campeonato);
        if (!existentes.isEmpty() || suportaFormatoPadrao(campeonato.getQuantidadeClubes())) {
            return existentes;
        }
        if (campeonato.getQuantidadeClubes() < 2) {
            throw new DadosInvalidosException("Uma copa precisa ter pelo menos dois clubes");
        }

        int quantidadeFases = quantidadeFases(campeonato.getQuantidadeClubes());
        int capacidade = 1 << quantidadeFases;
        List<FaseCampeonatoEntity> fases = new ArrayList<>();
        for (int ordem = 1; ordem <= quantidadeFases; ordem++) {
            int participantesDaFase = capacidade >> (ordem - 1);
            String nome = nomeFase(ordem, participantesDaFase, campeonato.getQuantidadeClubes());
            fases.add(new FaseCampeonatoEntity(
                    campeonato, ordem, nome, FormatoFase.JOGO_UNICO));
        }
        return faseRepository.saveAll(fases);
    }

    public int quantidadeFases(int quantidadeClubes) {
        if (quantidadeClubes < 2) {
            throw new DadosInvalidosException("Uma copa precisa ter pelo menos dois clubes");
        }
        int fases = 0;
        int capacidade = 1;
        while (capacidade < quantidadeClubes) {
            capacidade <<= 1;
            fases++;
        }
        return fases;
    }

    public boolean suportaFormatoPadrao(int quantidadeClubes) {
        return quantidadeClubes == 4 || quantidadeClubes == 8
                || quantidadeClubes == 16 || quantidadeClubes == 32;
    }

    private String nomeFase(int ordem, int participantes, int quantidadeClubes) {
        int capacidadeInicial = 1 << quantidadeFases(quantidadeClubes);
        if (ordem == 1 && quantidadeClubes != capacidadeInicial) {
            return "Fase preliminar";
        }
        return switch (participantes) {
            case 2 -> "Final";
            case 4 -> "Semifinal";
            case 8 -> "Quartas de final";
            case 16 -> "Oitavas de final";
            case 32 -> "16 avos de final";
            case 64 -> "32 avos de final";
            default -> "Fase de " + participantes;
        };
    }
}
