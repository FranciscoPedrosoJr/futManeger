package com.futmaneger.infrastructure.config;

import com.futmaneger.infrastructure.persistence.entity.CategoriaTransacao;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * O {@code ddl-auto=update} do Hibernate não altera constraints já existentes. Em bases
 * criadas antes de {@code COTA_TV} e {@code PATROCINIO}, o check de
 * {@code transacoes_financeiras.categoria} rejeita essas categorias. Esta migração recria o
 * check com todos os valores atuais do enum.
 */
@Component
public class CorrecaoConstraintCategoriaTransacaoMigration implements ApplicationRunner {

    private static final Logger LOG =
            LoggerFactory.getLogger(CorrecaoConstraintCategoriaTransacaoMigration.class);

    private static final String TABELA = "TRANSACOES_FINANCEIRAS";
    private static final String NOVA_CONSTRAINT = "CK_TRANSACAO_CATEGORIA";

    private final JdbcTemplate jdbcTemplate;

    public CorrecaoConstraintCategoriaTransacaoMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<ConstraintExistente> desatualizadas = buscarConstraintsDeCategoria().stream()
                .filter(constraint -> !cobreTodasAsCategorias(constraint.clausula()))
                .toList();

        if (desatualizadas.isEmpty()) {
            return;
        }

        desatualizadas.forEach(constraint -> jdbcTemplate.execute(
                "ALTER TABLE " + TABELA + " DROP CONSTRAINT \"" + constraint.nome() + "\""));
        jdbcTemplate.execute("ALTER TABLE " + TABELA + " ADD CONSTRAINT " + NOVA_CONSTRAINT
                + " CHECK (CATEGORIA IN (" + categoriasEmSql() + "))");
        LOG.info("Constraint de categoria das transações financeiras atualizada");
    }

    private List<ConstraintExistente> buscarConstraintsDeCategoria() {
        return jdbcTemplate.query("""
                SELECT cc.constraint_name, cc.check_clause
                  FROM information_schema.check_constraints cc
                  JOIN information_schema.table_constraints tc
                    ON tc.constraint_schema = cc.constraint_schema
                   AND tc.constraint_name = cc.constraint_name
                 WHERE tc.table_name = ?
                   AND tc.constraint_type = 'CHECK'
                   AND cc.check_clause LIKE '%CATEGORIA%'
                """,
                (rs, linha) -> new ConstraintExistente(rs.getString(1), rs.getString(2)),
                TABELA);
    }

    private boolean cobreTodasAsCategorias(String clausula) {
        return Arrays.stream(CategoriaTransacao.values())
                .allMatch(categoria -> clausula.contains("'" + categoria.name() + "'"));
    }

    private String categoriasEmSql() {
        return Arrays.stream(CategoriaTransacao.values())
                .map(categoria -> "'" + categoria.name() + "'")
                .collect(Collectors.joining(", "));
    }

    private record ConstraintExistente(String nome, String clausula) {
    }
}
