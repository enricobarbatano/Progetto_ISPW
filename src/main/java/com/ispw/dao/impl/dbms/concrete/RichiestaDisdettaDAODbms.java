package com.ispw.dao.impl.dbms.concrete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.ispw.dao.impl.base.BaseRichiestaDisdettaDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.model.entity.RichiestaDisdettaRimborso;
import com.ispw.model.enums.StatoRichiestaDisdetta;

public class RichiestaDisdettaDAODbms extends BaseRichiestaDisdettaDAO {

    private static final String SQL_FIND_ALL =
        "SELECT id_richiesta, id_prenotazione, id_utente, " +
        "timestamp_richiesta, timestamp_decisione, penale_stimata, rimborso_stimato, " +
        "stato, nota_utente, nota_gestore, id_gestore_decisione " +
        "FROM richieste_disdetta ORDER BY id_richiesta";

    private static final String SQL_FIND_BY_ID =
        "SELECT id_richiesta, id_prenotazione, id_utente, " +
        "timestamp_richiesta, timestamp_decisione, penale_stimata, rimborso_stimato, " +
        "stato, nota_utente, nota_gestore, id_gestore_decisione " +
        "FROM richieste_disdetta WHERE id_richiesta=?";

    private static final String SQL_INSERT =
        "INSERT INTO richieste_disdetta " +
        "(id_prenotazione, id_utente, timestamp_richiesta, timestamp_decisione, " +
        " penale_stimata, rimborso_stimato, stato, nota_utente, nota_gestore, id_gestore_decisione) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
        "UPDATE richieste_disdetta SET " +
        "id_prenotazione=?, id_utente=?, timestamp_richiesta=?, timestamp_decisione=?, " +
        "penale_stimata=?, rimborso_stimato=?, stato=?, nota_utente=?, nota_gestore=?, id_gestore_decisione=? " +
        "WHERE id_richiesta=?";

    private static final String SQL_DELETE =
        "DELETE FROM richieste_disdetta WHERE id_richiesta=?";

    private static final String SQL_UPDATE_STATO =
        "UPDATE richieste_disdetta SET stato=?, timestamp_decisione=?, id_gestore_decisione=?, nota_gestore=? " +
        "WHERE id_richiesta=?";

    private final ConnectionFactory cf;

    public RichiestaDisdettaDAODbms(ConnectionFactory cf) {
        // Se la tua BaseRichiestaDisdettaDAO usa Boolean, usa super(Boolean.TRUE)
        // super(Boolean.TRUE);

        // Se invece usa boolean, usa super(true)
        super(true);

        this.cf = cf;
    }

    /* ===================== LOAD ===================== */

