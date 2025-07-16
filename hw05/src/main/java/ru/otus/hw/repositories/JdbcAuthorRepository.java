package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Author;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcAuthorRepository implements AuthorRepository {

    private final NamedParameterJdbcOperations jdbcTemplate;

    @Override
    public List<Author> findAll() {
        try {
            return jdbcTemplate.query("SELECT id, full_name FROM authors", new AuthorRowMapper());
        } catch (DataAccessException e) {
            log.error("Error while finding all authors: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public Optional<Author> findById(long id) {
        try {
            SqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
            return jdbcTemplate.queryForStream("SELECT id, full_name FROM authors WHERE id = :id", params,
                            new AuthorRowMapper())
                    .findFirst();
        } catch (DataAccessException e) {
            log.error("Error while finding author by id: {}, {}", id, e.getMessage());
        }
        return Optional.empty();
    }

    private static class AuthorRowMapper implements RowMapper<Author> {
        @Nullable
        @Override
        public Author mapRow(ResultSet resultSet, int rowNum) {
            try {
                return new DataClassRowMapper<>(Author.class).mapRow(resultSet, rowNum);
            } catch (SQLException e) {
                log.error("Error while mapping author row: {}", e.getMessage());
            }
            return null;
        }
    }
}
