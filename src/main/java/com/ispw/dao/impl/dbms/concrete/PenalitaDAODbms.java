package com.ispw.dao.impl.dbms.concrete;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.ispw.dao.exception.DaoException;
import com.ispw.dao.impl.base.BasePenalitaDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.model.entity.Penalita;
import com.ispw.model.enums.StatoPenalita;

/**
 * Provider DBMS per Penalita.
 * Implementa solo raw I/O JDBC.
 */
public class PenalitaDAODbms extends BasePenalitaDAO {

    private static final String SQL_FIND_BY_ID =
            "SELECT id_penalita, id_utente, data_emissione, importo, motivazione, stato " +
            "FROM penalita WHERE id_penalita=?";

    private static final String SQL_FIND_BY_UTENTE =
            "SELECT id_penalita, id_utente, data_emissione, importo, motivazione, stato " +
            "FROM penalita WHERE id_utente=? ORDER BY data_emissione DESC, id_penalita DESC";

    private static final String SQL_INSERT =
            "INSERT INTO penalita (id_utente, data_emissione, importo, motivazione, stato) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE penalita SET id_utente=?, data_emissione=?, importo=?, motivazione=?, stato=? " +
            "WHERE id_penalita=?";

    private static final String SQL_DELETE =
            "DELETE FROM penalita WHERE id_penalita=?";

    private final ConnectionFactory cf;

    public PenalitaDAODbms(ConnectionFactory cf) {
        super(true);
        this.cf = cf;
    }

    @Override
    protected Penalita rawLoad(Integer id) {
        if (id == null || id <= 0) {
            return null;
        }

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_ID)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }

        } catch (SQLException e) {
            throw new DaoException("Errore DBMS Penalita rawLoad", e);
        }
    }

    @Override
    protected List<Penalita> rawFindByUtente(int idUtente) {
        List<Penalita> out = new ArrayList<>();

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_UTENTE)) {

            ps.setInt(1, idUtente);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }

        } catch (SQLException e) {
            throw new DaoException("Errore DBMS Penalita rawFindByUtente", e);
        }

        return out;
    }

    @Override
    protected void rawStore(Penalita p) {
        if (p == null) {
            return;
        }

        try (Connection c = cf.getConnection()) {

            if (p.getIdPenalita() > 0) {
                // UPDATE se la penalità ha già un id.
                try (PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
                    bind(ps, p);
                    ps.setInt(6, p.getIdPenalita());
                    ps.executeUpdate();
                }
            } else {
                // INSERT con generated key.
                try (PreparedStatement ps = c.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
                    bind(ps, p);
                    ps.executeUpdate();

                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) {
                            p.setIdPenalita(gk.getInt(1));
                        }
                    }
                }
            }

        } catch (SQLException e) {
            throw new DaoException("Errore DBMS Penalita rawStore", e);
        }
    }

    @Override
    protected void rawDelete(Integer id) {
        if (id == null || id <= 0) {
            return;
        }

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_DELETE)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DaoException("Errore DBMS Penalita rawDelete", e);
        }
    }

    private Penalita map(ResultSet rs) throws SQLException {
        Penalita p = new Penalita();

        p.setIdPenalita(rs.getInt("id_penalita"));
        p.setIdUtente(rs.getInt("id_utente"));

        Date d = rs.getDate("data_emissione");
        if (d != null) {
            p.setDataEmissione(d.toLocalDate());
        }

        p.setImporto(rs.getBigDecimal("importo"));
        p.setMotivazione(rs.getString("motivazione"));

        String stato = rs.getString("stato");
        if (stato != null) {
            try {
                p.setStato(StatoPenalita.valueOf(stato));
            } catch (IllegalArgumentException ignore) {
                // Valore stato non valido nel DB: lascio lo stato non valorizzato.
            }
        }

        return p;
    }

    private void bind(PreparedStatement ps, Penalita p) throws SQLException {
        ps.setInt(1, p.getIdUtente());

        if (p.getDataEmissione() != null) {
            ps.setDate(2, Date.valueOf(p.getDataEmissione()));
        } else {
            ps.setNull(2, Types.DATE);
        }

        ps.setBigDecimal(3, p.getImporto());
        ps.setString(4, p.getMotivazione());
        ps.setString(5, p.getStato() != null ? p.getStato().name() : null);
    }
}

