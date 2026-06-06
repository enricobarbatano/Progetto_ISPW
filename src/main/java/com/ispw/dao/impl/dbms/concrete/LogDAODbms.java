package com.ispw.dao.impl.dbms.concrete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ispw.dao.exception.DaoException;
import com.ispw.dao.impl.base.BaseLogDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.model.entity.SystemLog;
import com.ispw.model.enums.TipoOperazione;

/**
 * Provider DBMS per SystemLog.
 * Implementa solo raw I/O JDBC.
 */
public class LogDAODbms extends BaseLogDAO {

    private static final String TBL = "system_log";
    private static final String COLS = "id_log, timestamp, tipo_operazione, id_utente_coinvolto, descrizione";

    private static final String SELECT = "SELECT ";
    private static final String FROM = " FROM ";

    private static final String SQL_INSERT =
            "INSERT INTO " + TBL +
            " (timestamp, tipo_operazione, id_utente_coinvolto, descrizione) VALUES (?, ?, ?, ?)";

    private static final String SQL_FIND_BY_ID =
            SELECT + COLS + FROM + TBL + " WHERE id_log=?";

    private static final String SQL_FIND_BY_UTENTE =
            SELECT + COLS + FROM + TBL +
            " WHERE id_utente_coinvolto=? ORDER BY timestamp DESC, id_log DESC";

    private static final String SQL_FIND_LAST =
            SELECT + COLS + FROM + TBL +
            " ORDER BY timestamp DESC, id_log DESC LIMIT ?";

    private final ConnectionFactory cf;

    public LogDAODbms(ConnectionFactory cf) {
        super(true);
        this.cf = cf;
    }

    @Override
    protected void rawAppend(SystemLog log) {
        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, Timestamp.valueOf(log.getTimestamp()));
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
            throw new DaoException("Errore DBMS Log rawAppend", e);
        }
    }

    @Override
    protected SystemLog rawLoad(Integer id) {
        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_ID)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }

        } catch (SQLException e) {
            throw new DaoException("Errore DBMS Log rawLoad", e);
        }
    }

    @Override
    protected List<SystemLog> rawFindByUtente(int idUtente) {
        List<SystemLog> out = new ArrayList<>();

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_UTENTE)) {

            ps.setInt(1, idUtente);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }

        } catch (SQLException e) {
            throw new DaoException("Errore DBMS Log rawFindByUtente", e);
        }

        return out;
    }

    @Override
    protected List<SystemLog> rawFindLast(int limit) {
        List<SystemLog> out = new ArrayList<>();

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_LAST)) {

            ps.setInt(1, Math.max(1, limit));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }

        } catch (SQLException e) {
            throw new DaoException("Errore DBMS Log rawFindLast", e);
        }

        return out;
    }

    private SystemLog map(ResultSet rs) throws SQLException {
        SystemLog log = new SystemLog();

        log.setIdLog(rs.getInt("id_log"));

        Timestamp ts = rs.getTimestamp("timestamp");
        log.setTimestamp(ts != null ? ts.toLocalDateTime() : LocalDateTime.from(ZonedDateTime.now(ZoneId.systemDefault())));

        String tipo = rs.getString("tipo_operazione");
        if (tipo != null) {
            try {
                log.setTipoOperazione(TipoOperazione.valueOf(tipo));
            } catch (IllegalArgumentException ignore) {
                // Valore enum non valido nel DB: lascio tipoOperazione non valorizzato.
            }
        }

        log.setIdUtenteCoinvolto(rs.getInt("id_utente_coinvolto"));
        log.setDescrizione(rs.getString("descrizione"));

        return log;
    }
}