    @Override
    protected RichiestaDisdettaRimborso rawLoad(Integer id) {
        if (id == null || id <= 0) return null;

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_ID)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapRichiesta(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS RichiestaDisdetta rawLoad", e);
        }
    }

    @Override
    protected List<RichiestaDisdettaRimborso> rawFindAll() {
        List<RichiestaDisdettaRimborso> out = new ArrayList<>();

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(mapRichiesta(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS RichiestaDisdetta rawFindAll", e);
        }

        return out;
    }

    /* ===================== STORE ===================== */

    @Override
    protected void rawStore(RichiestaDisdettaRimborso r) {
        if (r == null) return;

        try (Connection conn = cf.getConnection()) {

            if (r.getIdRichiesta() > 0) {
                // UPDATE
                try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
                    bind(ps, r);
                    ps.setInt(11, r.getIdRichiesta());
                    ps.executeUpdate();
                }
            } else {
                // INSERT + generated key
                try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
                    bind(ps, r);
                    ps.executeUpdate();
                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) {
                            r.setIdRichiesta(gk.getInt(1));
                        }
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS RichiestaDisdetta rawStore", e);
        }
    }

    @Override
    protected void rawDelete(Integer id) {
        if (id == null || id <= 0) return;

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_DELETE)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS RichiestaDisdetta rawDelete", e);
        }
    }

    @Override
    protected void rawUpdateStato(int idRichiesta, StatoRichiestaDisdetta stato, Integer idGestore, String notaGestore) {
        if (idRichiesta <= 0 || stato == null) return;

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_UPDATE_STATO)) {

            ps.setString(1, stato.name());
            ps.setTimestamp(2, Timestamp.valueOf(java.time.LocalDateTime.now()));

            if (idGestore != null) ps.setInt(3, idGestore);
            else ps.setNull(3, Types.INTEGER);

            ps.setString(4, notaGestore);
            ps.setInt(5, idRichiesta);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS RichiestaDisdetta rawUpdateStato", e);
        }
    }

    /* ===================== HELPERS ===================== */

    private RichiestaDisdettaRimborso mapRichiesta(ResultSet rs) throws SQLException {
        RichiestaDisdettaRimborso r = new RichiestaDisdettaRimborso();
        r.setIdRichiesta(rs.getInt("id_richiesta"));
        r.setIdPrenotazione(rs.getInt("id_prenotazione"));
        r.setIdUtente(rs.getInt("id_utente"));

        Timestamp tr = rs.getTimestamp("timestamp_richiesta");
        if (tr != null) r.setTimestampRichiesta(tr.toLocalDateTime());

        Timestamp td = rs.getTimestamp("timestamp_decisione");
        if (td != null) r.setTimestampDecisione(td.toLocalDateTime());

        r.setPenaleStimata(rs.getBigDecimal("penale_stimata"));
        r.setRimborsoStimato(rs.getBigDecimal("rimborso_stimato"));

        String stato = rs.getString("stato");
        try {
            r.setStato(stato != null ? StatoRichiestaDisdetta.valueOf(stato) : StatoRichiestaDisdetta.PENDING);
        } catch (IllegalArgumentException ex) {
            r.setStato(StatoRichiestaDisdetta.PENDING);
        }

        r.setNotaUtente(rs.getString("nota_utente"));
        r.setNotaGestore(rs.getString("nota_gestore"));

        int idGestore = rs.getInt("id_gestore_decisione");
        r.setIdGestoreDecisione(rs.wasNull() ? null : idGestore);

        return r;
    }

    /** Bind campi 1..10 (uguale per INSERT e UPDATE) */
    private void bind(PreparedStatement ps, RichiestaDisdettaRimborso r) throws SQLException {
        ps.setInt(1, r.getIdPrenotazione());
        ps.setInt(2, r.getIdUtente());

        // timestamp_richiesta NOT NULL
        if (r.getTimestampRichiesta() != null) ps.setTimestamp(3, Timestamp.valueOf(r.getTimestampRichiesta()));
        else ps.setTimestamp(3, Timestamp.valueOf(java.time.LocalDateTime.now()));

        // timestamp_decisione nullable
        if (r.getTimestampDecisione() != null) ps.setTimestamp(4, Timestamp.valueOf(r.getTimestampDecisione()));
        else ps.setNull(4, Types.TIMESTAMP);

        if (r.getPenaleStimata() != null) ps.setBigDecimal(5, r.getPenaleStimata());
        else ps.setNull(5, Types.DECIMAL);

        if (r.getRimborsoStimato() != null) ps.setBigDecimal(6, r.getRimborsoStimato());
        else ps.setNull(6, Types.DECIMAL);

        StatoRichiestaDisdetta st = (r.getStato() != null) ? r.getStato() : StatoRichiestaDisdetta.PENDING;
        ps.setString(7, st.name());

        ps.setString(8, r.getNotaUtente());
        ps.setString(9, r.getNotaGestore());

        if (r.getIdGestoreDecisione() != null) ps.setInt(10, r.getIdGestoreDecisione());
        else ps.setNull(10, Types.INTEGER);
    }
}