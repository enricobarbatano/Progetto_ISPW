package com.ispw.dao.impl.dbms.concrete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import com.ispw.dao.impl.base.BaseGestoreDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.model.entity.Gestore;
import com.ispw.model.enums.Ruolo;
import com.ispw.model.enums.StatoAccount;

/**
 * Provider DBMS per Gestore.
 * NOTA: i permessi NON sono persistiti su DB (come nello stato attuale del progetto).
 */
public class GestoreDAODbms extends BaseGestoreDAO {

    private static final String TBL = "general_user";
    private static final String COLS =
        "id_utente, nome, cognome, email, password, stato_account, ruolo";

    private static final String SQL_SELECT_ONE =
        "SELECT " + COLS + " FROM " + TBL + " WHERE id_utente=?";

    private static final String SQL_SELECT_BY_EMAIL =
        "SELECT " + COLS + " FROM " + TBL + " WHERE LOWER(email)=?";

    private static final String SQL_INSERT =
        "INSERT INTO " + TBL +
        " (nome, cognome, email, password, stato_account, ruolo) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
        "UPDATE " + TBL +
        " SET nome=?, cognome=?, email=?, password=?, stato_account=?, ruolo=? WHERE id_utente=?";

    private static final String SQL_DELETE =
        "DELETE FROM " + TBL + " WHERE id_utente=?";

    private final ConnectionFactory cf;

    public GestoreDAODbms(ConnectionFactory cf) {
        super(true);
        this.cf = cf;
    }

    @Override
    protected Gestore rawLoad(Integer id) {
        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_SELECT_ONE)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS Gestore rawLoad", e);
        }
    }

    @Override
    protected Gestore rawFindByEmail(String email) {
        final String norm = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
        if (norm == null) return null;

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_SELECT_BY_EMAIL)) {
            ps.setString(1, norm);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS Gestore rawFindByEmail", e);
        }
    }

    @Override
    protected void rawStore(Gestore g) {
        try (Connection c = cf.getConnection()) {
            if (g.getIdUtente() > 0) {
                try (PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
                    bind(ps, g);
                    ps.setInt(7, g.getIdUtente());
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
                    bind(ps, g);
                    ps.executeUpdate();
                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) g.setIdUtente(gk.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS Gestore rawStore", e);
        }
    }

    @Override
    protected void rawDelete(Integer id) {
        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_DELETE)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS Gestore rawDelete", e);
        }
    }

    private static Gestore map(ResultSet rs) throws SQLException {
        Gestore g = new Gestore();
        g.setIdUtente(rs.getInt("id_utente"));
        g.setNome(rs.getString("nome"));
        g.setCognome(rs.getString("cognome"));
        g.setEmail(rs.getString("email"));
        g.setPassword(rs.getString("password"));
        g.setStatoAccount(StatoAccount.valueOf(rs.getString("stato_account")));
        g.setRuolo(Ruolo.valueOf(rs.getString("ruolo")));
        return g;
    }

    private static void bind(PreparedStatement ps, Gestore g) throws SQLException {
        ps.setString(1, g.getNome());
        ps.setString(2, g.getCognome());
        ps.setString(3, g.getEmail());
        ps.setString(4, g.getPassword());
        ps.setString(5, g.getStatoAccount() != null ? g.getStatoAccount().name() : null);
        ps.setString(6, g.getRuolo() != null ? g.getRuolo().name() : null);
    }
}
