package com.ispw.dao.impl.dbms.concrete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.ispw.dao.impl.base.BaseGeneralUserDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.UtenteFinale;
import com.ispw.model.enums.Ruolo;
import com.ispw.model.enums.StatoAccount;

/**
 * Provider DBMS per GeneralUser.
 * Implementa SOLO raw I/O JDBC (cache e template nella BaseGeneralUserDAO).
 */
public class GeneralUserDAODbms extends BaseGeneralUserDAO {

    private static final String TBL = "general_user";
    private static final String COLS =
        "id_utente, nome, cognome, email, password, stato_account, ruolo";

    private static final String SQL_SELECT_ONE =
        "SELECT " + COLS + " FROM " + TBL + " WHERE id_utente=?";

    private static final String SQL_SELECT_ALL =
        "SELECT " + COLS + " FROM " + TBL;

    private static final String SQL_SELECT_BY_EMAIL =
        "SELECT " + COLS + " FROM " + TBL + " WHERE LOWER(email)=?";

    private static final String SQL_EXISTS =
        "SELECT 1 FROM " + TBL + " WHERE id_utente=?";

    private static final String SQL_INSERT =
        "INSERT INTO " + TBL +
        " (nome, cognome, email, password, stato_account, ruolo) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
        "UPDATE " + TBL +
        " SET nome=?, cognome=?, email=?, password=?, stato_account=?, ruolo=? WHERE id_utente=?";

    private static final String SQL_DELETE =
        "DELETE FROM " + TBL + " WHERE id_utente=?";

    private final ConnectionFactory cf;

    public GeneralUserDAODbms(ConnectionFactory cf) {
        super(true); // persistent
        this.cf = cf;
    }

    // ========================
    // RAW METHODS
    // ========================

    @Override
    protected GeneralUser rawLoad(Integer id) {
        if (id == null || id <= 0) return null;

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_SELECT_ONE)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS GeneralUser rawLoad", e);
        }
    }

    @Override
    protected List<GeneralUser> rawFindAll() {
        List<GeneralUser> out = new ArrayList<>();

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(map(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS GeneralUser rawFindAll", e);
        }
        return out;
    }

    @Override
    protected GeneralUser rawFindByEmail(String email) {
        final String norm = normalizeEmail(email);
        if (norm == null || norm.isBlank()) return null;

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_SELECT_BY_EMAIL)) {

            ps.setString(1, norm);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS GeneralUser rawFindByEmail", e);
        }
    }

    @Override
    protected void rawStore(GeneralUser u) {
        if (u == null) return;

        try (Connection c = cf.getConnection()) {

            if (u.getIdUtente() > 0 && existsDb(c, u.getIdUtente())) {
                // UPDATE
                try (PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
                    bind(ps, u);
                    ps.setInt(7, u.getIdUtente());
                    ps.executeUpdate();
                }
            } else {
                // INSERT con generated key
                try (PreparedStatement ps = c.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
                    bind(ps, u);
                    ps.executeUpdate();
                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) {
                            u.setIdUtente(gk.getInt(1));
                        }
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS GeneralUser rawStore", e);
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
            throw new RuntimeException("Errore DBMS GeneralUser rawDelete", e);
        }
    }

    // ========================
    // HELPERS
    // ========================

    private static GeneralUser map(ResultSet rs) throws SQLException {
        UtenteFinale u = new UtenteFinale(); // GeneralUser è abstract
        u.setIdUtente(rs.getInt("id_utente"));
        u.setNome(rs.getString("nome"));
        u.setCognome(rs.getString("cognome"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setStatoAccount(parseEnum(rs.getString("stato_account"), StatoAccount.DA_CONFERMARE));
        u.setRuolo(parseEnum(rs.getString("ruolo"), Ruolo.UTENTE));
        return u;
    }

    private static <E extends Enum<E>> E parseEnum(String value, E defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Enum.valueOf(defaultValue.getDeclaringClass(), value);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    private static void bind(PreparedStatement ps, GeneralUser u) throws SQLException {
        ps.setString(1, u.getNome());
        ps.setString(2, u.getCognome());
        ps.setString(3, u.getEmail());
        ps.setString(4, u.getPassword());
        ps.setString(5, u.getStatoAccount() != null ? u.getStatoAccount().name() : null);
        ps.setString(6, u.getRuolo() != null ? u.getRuolo().name() : null);
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean existsDb(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SQL_EXISTS)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}