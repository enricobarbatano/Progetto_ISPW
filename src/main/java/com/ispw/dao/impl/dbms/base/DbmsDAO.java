package com.ispw.dao.impl.dbms.base;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.dao.interfaces.DAO;

public abstract class DbmsDAO<I, E> implements DAO<I, E> {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: base DAO DBMS con ConnectionFactory.
    // A2) IO: helper JDBC riusabili.

    
    @FunctionalInterface
    protected interface StatementBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    @FunctionalInterface
    protected interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    protected final ConnectionFactory cf;

    protected DbmsDAO(ConnectionFactory cf) {
        this.cf = cf;
    }

    protected Connection openConnection() throws SQLException {
        return cf.getConnection();
    }

    protected RuntimeException wrap(SQLException e) {
        return new RuntimeException("Errore DBMS DAO: " + e.getMessage(), e);
    }

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) executeUpdate/queryOne/queryList/queryExists: helper JDBC.
    // L2) openConnection/wrap: accesso e gestione errori.

    protected int executeUpdate(String sql, StatementBinder binder) {
        try (Connection c = openConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            if (binder != null) binder.bind(ps);
            return ps.executeUpdate();

        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    protected Optional<E> queryOne(String sql, StatementBinder binder, RowMapper<E> mapper) {
        try (Connection c = openConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            if (binder != null) binder.bind(ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.ofNullable(mapper.map(rs));
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    protected List<E> queryList(String sql, StatementBinder binder, RowMapper<E> mapper) {
        try (Connection c = openConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            if (binder != null) binder.bind(ps);

            try (ResultSet rs = ps.executeQuery()) {
                List<E> out = new ArrayList<>();
                while (rs.next()) out.add(mapper.map(rs));
                return out;
            }

        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    protected boolean queryExists(String sql, StatementBinder binder) {
        try (Connection c = openConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            if (binder != null) binder.bind(ps);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw wrap(e);
        }
    }
}

