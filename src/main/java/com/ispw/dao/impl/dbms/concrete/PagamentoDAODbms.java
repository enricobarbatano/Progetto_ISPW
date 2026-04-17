package com.ispw.dao.impl.dbms.concrete;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

import com.ispw.dao.impl.base.BasePagamentoDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.dao.interfaces.PagamentoDAO;
import com.ispw.model.entity.Pagamento;
import com.ispw.model.enums.MetodoPagamento;
import com.ispw.model.enums.StatoPagamento;

/**
 * Pagamento DAO for DBMS as subclass of BasePagamentoDAO.
 * Implements only raw I/O (JDBC) while leveraging BasePagamentoDAO cache/template.
 */
public class PagamentoDAODbms extends BasePagamentoDAO implements PagamentoDAO {

    private static final String SQL_FIND_BY_ID =
        "SELECT id_pagamento, id_prenotazione, importo_finale, metodo, stato, data_pagamento " +
        "FROM pagamenti WHERE id_pagamento=?";

    private static final String SQL_INSERT =
        "INSERT INTO pagamenti (id_prenotazione, importo_finale, metodo, stato, data_pagamento) " +
        "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
        "UPDATE pagamenti SET id_prenotazione=?, importo_finale=?, metodo=?, stato=?, data_pagamento=? " +
        "WHERE id_pagamento=?";

    private static final String SQL_DELETE =
        "DELETE FROM pagamenti WHERE id_pagamento=?";

    private static final String SQL_FIND_BY_PRENOTAZIONE =
        "SELECT id_pagamento, id_prenotazione, importo_finale, metodo, stato, data_pagamento " +
        "FROM pagamenti WHERE id_prenotazione=? " +
        "ORDER BY data_pagamento DESC, id_pagamento DESC";

    private final ConnectionFactory cf;

    public PagamentoDAODbms(ConnectionFactory cf) {
        super(true); // persistent
        this.cf = cf;
    }

    // Row mapping helper
    private Pagamento mapRow(ResultSet rs) throws SQLException {
        Pagamento p = new Pagamento();
        p.setIdPagamento(rs.getInt("id_pagamento"));
        p.setIdPrenotazione(rs.getInt("id_prenotazione"));

        BigDecimal importo = rs.getBigDecimal("importo_finale");
        p.setImportoFinale(importo);

        String metodo = rs.getString("metodo");
        if (metodo != null) {
            try { p.setMetodo(MetodoPagamento.valueOf(metodo)); }
            catch (IllegalArgumentException ignore) { /* ignore invalid enum */ }
        }

        String stato = rs.getString("stato");
        if (stato != null) {
            try { p.setStato(StatoPagamento.valueOf(stato)); }
            catch (IllegalArgumentException ignore) { /* ignore invalid enum */ }
        }

        Timestamp ts = rs.getTimestamp("data_pagamento");
        if (ts != null) p.setDataPagamento(ts.toLocalDateTime());

        return p;
    }

    @Override
    protected Pagamento rawLoad(Integer id) {
        if (id == null) return null;
        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS Pagamento load: " + e.getMessage(), e);
        }
    }

    @Override
    protected void rawStore(Pagamento entity) {
        if (entity == null) return;
        if (entity.getIdPagamento() == 0) {
            // insert and retrieve generated id
            try (Connection c = cf.getConnection();
                 PreparedStatement ps = c.prepareStatement(SQL_INSERT, PreparedStatement.RETURN_GENERATED_KEYS)) {
                bindPagamento(ps, entity, 1);
                ps.executeUpdate();
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        entity.setIdPagamento(gk.getInt(1));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Errore DBMS Pagamento insert: " + e.getMessage(), e);
            }
        } else {
            // update
            try (Connection c = cf.getConnection();
                 PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
                bindPagamento(ps, entity, 1);
                ps.setInt(6, entity.getIdPagamento());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Errore DBMS Pagamento update: " + e.getMessage(), e);
            }
        }
    }

    @Override
    protected void rawDelete(Integer id) {
        if (id == null) return;
        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_DELETE)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS Pagamento delete: " + e.getMessage(), e);
        }
    }

    @Override
    protected Pagamento rawFindByPrenotazione(int idPrenotazione) {
        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_PRENOTAZIONE)) {
            ps.setInt(1, idPrenotazione);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS Pagamento findByPrenotazione: " + e.getMessage(), e);
        }
    }

    private void bindPagamento(PreparedStatement ps, Pagamento p, int startIndex) throws SQLException {
        ps.setInt(startIndex, p.getIdPrenotazione());
        if (p.getImportoFinale() != null) ps.setBigDecimal(startIndex + 1, p.getImportoFinale());
        else ps.setNull(startIndex + 1, Types.DECIMAL);

        ps.setString(startIndex + 2, p.getMetodo() != null ? p.getMetodo().name() : null);
        ps.setString(startIndex + 3, p.getStato()  != null ? p.getStato().name()  : null);

        LocalDateTime dt = p.getDataPagamento();
        if (dt != null) ps.setTimestamp(startIndex + 4, Timestamp.valueOf(dt));
        else ps.setNull(startIndex + 4, Types.TIMESTAMP);
    }
}
