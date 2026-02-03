package com.ispw.dao.impl.dbms.concrete;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Optional;

import com.ispw.dao.impl.dbms.base.DbmsDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.dao.interfaces.FatturaDAO;
import com.ispw.model.entity.Fattura;
/**
 * DAO DBMS per Fattura.
 * - Persistenza su database relazionale via JDBC.
 * - SQL minimale.
 */

public class DbmsFatturaDAO extends DbmsDAO<Integer, Fattura> implements FatturaDAO {

    // ----------------------
    // SQL (minimale)
    // ----------------------
    private static final String TBL = "fattura";
    private static final String COLS = "id_fattura, id_prenotazione, codice_fiscale_cliente, data_emissione, link_pdf";

    private static final String SQL_SELECT_ONE =
            "SELECT " + COLS + " FROM " + TBL + " WHERE id_fattura = ?";

    private static final String SQL_EXISTS =
            "SELECT 1 FROM " + TBL + " WHERE id_fattura = ?";

    private static final String SQL_INSERT =
            "INSERT INTO " + TBL + " (id_prenotazione, codice_fiscale_cliente, data_emissione, link_pdf) VALUES (?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE " + TBL + " SET id_prenotazione = ?, codice_fiscale_cliente = ?, data_emissione = ?, link_pdf = ? WHERE id_fattura = ?";

    private static final String SQL_DELETE =
            "DELETE FROM " + TBL + " WHERE id_fattura = ?";

    // NB: usa LIMIT 1 (MySQL/PostgreSQL). Per DB diversi, sostituire con clausole equivalenti (es. FETCH FIRST 1 ROW ONLY).
    private static final String SQL_FIND_LAST_BY_UTENTE =
            "SELECT " + COLS + " " +
            "FROM " + TBL + " f " +
            "JOIN prenotazione p ON p.id_prenotazione = f.id_prenotazione " +
            "WHERE p.id_utente = ? " +
            "ORDER BY f.data_emissione DESC, f.id_fattura DESC " +
            "LIMIT 1";

    public DbmsFatturaDAO(ConnectionFactory cf) {
        super(cf);
    }

    // ----------------------
    // RowMapper
    // ----------------------
    private static Fattura map(ResultSet rs) throws SQLException {
        Fattura f = new Fattura();
        f.setIdFattura(rs.getInt("id_fattura"));
        f.setIdPrenotazione(rs.getInt("id_prenotazione"));
        f.setCodiceFiscaleCliente(rs.getString("codice_fiscale_cliente"));
        Date d = rs.getDate("data_emissione");
        f.setDataEmissione((d != null) ? d.toLocalDate() : null);
        f.setLinkPdf(rs.getString("link_pdf"));
        return f;
    }

    // ----------------------
    // Implementazione DAO
    // ----------------------

    @Override
    public Fattura load(Integer id) {
        Optional<Fattura> r = queryOne(SQL_SELECT_ONE,
                ps -> ps.setInt(1, id),
                DbmsFatturaDAO::map);
        return r.orElse(null);
    }

    @Override
    public void store(Fattura entity) {
        final int id = entity.getIdFattura();
        if (id > 0 && queryExists(SQL_EXISTS, ps -> ps.setInt(1, id))) {
            // UPDATE
            executeUpdate(SQL_UPDATE, ps -> {
                ps.setInt(1, entity.getIdPrenotazione());
                ps.setString(2, entity.getCodiceFiscaleCliente());
                if (entity.getDataEmissione() != null) {
                    ps.setDate(3, Date.valueOf(entity.getDataEmissione()));
                } else {
                    ps.setNull(3, Types.DATE);
                }
                ps.setString(4, entity.getLinkPdf());
                ps.setInt(5, entity.getIdFattura());
            });
        } else {
            // INSERT con generated key
            try (Connection c = openConnection();
                 PreparedStatement ps = c.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, entity.getIdPrenotazione());
                ps.setString(2, entity.getCodiceFiscaleCliente());
                if (entity.getDataEmissione() != null) {
                    ps.setDate(3, Date.valueOf(entity.getDataEmissione()));
                } else {
                    ps.setNull(3, Types.DATE);
                }
                ps.setString(4, entity.getLinkPdf());

                ps.executeUpdate();
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        entity.setIdFattura(gk.getInt(1));
                    }
                }
            } catch (SQLException e) {
                throw wrap(e);
            }
        }
    }

    @Override
    public void delete(Integer id) {
        executeUpdate(SQL_DELETE, ps -> ps.setInt(1, id));
    }

    @Override
    public boolean exists(Integer id) {
        return queryExists(SQL_EXISTS, ps -> ps.setInt(1, id));
    }

    @Override
    public Fattura create(Integer id) {
        // Facoltativo: crea "vuoto" (non persistito). Utile in alcuni scenari.
        Fattura f = new Fattura();
        f.setIdFattura(id != null ? id : 0);
        return f;
    }

    // ----------------------
    // Finder specifico
    // ----------------------

    @Override
    public Fattura findLastByUtente(int idUtente) {
        Optional<Fattura> r = queryOne(SQL_FIND_LAST_BY_UTENTE,
                ps -> ps.setInt(1, idUtente),
                DbmsFatturaDAO::map);
        return r.orElse(null);
    }
}
