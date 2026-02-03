package com.ispw.dao.impl.dbms.concrete;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

import com.ispw.dao.impl.dbms.base.DbmsDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.dao.interfaces.PagamentoDAO;
import com.ispw.model.entity.Pagamento;
import com.ispw.model.enums.MetodoPagamento;
import com.ispw.model.enums.StatoPagamento;

/**
 * DAO DBMS per Pagamento (JDBC minimale).
 * - Usa gli helper di DbmsDAO (executeUpdate/queryOne/queryList/queryExists).
 * - Mappa BigDecimal e LocalDateTime <-> SQL DECIMAL/TIMESTAMP.
 * - Nessuna logica applicativa qui: solo persistenza.
 */
public class DbmsPagamentoDAO extends DbmsDAO<Integer, Pagamento> implements PagamentoDAO {

    // =====================
    // SQL (adatta a schema)
    // =====================
    private static final String SQL_FIND_BY_ID =
        "SELECT id_pagamento, id_prenotazione, importo_finale, metodo, stato, data_pagamento " +
        "FROM pagamenti WHERE id_pagamento=?";

    private static final String SQL_EXISTS =
        "SELECT 1 FROM pagamenti WHERE id_pagamento=?";

    private static final String SQL_INSERT =
        "INSERT INTO pagamenti (id_pagamento, id_prenotazione, importo_finale, metodo, stato, data_pagamento) " +
        "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
        "UPDATE pagamenti SET id_prenotazione=?, importo_finale=?, metodo=?, stato=?, data_pagamento=? " +
        "WHERE id_pagamento=?";

    private static final String SQL_DELETE =
        "DELETE FROM pagamenti WHERE id_pagamento=?";

    // Se possono esistere più pagamenti per una prenotazione, prendo il più recente
    private static final String SQL_FIND_BY_PRENOTAZIONE =
        "SELECT id_pagamento, id_prenotazione, importo_finale, metodo, stato, data_pagamento " +
        "FROM pagamenti WHERE id_prenotazione=? " +
        "ORDER BY data_pagamento DESC";

    public DbmsPagamentoDAO(ConnectionFactory cf) {
        super(cf);
    }

    // =====================
    // RowMapper (ResultSet -> Entity)
    // =====================
    private final RowMapper<Pagamento> MAPPER = rs -> {
        Pagamento p = new Pagamento();
        p.setIdPagamento(rs.getInt("id_pagamento"));
        p.setIdPrenotazione(rs.getInt("id_prenotazione"));

        BigDecimal importo = rs.getBigDecimal("importo_finale");
        p.setImportoFinale(importo);

        String metodo = rs.getString("metodo");
        if (metodo != null) {
            try { p.setMetodo(MetodoPagamento.valueOf(metodo)); } catch (IllegalArgumentException ignore) { }
        }

        String stato = rs.getString("stato");
        if (stato != null) {
            try { p.setStato(StatoPagamento.valueOf(stato)); } catch (IllegalArgumentException ignore) { }
        }

        Timestamp ts = rs.getTimestamp("data_pagamento");
        if (ts != null) p.setDataPagamento(ts.toLocalDateTime());

        return p;
    };

    // =====================
    // Metodi base DAO
    // =====================
    @Override
    public Pagamento load(Integer id) {
        if (id == null) return null;
        return queryOne(SQL_FIND_BY_ID, ps -> ps.setInt(1, id), MAPPER).orElse(null);
    }

    @Override
    public void store(Pagamento entity) {
        if (entity == null) return;
        if (exists(entity.getIdPagamento())) {
            update(entity);
        } else {
            insert(entity);
        }
    }

    @Override
    public void delete(Integer id) {
        if (id == null) return;
        executeUpdate(SQL_DELETE, ps -> ps.setInt(1, id));
    }

    @Override
    public boolean exists(Integer id) {
        if (id == null) return false;
        return queryExists(SQL_EXISTS, ps -> ps.setInt(1, id));
    }

    @Override
    public Pagamento create(Integer id) {
        Pagamento p = new Pagamento();
        if (id != null) p.setIdPagamento(id);
        return p; // crea solo in memoria; persistilo poi con store()
    }

    // =====================
    // Finder specifico
    // =====================
    @Override
    public Pagamento findByPrenotazione(int idPrenotazione) {
        return queryOne(SQL_FIND_BY_PRENOTAZIONE, ps -> ps.setInt(1, idPrenotazione), MAPPER)
               .orElse(null);
    }

    // =====================
    // Helper insert/update
    // =====================
    private void insert(Pagamento p) {
        executeUpdate(SQL_INSERT, ps -> {
            ps.setInt(1, p.getIdPagamento());
            ps.setInt(2, p.getIdPrenotazione());
            if (p.getImportoFinale() != null) ps.setBigDecimal(3, p.getImportoFinale());
            else ps.setNull(3, Types.DECIMAL);

            ps.setString(4, p.getMetodo() != null ? p.getMetodo().name() : null);
            ps.setString(5, p.getStato()  != null ? p.getStato().name()  : null);

            LocalDateTime dt = p.getDataPagamento();
            if (dt != null) ps.setTimestamp(6, Timestamp.valueOf(dt));
            else ps.setNull(6, Types.TIMESTAMP);
        });
    }

    private void update(Pagamento p) {
        executeUpdate(SQL_UPDATE, ps -> {
            ps.setInt(1, p.getIdPrenotazione());
            if (p.getImportoFinale() != null) ps.setBigDecimal(2, p.getImportoFinale());
            else ps.setNull(2, Types.DECIMAL);

            ps.setString(3, p.getMetodo() != null ? p.getMetodo().name() : null);
            ps.setString(4, p.getStato()  != null ? p.getStato().name()  : null);

            LocalDateTime dt = p.getDataPagamento();
            if (dt != null) ps.setTimestamp(5, Timestamp.valueOf(dt));
            else ps.setNull(5, Types.TIMESTAMP);

            ps.setInt(6, p.getIdPagamento());
        });
    }
}