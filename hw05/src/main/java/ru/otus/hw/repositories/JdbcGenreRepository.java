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
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcGenreRepository implements GenreRepository {

    private final NamedParameterJdbcOperations jdbcTemplate;

    @Override
    public List<Genre> findAll() {
        try {
            return jdbcTemplate.query("SELECT id, name FROM genres", new GenreRowMapper());
        } catch (DataAccessException e) {
            log.error("Error while finding all genres: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public List<Genre> findByIds(Set<Long> ids) {
        try {
            SqlParameterSource params = new MapSqlParameterSource().addValue("ids", ids);
            return jdbcTemplate.query("SELECT id, name FROM genres WHERE id IN (:ids)", params,
                    new GenreRowMapper());
        } catch (DataAccessException e) {
            log.error("Error while finding genres by ids: {}, {}", ids, e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public Optional<Genre> findById(long id) {
        try {
            SqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
            return jdbcTemplate.queryForStream("SELECT id, name FROM genres WHERE id = :id", params,
                            new GenreRowMapper())
                    .findFirst();
        } catch (DataAccessException e) {
            log.error("Error while finding genres by id: {}, {}", id, e.getMessage());
        }
        return Optional.empty();
    }

    private static class GenreRowMapper implements RowMapper<Genre> {
        @Nullable
        @Override
        public Genre mapRow(ResultSet resultSet, int rowNum) {
            try {
                return new DataClassRowMapper<>(Genre.class).mapRow(resultSet, rowNum);
            } catch (SQLException e) {
                log.error("Error while mapping genre row: {}", e.getMessage());
            }
            return null;
        }
    }
}
