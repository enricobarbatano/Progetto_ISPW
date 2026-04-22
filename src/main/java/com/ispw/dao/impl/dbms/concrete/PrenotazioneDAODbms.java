package com.ispw.dao.impl.dbms.concrete;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.ispw.dao.impl.base.BasePrenotazioneDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.enums.StatoPrenotazione;

public class PrenotazioneDAODbms extends BasePrenotazioneDAO {
    // query SQL per le operazioni CRUD e di ricerca specifiche per Prenotazione
    private static final String SQL_FIND_BY_ID =
        "SELECT id_prenotazione, id_utente, id_campo, data, ora_inizio, ora_fine, stato, notifica_richiesta " +
        "FROM prenotazioni WHERE id_prenotazione=?";
    // query SQL per verificare l'esistenza di una prenotazione con un certo id
    private static final String SQL_EXISTS =
        "SELECT 1 FROM prenotazioni WHERE id_prenotazione=?";
    // query SQL per inserire una nuova prenotazione (inserisce anche l'id, che deve essere generato prima)
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

    private static final String SQL_NEXT_ID =
        "SELECT COALESCE(MAX(id_prenotazione), 0) + 1 AS next_id FROM prenotazioni";

    private final ConnectionFactory cf;

    public PrenotazioneDAODbms(ConnectionFactory cf) {
        super(true);
        this.cf = cf;
    }


    // carico nella entity una prenotazione dal db
    @Override
    protected Prenotazione rawLoad(Integer id) {
        if (id == null || id <= 0) return null;
        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS Prenotazione rawLoad", e);
        }
    }
    
    // carico nella entity tutte le prenotazioni di un utente (in base al suo id)
    @Override
    protected List<Prenotazione> rawFindByUtente(int idUtente) {
        List<Prenotazione> out = new ArrayList<>();
        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_UTENTE)) {
            ps.setInt(1, idUtente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS Prenotazione rawFindByUtente", e);
        }
        return out;
    }

    // carico nella entity tutte le prenotazioni di un utente filtrate per stato (in base al suo id)
    @Override
    protected List<Prenotazione> rawFindByUtenteAndStato(int idUtente, StatoPrenotazione stato) {
        List<Prenotazione> out = new ArrayList<>();
        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_UTENTE_STATO)) {
            ps.setInt(1, idUtente);
            ps.setString(2, stato != null ? stato.name() : null);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS Prenotazione rawFindByUtenteAndStato", e);
        }
        return out;
    }

    // aggiorno lo stato di una prenotazione (in base al suo id)
    @Override
    protected void rawUpdateStato(int idPrenotazione, StatoPrenotazione nuovoStato) {
        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_UPDATE_STATO)) {
            ps.setString(1, nuovoStato != null ? nuovoStato.name() : null);
            ps.setInt(2, idPrenotazione);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS Prenotazione rawUpdateStato", e);
        }
    }
    // cancello una prenotazione dal db (in base al suo id)
    @Override
    protected void rawDelete(Integer id) {
        if (id == null || id <= 0) return;
        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_DELETE)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS Prenotazione rawDelete", e);
        }
    }
    // salvo una prenotazione nel db (se id==0, genero un nuovo id, altrimenti aggiorno la prenotazione esistente)
    @Override
    protected void rawStore(Prenotazione entity) {
        if (entity == null) return;

        try (Connection c = cf.getConnection()) {

            // Se id==0, genera un id lato DBMS (coerente con INSERT che include id_prenotazione)
            if (entity.getIdPrenotazione() == 0) {
                entity.setIdPrenotazione(nextId(c));
            }
            // Se id!=0, verifica se esiste già una prenotazione con quell'id per decidere se fare UPDATE o INSERT

            if (existsDb(c, entity.getIdPrenotazione())) {
                try (PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
                    ps.setInt(1, entity.getIdUtente());
                    ps.setInt(2, entity.getIdCampo());
                    bindDateTime(ps, 3, 4, 5, entity);
                    bindStato(ps, 6, entity);
                    ps.setBoolean(7, entity.isNotificaRichiesta());
                    ps.setInt(8, entity.getIdPrenotazione());
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement(SQL_INSERT)) {
                    ps.setInt(1, entity.getIdPrenotazione());
                    ps.setInt(2, entity.getIdUtente());
                    ps.setInt(3, entity.getIdCampo());
                    bindDateTime(ps, 4, 5, 6, entity);
                    bindStato(ps, 7, entity);
                    ps.setBoolean(8, entity.isNotificaRichiesta());
                    ps.executeUpdate();
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS Prenotazione rawStore", e);
        }
    }
    // verifica se esiste una prenotazione con un certo id nel db
    private boolean existsDb(Connection c, int idPrenotazione) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SQL_EXISTS)) {
            ps.setInt(1, idPrenotazione);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    // genera un nuovo id per una prenotazione (calcolando il max id esistente + 1)
    private int nextId(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SQL_NEXT_ID);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("next_id") : 1;
        }
    }
    // mappa una riga del ResultSet in un oggetto Prenotazione 
    private Prenotazione mapRow(ResultSet rs) throws SQLException {
        Prenotazione p = new Prenotazione();
        p.setIdPrenotazione(rs.getInt("id_prenotazione"));
        p.setIdUtente(rs.getInt("id_utente"));
        p.setIdCampo(rs.getInt("id_campo"));

        Date d = rs.getDate("data");
        Time tStart = rs.getTime("ora_inizio");
        Time tEnd = rs.getTime("ora_fine");
        if (d != null) p.setData(d.toLocalDate());
        if (tStart != null) p.setOraInizio(tStart.toLocalTime());
        if (tEnd != null) p.setOraFine(tEnd.toLocalTime());

        String stato = rs.getString("stato");
        if (stato != null) p.setStato(StatoPrenotazione.valueOf(stato));

        p.setNotificaRichiesta(rs.getBoolean("notifica_richiesta"));
        return p;
    }
    // metodo di utilità per bindare data e orari in un PreparedStatement, gestendo anche i valori null
    private void bindDateTime(PreparedStatement ps, int idxDate, int idxStart, int idxEnd,
                              Prenotazione p) throws SQLException {
        LocalDate d = p.getData();
        LocalTime tStart = p.getOraInizio();
        LocalTime tEnd = p.getOraFine();

        if (d != null) ps.setDate(idxDate, Date.valueOf(d)); else ps.setNull(idxDate, Types.DATE);
        if (tStart != null) ps.setTime(idxStart, Time.valueOf(tStart)); else ps.setNull(idxStart, Types.TIME);
        if (tEnd != null) ps.setTime(idxEnd, Time.valueOf(tEnd)); else ps.setNull(idxEnd, Types.TIME);
    }
    // metodo di utilità per bindare lo stato in un PreparedStatement, gestendo anche il valore null
    private void bindStato(PreparedStatement ps, int idxStato, Prenotazione p) throws SQLException {
        ps.setString(idxStato, p.getStato() != null ? p.getStato().name() : null);
    }
}