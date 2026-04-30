package com.ispw.dao.impl.dbms.concrete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.ispw.dao.impl.base.BaseGestoreDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.model.entity.Gestore;
import com.ispw.model.enums.Ruolo;
import com.ispw.model.enums.StatoAccount;

/**
 * Provider DBMS per Gestore.
 * NOTA: i permessi NON sono persistiti su DB.
 */
public class GestoreDAODbms extends BaseGestoreDAO {

    private static final String TBL = "general_user";
    private static final String COLS =
        "id_utente, nome, cognome, email, password, stato_account, ruolo";

    private static final String SQL_SELECT_ONE =
        "SELECT " + COLS + " FROM " + TBL +
        " WHERE id_utente=? AND ruolo='GESTORE'";

    private static final String SQL_SELECT_BY_EMAIL =
        "SELECT " + COLS + " FROM " + TBL +
        " WHERE LOWER(email)=? AND ruolo='GESTORE'";

    private static final String SQL_SELECT_ALL =
        "SELECT " + COLS + " FROM " + TBL +
        " WHERE ruolo='GESTORE' ORDER BY id_utente";

    private static final String SQL_EXISTS =
        "SELECT 1 FROM " + TBL +
        " WHERE id_utente=? AND ruolo='GESTORE'";

    private static final String SQL_INSERT =
        "INSERT INTO " + TBL +
        " (nome, cognome, email, password, stato_account, ruolo) " +
        "VALUES (?, ?, ?, ?, ?, 'GESTORE')";

    private static final String SQL_UPDATE =
        "UPDATE " + TBL +
        " SET nome=?, cognome=?, email=?, password=?, stato_account=? " +
        "WHERE id_utente=? AND ruolo='GESTORE'";

    private static final String SQL_DELETE =
        "DELETE FROM " + TBL +
        " WHERE id_utente=? AND ruolo='GESTORE'";

    private final ConnectionFactory cf;

    public GestoreDAODbms(ConnectionFactory cf) {
        super(true);
        this.cf = cf;
    }

    // ================= RAW =================

    @Override
    protected Gestore rawLoad(Integer id) {
        if (id == null || id <= 0) return null;

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
        final String norm = normalizeEmail(email);
        if (norm == null || norm.isBlank()) return null;

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
    protected List<Gestore> rawFindAll() {
        List<Gestore> out = new ArrayList<>();

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) out.add(map(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS Gestore rawFindAll", e);
        }
        return out;
    }

    @Override
    protected void rawStore(Gestore g) {
        if (g == null) return;

        try (Connection c = cf.getConnection()) {

            if (g.getIdUtente() > 0 && existsDb(c, g.getIdUtente())) {
                try (PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
                    bindUpdate(ps, g);
                    ps.setInt(6, g.getIdUtente());
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps =
                        c.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

                    bindInsert(ps, g);
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
        if (id == null || id <= 0) return;

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_DELETE)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS Gestore rawDelete", e);
        }
    }

    // ================= HELPERS =================
    //serve per mappare un ResultSet su un oggetto Gestore,
    //e per bindare i parametri di una PreparedStatement da un oggetto Gestore.

    private static Gestore map(ResultSet rs) throws SQLException {
        Gestore g = new Gestore();
        g.setIdUtente(rs.getInt("id_utente"));
        g.setNome(rs.getString("nome"));
        g.setCognome(rs.getString("cognome"));
        g.setEmail(rs.getString("email"));
        g.setPassword(rs.getString("password"));
        g.setStatoAccount(parseEnum(rs.getString("stato_account"), StatoAccount.DA_CONFERMARE));
        g.setRuolo(parseEnum(rs.getString("ruolo"), Ruolo.GESTORE));
        return g;
    }

    private static <E extends Enum<E>> E parseEnum(String v, E def) {
        if (v == null || v.isBlank()) return def;
        try { return Enum.valueOf(def.getDeclaringClass(), v); }
        catch (IllegalArgumentException e) { return def; }
    }

    private static void bindInsert(PreparedStatement ps, Gestore g) throws SQLException {
        ps.setString(1, g.getNome());
        ps.setString(2, g.getCognome());
        ps.setString(3, g.getEmail());
        ps.setString(4, g.getPassword());
        ps.setString(5, g.getStatoAccount() != null ? g.getStatoAccount().name() : null);
    }

    private static void bindUpdate(PreparedStatement ps, Gestore g) throws SQLException {
        ps.setString(1, g.getNome());
        ps.setString(2, g.getCognome());
        ps.setString(3, g.getEmail());
        ps.setString(4, g.getPassword());
        ps.setString(5, g.getStatoAccount() != null ? g.getStatoAccount().name() : null);
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean existsDb(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SQL_EXISTS)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }
}