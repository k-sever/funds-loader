package org.ksever.fundsLoader.repository;

import org.ksever.fundsLoader.model.LoadFundsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@Transactional
public class SQLFundsRepository implements FundsRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SQLFundsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(LoadFundsRequest request, boolean accepted) {
        var sql = "INSERT INTO \"funds\" (\"id\", \"customer_id\", \"time\", \"amount\", \"accepted\") VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, request.id(), request.customerId(), request.timestamp(), request.amount(), accepted);
    }

    @Override
    public Optional<LoadFundsRequest> findByIdAndCustomerId(int id, int customerId) {
        var sql = "SELECT * FROM \"funds\" WHERE \"id\" = ? AND \"customer_id\" = ?";
        RowMapper<LoadFundsRequest> rowMapper = (rs, rowNum) -> {
            int amount = rs.getInt("amount");
            var timestamp = rs.getTimestamp("time").toLocalDateTime();
            return new LoadFundsRequest(id, customerId, amount, timestamp);
        };
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id, customerId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
