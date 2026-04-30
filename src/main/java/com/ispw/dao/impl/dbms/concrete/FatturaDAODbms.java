package com.ispw.dao.impl.dbms.concrete;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.ispw.dao.impl.base.BaseFatturaDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.model.entity.Fattura;

/**
 * Provider DBMS per Fattura: implementa SOLO raw I/O JDBC.
 * La cache e la logica cache-first stanno nella BaseFatturaDAO.
 */
public class FatturaDAODbms extends BaseFatturaDAO {

    private static final String TBL  = "fatture";
    private static final String COLS = "id_fattura, id_prenotazione, id_utente, codice_fiscale_cliente, data_emissione, link_pdf";

    private static final String SQL_SELECT_ONE =
            "SELECT " + COLS + " FROM " + TBL + " WHERE id_fattura = ?";

    private static final String SQL_EXISTS =
            "SELECT 1 FROM " + TBL + " WHERE id_fattura = ?";

    private static final String SQL_INSERT =
            "INSERT INTO " + TBL + " (id_prenotazione, id_utente, codice_fiscale_cliente, data_emissione, link_pdf) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE " + TBL + " SET id_prenotazione=?, id_utente=?, codice_fiscale_cliente=?, data_emissione=?, link_pdf=? " +
            "WHERE id_fattura=?";

    private static final String SQL_DELETE =
            "DELETE FROM " + TBL + " WHERE id_fattura=?";

    private static final String SQL_FIND_LAST_BY_UTENTE =
            "SELECT " + COLS + " FROM " + TBL + " WHERE id_utente = ? " +
            "ORDER BY data_emissione DESC, id_fattura DESC LIMIT 1";

    private final ConnectionFactory cf;

    public FatturaDAODbms(ConnectionFactory cf) {
        super(true);
        this.cf = cf;
    }

    @Override
    protected Fattura rawLoad(Integer id) {
        if (id == null || id <= 0) return null;
        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_SELECT_ONE)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS Fattura rawLoad", e);
        }
    }

    @Override
    protected void rawStore(Fattura e) {
        if (e == null) return;

        try (Connection c = cf.getConnection()) {

            final int id = e.getIdFattura();

            if (id > 0 && existsDb(c, id)) {
                // UPDATE
                try (PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
                    bindForUpsert(ps, e, false);
                    ps.setInt(6, e.getIdFattura());
                    ps.executeUpdate();
                }
            } else {
                // INSERT (id generato dal DB)
                try (PreparedStatement ps = c.prepareStatement(SQL_INSERT, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    bindForUpsert(ps, e, true);
                    ps.executeUpdate();

                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) e.setIdFattura(gk.getInt(1));
                    }
                }
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Errore DBMS Fattura rawStore", ex);
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
            throw new RuntimeException("Errore DBMS Fattura rawDelete", e);
        }
    }

    @Override
    protected Fattura rawFindLastByUtente(int idUtente) {
        if (idUtente <= 0) return null;

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_LAST_BY_UTENTE)) {

            ps.setInt(1, idUtente);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS Fattura rawFindLastByUtente", e);
        }
    }

    private boolean existsDb(Connection c, int idFattura) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SQL_EXISTS)) {
            ps.setInt(1, idFattura);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Mapper ResultSet -> Entity (raw) */
    private Fattura mapRow(ResultSet rs) throws SQLException {
        Fattura f = new Fattura();
        f.setIdFattura(rs.getInt("id_fattura"));
        f.setIdPrenotazione(rs.getInt("id_prenotazione"));
        f.setIdUtente(rs.getInt("id_utente"));

        Date d = rs.getDate("data_emissione");
        f.setDataEmissione(d != null ? d.toLocalDate() : null);

        f.setCodiceFiscaleCliente(rs.getString("codice_fiscale_cliente"));
        f.setLinkPdf(rs.getString("link_pdf"));
        return f;
    }

    /**
     * Bind per INSERT/UPDATE:
     * - INSERT: 5 parametri
     * - UPDATE: stessi 5 parametri + id in coda (gestito dal chiamante)
     */
    private void bindForUpsert(PreparedStatement ps, Fattura e, boolean insert) throws SQLException {
        ps.setInt(1, e.getIdPrenotazione());
        ps.setInt(2, e.getIdUtente());
        ps.setString(3, e.getCodiceFiscaleCliente());

        if (e.getDataEmissione() != null) ps.setDate(4, Date.valueOf(e.getDataEmissione()));
        else ps.setNull(4, Types.DATE);

        ps.setString(5, e.getLinkPdf());
    }
}