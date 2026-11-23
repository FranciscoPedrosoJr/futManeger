package com.futmaneger.infrastructure.persistence.jpa;

import com.futmaneger.infrastructure.persistence.entity.EstatisticaJogadorPartidaEntity;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persiste estatísticas em lote sem exigir o identificador gerado de cada linha.
 * O JdbcTemplate participa da mesma transação da simulação.
 */
@Repository
public class EstatisticaJogadorPartidaBatchRepository {

    private static final int TAMANHO_LOTE = 100;
    private static final String INSERT_SQL = """
            INSERT INTO estatisticas_jogador_partida
                (cartoes_amarelos, cartoes_vermelhos, gols, jogador_id,
                 nome_jogador, nota, partida_id, partida_mata_mata)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public EstatisticaJogadorPartidaBatchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveAll(List<EstatisticaJogadorPartidaEntity> estatisticas) {
        if (estatisticas.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate(INSERT_SQL, estatisticas, TAMANHO_LOTE,
                EstatisticaJogadorPartidaBatchRepository::preencherParametros);
    }

    private static void preencherParametros(PreparedStatement ps,
                                             EstatisticaJogadorPartidaEntity estatistica)
            throws SQLException {
        ps.setInt(1, estatistica.getCartoesAmarelos());
        ps.setInt(2, estatistica.getCartoesVermelhos());
        ps.setInt(3, estatistica.getGols());
        ps.setLong(4, estatistica.getJogadorId());
        ps.setString(5, estatistica.getNomeJogador());
        ps.setDouble(6, estatistica.getNota());
        ps.setLong(7, estatistica.getPartidaId());
        ps.setBoolean(8, estatistica.isPartidaMataMata());
    }
}
