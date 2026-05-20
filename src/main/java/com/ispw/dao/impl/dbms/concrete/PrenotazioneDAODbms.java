package com.ispw.dao.impl.dbms.concrete;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.ispw.dao.exception.DaoException;
import com.ispw.dao.impl.base.BasePrenotazioneDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.enums.StatoPrenotazione;

/**
 * Provider DBMS per Prenotazione.
 * Implementa solo raw I/O JDBC.
 * Cache-first e composizione A2 restano in BasePrenotazioneDAO.
 */
public class PrenotazioneDAODbms extends BasePrenotazioneDAO {

    private static final String SQL_FIND_BY_ID =
            "SELECT id_prenotazione, id_utente, id_campo, data, ora_inizio, ora_fine, stato, notifica_richiesta " +
            "FROM prenotazioni WHERE id_prenotazione=?";

    private static final String SQL_FIND_BY_UTENTE =
            "SELECT id_prenotazione, id_utente, id_campo, data, ora_inizio, ora_fine, stato, notifica_richiesta " +
            "FROM prenotazioni WHERE id_utente=? ORDER BY data, ora_inizio";

    private static final String SQL_FIND_BY_UTENTE_STATO =
            "SELECT id_prenotazione, id_utente, id_campo, data, ora_inizio, ora_fine, stato, notifica_richiesta " +
            "FROM prenotazioni WHERE id_utente=? AND stato=? ORDER BY data, ora_inizio";

    private static final String SQL_FIND_BY_CAMPO =
            "SELECT id_prenotazione, id_utente, id_campo, data, ora_inizio, ora_fine, stato, notifica_richiesta " +
            "FROM prenotazioni WHERE id_campo=? ORDER BY data, ora_inizio, id_prenotazione";

    private static final String SQL_INSERT =
            "INSERT INTO prenotazioni (id_utente, id_campo, data, ora_inizio, ora_fine, stato, notifica_richiesta) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE prenotazioni SET id_utente=?, id_campo=?, data=?, ora_inizio=?, ora_fine=?, stato=?, notifica_richiesta=? " +
            "WHERE id_prenotazione=?";

    private static final String SQL_DELETE =
            "DELETE FROM prenotazioni WHERE id_prenotazione=?";

    private static final String SQL_UPDATE_STATO =
            "UPDATE prenotazioni SET stato=? WHERE id_prenotazione=?";

    private final ConnectionFactory cf;

    public PrenotazioneDAODbms(ConnectionFactory cf) {
        super(true);
        this.cf = cf;
    }

    @Override
    protected Prenotazione rawLoad(Integer id) {
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
            throw new DaoException("Errore DBMS Prenotazione rawLoad", e);
        }
    }

    @Override
    protected List<Prenotazione> rawFindByUtente(int idUtente) {
        List<Prenotazione> out = new ArrayList<>();

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_UTENTE)) {

            ps.setInt(1, idUtente);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }

        } catch (SQLException e) {
            throw new DaoException("Errore DBMS Prenotazione rawFindByUtente", e);
        }

        return out;
    }

    @Override
    protected List<Prenotazione> rawFindByUtenteAndStato(int idUtente, StatoPrenotazione stato) {
        List<Prenotazione> out = new ArrayList<>();

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_UTENTE_STATO)) {

            ps.setInt(1, idUtente);
            ps.setString(2, stato != null ? stato.name() : null);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }

        } catch (SQLException e) {
            throw new DaoException("Errore DBMS Prenotazione rawFindByUtenteAndStato", e);
        }

        return out;
    }

    @Override
    protected List<Prenotazione> rawFindByCampo(int idCampo) {
        List<Prenotazione> out = new ArrayList<>();

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_CAMPO)) {

            ps.setInt(1, idCampo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }

        } catch (SQLException e) {
            throw new DaoException("Errore DBMS Prenotazione rawFindByCampo", e);
        }

        return out;
    }

    @Override
    protected void rawUpdateStato(int idPrenotazione, StatoPrenotazione nuovoStato) {
        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_UPDATE_STATO)) {

            ps.setString(1, nuovoStato != null ? nuovoStato.name() : null);
            ps.setInt(2, idPrenotazione);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DaoException("Errore DBMS Prenotazione rawUpdateStato", e);
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
            throw new DaoException("Errore DBMS Prenotazione rawDelete", e);
        }
    }

    @Override
    protected void rawStore(Prenotazione p) {
        if (p == null) {
            return;
        }

        try (Connection c = cf.getConnection()) {

            if (p.getIdPrenotazione() > 0) {
                // UPDATE se la prenotazione ha già un id.
                try (PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
                    bind(ps, p);
                    ps.setInt(8, p.getIdPrenotazione());
                    ps.executeUpdate();
                }
            } else {
                // INSERT con generated key.
                try (PreparedStatement ps = c.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
                    bind(ps, p);
                    ps.executeUpdate();

                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) {
                            p.setIdPrenotazione(gk.getInt(1));
                        }
                    }
                }
            }

        } catch (SQLException e) {
            throw new DaoException("Errore DBMS Prenotazione rawStore", e);
        }
    }

    private Prenotazione map(ResultSet rs) throws SQLException {
        Prenotazione p = new Prenotazione();

        p.setIdPrenotazione(rs.getInt("id_prenotazione"));
        p.setIdUtente(rs.getInt("id_utente"));
        p.setIdCampo(rs.getInt("id_campo"));

        Date d = rs.getDate("data");
        if (d != null) {
            p.setData(d.toLocalDate());
        }

        Time tStart = rs.getTime("ora_inizio");
        if (tStart != null) {
            p.setOraInizio(tStart.toLocalTime());
        }

        Time tEnd = rs.getTime("ora_fine");
        if (tEnd != null) {
            p.setOraFine(tEnd.toLocalTime());
        }

        String stato = rs.getString("stato");
        if (stato != null) {
            p.setStato(StatoPrenotazione.valueOf(stato));
        }

        p.setNotificaRichiesta(rs.getBoolean("notifica_richiesta"));

        return p;
    }

    private void bind(PreparedStatement ps, Prenotazione p) throws SQLException {
        ps.setInt(1, p.getIdUtente());
        ps.setInt(2, p.getIdCampo());

        if (p.getData() != null) {
            ps.setDate(3, Date.valueOf(p.getData()));
        } else {
            ps.setNull(3, Types.DATE);
        }

        if (p.getOraInizio() != null) {
            ps.setTime(4, Time.valueOf(p.getOraInizio()));
        } else {
            ps.setNull(4, Types.TIME);
        }

        if (p.getOraFine() != null) {
            ps.setTime(5, Time.valueOf(p.getOraFine()));
        } else {
            ps.setNull(5, Types.TIME);
        }

        ps.setString(6, p.getStato() != null ? p.getStato().name() : null);
        ps.setBoolean(7, p.isNotificaRichiesta());
    }
}
