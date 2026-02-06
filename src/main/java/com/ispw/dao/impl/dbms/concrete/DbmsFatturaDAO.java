package com.ispw.dao.impl.dbms.concrete;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.ispw.dao.impl.dbms.base.DbmsDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.dao.interfaces.FatturaDAO;
import com.ispw.model.entity.Fattura;

/**
 * SEZIONE ARCHITETTURALE
 * Ruolo: DAO DBMS per Fattura.
 * Responsabilita': gestire accesso al DB tramite SQL e mapping.
 *
 * SEZIONE LOGICA
 * Usa DbmsDAO per eseguire query e mappare i record in entita'.
 */
public class DbmsFatturaDAO extends DbmsDAO<Integer, Fattura> implements FatturaDAO {

    private static final String TBL  = "fattura";
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

    // Niente JOIN: ricerca diretta sull’utente
    private static final String SQL_FIND_LAST_BY_UTENTE =
            "SELECT " + COLS + " FROM " + TBL + " WHERE id_utente = ? " +
            "ORDER BY data_emissione DESC, id_fattura DESC LIMIT 1";

    public DbmsFatturaDAO(ConnectionFactory cf) { super(cf); }

    private static Fattura map(ResultSet rs) throws SQLException {
        Fattura f = new Fattura();
        f.setIdFattura(rs.getInt("id_fattura"));
        f.setIdPrenotazione(rs.getInt("id_prenotazione"));
        f.setIdUtente(rs.getInt("id_utente"));                 // <--- nuovo campo
        java.sql.Date d = rs.getDate("data_emissione");
        f.setDataEmissione(d != null ? d.toLocalDate() : null);
        f.setCodiceFiscaleCliente(rs.getString("codice_fiscale_cliente"));
        f.setLinkPdf(rs.getString("link_pdf"));
        return f;
    }

    @Override
    public Fattura load(Integer id) {
        return queryOne(SQL_SELECT_ONE, ps -> ps.setInt(1, id), DbmsFatturaDAO::map).orElse(null);
    }

    @Override
    public void store(Fattura e) {
        final int id = e.getIdFattura();
        if (id > 0 && queryExists(SQL_EXISTS, ps -> ps.setInt(1, id))) {
            executeUpdate(SQL_UPDATE, ps -> {
                ps.setInt(1, e.getIdPrenotazione());
                ps.setInt(2, e.getIdUtente());                 // <--- nuovo bind
                ps.setString(3, e.getCodiceFiscaleCliente());
                if (e.getDataEmissione() != null) ps.setDate(4, java.sql.Date.valueOf(e.getDataEmissione()));
                else ps.setNull(4, java.sql.Types.DATE);
                ps.setString(5, e.getLinkPdf());
                ps.setInt(6, e.getIdFattura());
            });
        } else {
            try (var c = openConnection();
                 var ps = c.prepareStatement(SQL_INSERT, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, e.getIdPrenotazione());
                ps.setInt(2, e.getIdUtente());                 // <--- nuovo bind
                ps.setString(3, e.getCodiceFiscaleCliente());
                if (e.getDataEmissione() != null) ps.setDate(4, java.sql.Date.valueOf(e.getDataEmissione()));
                else ps.setNull(4, java.sql.Types.DATE);
                ps.setString(5, e.getLinkPdf());
                ps.executeUpdate();
                try (var gk = ps.getGeneratedKeys()) { if (gk.next()) e.setIdFattura(gk.getInt(1)); }
            } catch (SQLException ex) { throw wrap(ex); }
        }
    }

    @Override public void delete(Integer id) { executeUpdate(SQL_DELETE, ps -> ps.setInt(1, id)); }
    @Override public boolean exists(Integer id) { return queryExists(SQL_EXISTS, ps -> ps.setInt(1, id)); }
    @Override public Fattura create(Integer id) { var f = new Fattura(); f.setIdFattura(id != null ? id : 0); return f; }

    @Override
    public Fattura findLastByUtente(int idUtente) {
        return queryOne(SQL_FIND_LAST_BY_UTENTE, ps -> ps.setInt(1, idUtente), DbmsFatturaDAO::map)
               .orElse(null);
    }
}
