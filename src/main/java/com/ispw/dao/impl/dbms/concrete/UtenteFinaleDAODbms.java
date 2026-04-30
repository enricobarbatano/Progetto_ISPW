package com.ispw.dao.impl.dbms.concrete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.ispw.dao.impl.base.BaseUtenteFinaleDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.model.entity.UtenteFinale;
import com.ispw.model.enums.Ruolo;
import com.ispw.model.enums.StatoAccount;

/**
 * Provider DBMS per UtenteFinale.
 */
public class UtenteFinaleDAODbms extends BaseUtenteFinaleDAO {

    private static final String TBL = "general_user";
    private static final String COLS =
        "id_utente, nome, cognome, email, password, stato_account, ruolo";

    private static final String SQL_SELECT_ONE =
        "SELECT " + COLS + " FROM " + TBL +
        " WHERE id_utente=? AND ruolo='UTENTE'";

    private static final String SQL_SELECT_BY_EMAIL =
        "SELECT " + COLS + " FROM " + TBL +
        " WHERE LOWER(email)=? AND ruolo='UTENTE'";

    private static final String SQL_SELECT_ALL =
        "SELECT " + COLS + " FROM " + TBL +
        " WHERE ruolo='UTENTE' ORDER BY id_utente";

    private static final String SQL_EXISTS =
        "SELECT 1 FROM " + TBL +
        " WHERE id_utente=? AND ruolo='UTENTE'";

    private static final String SQL_INSERT =
        "INSERT INTO " + TBL +
        " (nome, cognome, email, password, stato_account, ruolo) " +
        "VALUES (?, ?, ?, ?, ?, 'UTENTE')";

    private static final String SQL_UPDATE =
        "UPDATE " + TBL +
        " SET nome=?, cognome=?, email=?, password=?, stato_account=? " +
        "WHERE id_utente=? AND ruolo='UTENTE'";

    private static final String SQL_DELETE =
        "DELETE FROM " + TBL +
        " WHERE id_utente=? AND ruolo='UTENTE'";

    private final ConnectionFactory cf;

    public UtenteFinaleDAODbms(ConnectionFactory cf) {
        super(true);
        this.cf = cf;
    }

    // ================= RAW =================

    @Override
    protected UtenteFinale rawLoad(Integer id) {
        if (id == null || id <= 0) return null;

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_SELECT_ONE)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS UtenteFinale rawLoad", e);
        }
    }

    @Override
    protected UtenteFinale rawFindByEmail(String email) {
        final String norm = normalizeEmail(email);
        if (norm == null || norm.isBlank()) return null;

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_SELECT_BY_EMAIL)) {

            ps.setString(1, norm);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS UtenteFinale rawFindByEmail", e);
        }
    }

    @Override
    protected List<UtenteFinale> rawFindAll() {
        List<UtenteFinale> out = new ArrayList<>();

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) out.add(map(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS UtenteFinale rawFindAll", e);
        }
        return out;
    }

    @Override
    protected void rawStore(UtenteFinale u) {
        if (u == null) return;

        try (Connection c = cf.getConnection()) {

            if (u.getIdUtente() > 0 && existsDb(c, u.getIdUtente())) {
                try (PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
                    bindUpdate(ps, u);
                    ps.setInt(6, u.getIdUtente());
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps =
                        c.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

                    bindInsert(ps, u);
                    ps.executeUpdate();
                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) u.setIdUtente(gk.getInt(1));
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS UtenteFinale rawStore", e);
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
            throw new RuntimeException("Errore DBMS UtenteFinale rawDelete", e);
        }
    }

    // ================= HELPERS =================

    private static UtenteFinale map(ResultSet rs) throws SQLException {
        UtenteFinale u = new UtenteFinale();
        u.setIdUtente(rs.getInt("id_utente"));
        u.setNome(rs.getString("nome"));
        u.setCognome(rs.getString("cognome"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setStatoAccount(parseEnum(rs.getString("stato_account"), StatoAccount.DA_CONFERMARE));
        u.setRuolo(parseEnum(rs.getString("ruolo"), Ruolo.UTENTE));
        return u;
    }

    private static <E extends Enum<E>> E parseEnum(String v, E def) {
        if (v == null || v.isBlank()) return def;
        try { return Enum.valueOf(def.getDeclaringClass(), v); }
        catch (IllegalArgumentException e) { return def; }
    }

    private static void bindInsert(PreparedStatement ps, UtenteFinale u) throws SQLException {
        ps.setString(1, u.getNome());
        ps.setString(2, u.getCognome());
        ps.setString(3, u.getEmail());
        ps.setString(4, u.getPassword());
        ps.setString(5, u.getStatoAccount() != null ? u.getStatoAccount().name() : null);
    }

    private static void bindUpdate(PreparedStatement ps, UtenteFinale u) throws SQLException {
        ps.setString(1, u.getNome());
        ps.setString(2, u.getCognome());
        ps.setString(3, u.getEmail());
        ps.setString(4, u.getPassword());
        ps.setString(5, u.getStatoAccount() != null ? u.getStatoAccount().name() : null);
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