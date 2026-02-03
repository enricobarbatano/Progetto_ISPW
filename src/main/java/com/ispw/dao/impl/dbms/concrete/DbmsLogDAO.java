package com.ispw.dao.impl.dbms.concrete;

import com.ispw.dao.impl.dbms.base.DbmsDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.model.entity.SystemLog;
import com.ispw.model.enums.TipoOperazione;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * DAO DBMS per SystemLog (append-only).
 * - append() = INSERT con generated key.
 * - delete() vietata (UnsupportedOperationException).
 * - findByUtente / findLast: ORDER BY timestamp DESC, id_log DESC.
 * SonarCloud: try-with-resources, no System.out, costanti, early return.
 */
public final class DbmsLogDAO extends DbmsDAO<Integer, SystemLog> implements LogDAO {

    // ======= Costanti SQL (adatta i nomi a schema reale) =======
    private static final String TBL = "system_log";
    private static final String COLS = "id_log, timestamp, tipo_operazione, id_utente_coinvolto, descrizione";

    private static final String SQL_SELECT_ONE =
            "SELECT " + COLS + " FROM " + TBL + " WHERE id_log = ?";

    private static final String SQL_INSERT =
            "INSERT INTO " + TBL + " (timestamp, tipo_operazione, id_utente_coinvolto, descrizione) VALUES (?, ?, ?, ?)";

    private static final String SQL_EXISTS =
            "SELECT 1 FROM " + TBL + " WHERE id_log = ?";

    private static final String SQL_FIND_BY_UTENTE =
            "SELECT " + COLS + " FROM " + TBL + " WHERE id_utente_coinvolto = ? " +
            "ORDER BY timestamp DESC, id_log DESC";

    private static final String SQL_FIND_LAST =
            "SELECT " + COLS + " FROM " + TBL + " ORDER BY timestamp DESC, id_log DESC LIMIT ?";

    private static final int MIN_LIMIT = 1;

    public DbmsLogDAO(ConnectionFactory cf) {
        super(cf);
    }

    // ======= RowMapper =======
    private static SystemLog map(ResultSet rs) throws SQLException {
        final SystemLog l = new SystemLog();
        l.setIdLog(rs.getInt("id_log"));

        final Timestamp ts = rs.getTimestamp("timestamp");
        l.setTimestamp(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());

        final String tipo = rs.getString("tipo_operazione");
        try {
            l.setTipoOperazione(tipo != null ? TipoOperazione.valueOf(tipo) : null);
        } catch (IllegalArgumentException e) {
            l.setTipoOperazione(null);
        }

        l.setIdUtenteCoinvolto(rs.getInt("id_utente_coinvolto"));
        l.setDescrizione(rs.getString("descrizione"));
        return l;
    }

    // ======= Implementazione LogDAO (append-only) =======

    @Override
    public void append(SystemLog log) {
        if (log == null) {
            throw new IllegalArgumentException("log non può essere null");
        }
        final Timestamp ts = Timestamp.valueOf(
                log.getTimestamp() != null ? log.getTimestamp() : LocalDateTime.now());

        try (Connection c = openConnection();
             PreparedStatement ps = c.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, ts);
            ps.setString(2, log.getTipoOperazione() != null ? log.getTipoOperazione().name() : null);
            ps.setInt(3, log.getIdUtenteCoinvolto());
            ps.setString(4, log.getDescrizione());
            ps.executeUpdate();

            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    log.setIdLog(gk.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    @Override
    public List<SystemLog> findByUtente(int idUtente) {
        return queryList(SQL_FIND_BY_UTENTE, ps -> ps.setInt(1, idUtente), DbmsLogDAO::map);
    }

    @Override
    public List<SystemLog> findLast(int limit) {
        final int safeLimit = Math.max(MIN_LIMIT, limit);
        return queryList(SQL_FIND_LAST, ps -> ps.setInt(1, safeLimit), DbmsLogDAO::map);
    }

    // ======= Metodi DAO generici (solo lettura / esistenza) =======

    @Override
    public SystemLog load(Integer id) {
        final Optional<SystemLog> r = queryOne(SQL_SELECT_ONE, ps -> ps.setInt(1, id), DbmsLogDAO::map);
        return r.orElse(null);
    }

    /** Append-only: store delega ad append (non fa update). */
    @Override
    public void store(SystemLog entity) {
        append(entity);
    }

    /** Append-only: delete vietata. */
    @Override
    public void delete(Integer id) {
        throw new UnsupportedOperationException("SystemLog è append-only: delete non consentita");
    }

    @Override
    public boolean exists(Integer id) {
        return queryExists(SQL_EXISTS, ps -> ps.setInt(1, id));
    }

    @Override
    public SystemLog create(Integer id) {
        final SystemLog l = new SystemLog();
        l.setIdLog(id != null ? id : 0);
        l.setTimestamp(LocalDateTime.now());
        return l;
    }
}
