
package com.ispw.dao.impl.dbms.concrete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;     // abstract
import java.sql.Statement;   // concreto da istanziare (sostituisci se diverso)
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import com.ispw.dao.impl.dbms.base.DbmsDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.UtenteFinale;
import com.ispw.model.enums.Ruolo;
import com.ispw.model.enums.StatoAccount;

/**
 * DAO DBMS per GeneralUser.
 * SonarCloud-friendly:
 * - costanti per SQL, try-with-resources, no System.out;
 * - helper per bind campi comuni (evita duplicazioni);
 * - validazioni input (early return).
 */
public final class DbmsGeneralUserDAO extends DbmsDAO<Integer, GeneralUser> implements GeneralUserDAO {
    private static final String ID_UTENTE= "id_utente";
    private static final String WHERE= " WHERE ";
    private static final String SELECT = "SELECT ";
    private static final String DA = " FROM ";
    // ======= Costanti SQL (adatta al tuo schema reale) =======
    private static final String TBL  = "general_user";
    private static final String COLS = "id_utente, nome, email, password, stato_account, ruolo";

    private static final String SQL_SELECT_ONE =
            SELECT + COLS + DA + TBL + WHERE + ID_UTENTE + " = ?";

    private static final String SQL_SELECT_BY_EMAIL =
            SELECT + COLS + DA + TBL + WHERE + "LOWER(email) = ?";

        private static final String SQL_SELECT_ALL =
            SELECT + COLS + DA + TBL;

    private static final String SQL_EXISTS =
            "SELECT 1 FROM " + TBL + WHERE + ID_UTENTE + " = ?";

    private static final String SQL_INSERT =
            "INSERT INTO " + TBL + " (nome, email, password, stato_account, ruolo) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE " + TBL + " SET nome = ?, email = ?, password = ?, stato_account = ?, ruolo = ? " + WHERE + ID_UTENTE + " = ?";

    private static final String SQL_DELETE =
            "DELETE FROM " + TBL + WHERE + ID_UTENTE + " = ?";
    public DbmsGeneralUserDAO(ConnectionFactory cf) {
        super(cf);
    }

    // ======= RowMapper =======
    private static GeneralUser map(ResultSet rs) throws SQLException {
        final UtenteFinale u = new UtenteFinale(); // GeneralUser è abstract
        u.setIdUtente(rs.getInt(ID_UTENTE));
        u.setNome(rs.getString("nome"));
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
        } catch (IllegalArgumentException ex) {
            return defaultValue;
        }
    }

    // ======= Helper per bind dei campi comuni =======
    private static void bindCommonFields(PreparedStatement ps, GeneralUser e, int startIndex) throws SQLException {
        ps.setString(startIndex,     e.getNome());
        ps.setString(startIndex + 1, e.getEmail());
        ps.setString(startIndex + 2, e.getPassword());
        ps.setString(startIndex + 3, e.getStatoAccount() != null ? e.getStatoAccount().name() : null);
        ps.setString(startIndex + 4, e.getRuolo() != null ? e.getRuolo().name() : null);
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    // ======= Implementazione DAO =======

    @Override
    public GeneralUser load(Integer id) {
        if (id == null || id <= 0) return null;
        Optional<GeneralUser> r = queryOne(SQL_SELECT_ONE, ps -> ps.setInt(1, id), DbmsGeneralUserDAO::map);
        return r.orElse(null);
    }

    @Override
    public void store(GeneralUser entity) {
        Objects.requireNonNull(entity, "entity non può essere null");
        final int id = entity.getIdUtente();

        if (id > 0 && queryExists(SQL_EXISTS, ps -> ps.setInt(1, id))) {
            // UPDATE
            executeUpdate(SQL_UPDATE, ps -> {
                bindCommonFields(ps, entity, 1);
                ps.setInt(6, id);
            });
            return;
        }

        // INSERT con generated key
        try (Connection c = openConnection();
             PreparedStatement ps = c.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            bindCommonFields(ps, entity, 1);
            ps.executeUpdate();

            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    entity.setIdUtente(gk.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    @Override
    public void delete(Integer id) {
        if (id == null || id <= 0) return; // early return
        executeUpdate(SQL_DELETE, ps -> ps.setInt(1, id));
    }

    @Override
    public boolean exists(Integer id) {
        if (id == null || id <= 0) return false;
        return queryExists(SQL_EXISTS, ps -> ps.setInt(1, id));
    }

    @Override
    public GeneralUser create(Integer id) {
        final UtenteFinale u = new UtenteFinale();
        u.setIdUtente(id != null ? id : 0);
        return u;
    }

    @Override
    public GeneralUser findByEmail(String email) {
        final String norm = normalizeEmail(email);
        if (norm == null || norm.isBlank()) return null;
        Optional<GeneralUser> r = queryOne(SQL_SELECT_BY_EMAIL, ps -> ps.setString(1, norm), DbmsGeneralUserDAO::map);
        return r.orElse(null);
    }

    @Override
    public GeneralUser findById(int idUtente) {
        return load(idUtente);
    }

    @Override
    public List<GeneralUser> findAll() {
        return queryList(SQL_SELECT_ALL, null, DbmsGeneralUserDAO::map);
    }
}
