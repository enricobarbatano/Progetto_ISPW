package com.ispw.dao.impl.dbms.concrete;

import java.sql.Date;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.ispw.dao.impl.dbms.base.DbmsDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.dao.interfaces.PrenotazioneDAO;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.enums.StatoPrenotazione;

/**
 * SEZIONE ARCHITETTURALE
 * Ruolo: DAO DBMS per Prenotazione.
 * Responsabilita': gestire accesso al DB tramite SQL e mapping.
 *
 * SEZIONE LOGICA
 * Usa DbmsDAO per eseguire query e mappare i record in entita'.
 */
public class DbmsPrenotazioneDAO extends DbmsDAO<Integer, Prenotazione> implements PrenotazioneDAO {

    // SQL (adatta allo schema)
    private static final String SQL_FIND_BY_ID =
        "SELECT id_prenotazione, id_utente, id_campo, data, ora_inizio, ora_fine, stato, notifica_richiesta " +
        "FROM prenotazioni WHERE id_prenotazione=?";

    private static final String SQL_EXISTS =
        "SELECT 1 FROM prenotazioni WHERE id_prenotazione=?";

    private static final String SQL_INSERT =
        "INSERT INTO prenotazioni (id_prenotazione, id_utente, id_campo, data, ora_inizio, ora_fine, stato, notifica_richiesta) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
        "UPDATE prenotazioni SET id_utente=?, id_campo=?, data=?, ora_inizio=?, ora_fine=?, stato=?, notifica_richiesta=? " +
        "WHERE id_prenotazione=?";

    private static final String SQL_DELETE =
        "DELETE FROM prenotazioni WHERE id_prenotazione=?";

    private static final String SQL_FIND_BY_UTENTE =
        "SELECT id_prenotazione, id_utente, id_campo, data, ora_inizio, ora_fine, stato, notifica_richiesta " +
        "FROM prenotazioni WHERE id_utente=? ORDER BY data, ora_inizio";

    private static final String SQL_FIND_BY_UTENTE_STATO =
        "SELECT id_prenotazione, id_utente, id_campo, data, ora_inizio, ora_fine, stato, notifica_richiesta " +
        "FROM prenotazioni WHERE id_utente=? AND stato=? ORDER BY data, ora_inizio";

    private static final String SQL_UPDATE_STATO =
        "UPDATE prenotazioni SET stato=? WHERE id_prenotazione=?";

    public DbmsPrenotazioneDAO(ConnectionFactory cf) {
        super(cf);
    }

    // RowMapper
    private final RowMapper<Prenotazione> mapper= rs -> {
        Prenotazione p = new Prenotazione();
        p.setIdPrenotazione(rs.getInt("id_prenotazione"));
        p.setIdUtente(rs.getInt("id_utente"));
        p.setIdCampo(rs.getInt("id_campo"));

        Date d = rs.getDate("data");
        Time tStart = rs.getTime("ora_inizio");
        Time tEnd   = rs.getTime("ora_fine");
        if (d != null) p.setData(d.toLocalDate());
        if (tStart != null) p.setOraInizio(tStart.toLocalTime());
        if (tEnd != null) p.setOraFine(tEnd.toLocalTime());

        String stato = rs.getString("stato");
        if (stato != null) p.setStato(StatoPrenotazione.valueOf(stato));

        p.setNotificaRichiesta(rs.getBoolean("notifica_richiesta"));
        return p;
    };

    // Metodi DAO base
    @Override
    public Prenotazione load(Integer id) {
        if (id == null) return null;
        return queryOne(SQL_FIND_BY_ID, ps -> ps.setInt(1, id), mapper).orElse(null);
    }

    @Override
    public Prenotazione findById(int idPrenotazione) {
        return load(idPrenotazione);
    }

    @Override
    public void store(Prenotazione entity) {
        if (entity == null) return;
        if (exists(entity.getIdPrenotazione())) {
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
    public Prenotazione create(Integer id) {
        Prenotazione p = new Prenotazione();
        if (id != null) p.setIdPrenotazione(id);
        return p; // solo in memoria; persistilo poi con store()
    }

    // Finder specifici
    @Override
    public List<Prenotazione> findByUtente(int idUtente) {
        return queryList(SQL_FIND_BY_UTENTE, ps -> ps.setInt(1, idUtente), mapper);
    }

    @Override
    public List<Prenotazione> findByUtenteAndStato(int idUtente, StatoPrenotazione stato) {
        return queryList(SQL_FIND_BY_UTENTE_STATO, ps -> {
            ps.setInt(1, idUtente);
            ps.setString(2, stato.name());
        }, mapper);
    }

    @Override
    public void updateStato(int idPrenotazione, StatoPrenotazione nuovoStato) {
        executeUpdate(SQL_UPDATE_STATO, ps -> {
            ps.setString(1, nuovoStato.name());
            ps.setInt(2, idPrenotazione);
        });
    }

    // Helper interni
    private void insert(Prenotazione p) {
        executeUpdate(SQL_INSERT, ps -> {
            ps.setInt(1, p.getIdPrenotazione());
            ps.setInt(2, p.getIdUtente());
            ps.setInt(3, p.getIdCampo());
            bindDateTime(ps, 4, 5, 6, p);
            bindStato(ps, 7, p);
            ps.setBoolean(8, p.isNotificaRichiesta());
        });
    }

    private void update(Prenotazione p) {
        executeUpdate(SQL_UPDATE, ps -> {
            ps.setInt(1, p.getIdUtente());
            ps.setInt(2, p.getIdCampo());
            bindDateTime(ps, 3, 4, 5, p);
            bindStato(ps, 6, p);
            ps.setBoolean(7, p.isNotificaRichiesta());
            ps.setInt(8, p.getIdPrenotazione());
        });
    }

    private void bindDateTime(java.sql.PreparedStatement ps, int idxDate, int idxStart, int idxEnd,
                              Prenotazione p) throws java.sql.SQLException {
        LocalDate d = p.getData();
        LocalTime tStart = p.getOraInizio();
        LocalTime tEnd = p.getOraFine();

        if (d != null) ps.setDate(idxDate, Date.valueOf(d)); else ps.setNull(idxDate, Types.DATE);
        if (tStart != null) ps.setTime(idxStart, Time.valueOf(tStart)); else ps.setNull(idxStart, Types.TIME);
        if (tEnd != null) ps.setTime(idxEnd, Time.valueOf(tEnd)); else ps.setNull(idxEnd, Types.TIME);
    }

    private void bindStato(java.sql.PreparedStatement ps, int idxStato, Prenotazione p) throws java.sql.SQLException {
        ps.setString(idxStato, p.getStato() != null ? p.getStato().name() : null);
    }
}
