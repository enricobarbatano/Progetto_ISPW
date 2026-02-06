package com.ispw.dao.impl.dbms.concrete;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import com.ispw.dao.impl.dbms.base.DbmsDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.dao.interfaces.PenalitaDAO;
import com.ispw.model.entity.Penalita;
import com.ispw.model.enums.StatoPenalita;

/**
 * SEZIONE ARCHITETTURALE
 * Ruolo: DAO DBMS per Penalita.
 * Responsabilita': gestire accesso al DB tramite SQL e mapping.
 *
 * SEZIONE LOGICA
 * Usa DbmsDAO per eseguire query e mappare i record in entita'.
 */
public class DbmsPenalitaDAO extends DbmsDAO<Integer, Penalita> implements PenalitaDAO {

    public DbmsPenalitaDAO(ConnectionFactory cf) { super(cf); }

    private static final String SQL_FIND_BY_ID =
        "SELECT id_penalita, id_utente, data_emissione, importo, motivazione, stato " +
        "FROM penalita WHERE id_penalita=?";

    private static final String SQL_EXISTS =
        "SELECT 1 FROM penalita WHERE id_penalita=?";

    private static final String SQL_INSERT =
        "INSERT INTO penalita (id_utente, data_emissione, importo, motivazione, stato) " +
        "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
        "UPDATE penalita SET id_utente=?, data_emissione=?, importo=?, motivazione=?, stato=? " +
        "WHERE id_penalita=?";

    private static final String SQL_DELETE =
        "DELETE FROM penalita WHERE id_penalita=?";

    private static final String SQL_FIND_BY_UTENTE =
        "SELECT id_penalita, id_utente, data_emissione, importo, motivazione, stato " +
        "FROM penalita WHERE id_utente=? ORDER BY data_emissione DESC, id_penalita DESC";

    private final RowMapper<Penalita> mapper = rs -> {
        Penalita p = new Penalita();
        p.setIdPenalita(rs.getInt("id_penalita"));
        p.setIdUtente(rs.getInt("id_utente"));

        Date d = rs.getDate("data_emissione");
        if (d != null) p.setDataEmissione(d.toLocalDate());

        BigDecimal imp = rs.getBigDecimal("importo");
        p.setImporto(imp);

        p.setMotivazione(rs.getString("motivazione"));

        String stato = rs.getString("stato");
        if (stato != null) {
            try { p.setStato(StatoPenalita.valueOf(stato)); }
            catch (IllegalArgumentException ignore) { /* ignored: invalid stato */ }
        }

        return p;
    };

    @Override
    public Penalita load(Integer id) {
        if (id == null) return null;
        return queryOne(SQL_FIND_BY_ID, ps -> ps.setInt(1, id), mapper).orElse(null);
    }

    @Override
    public void store(Penalita entity) {
        if (entity == null) return;
        if (exists(entity.getIdPenalita())) {
            update(entity);
        } else {
            insert(entity);
        }
    }

    @Override
    public void delete(Integer id) {
        if (id == null) return;
        executeUpdate(SQL_DELETE, ps -> ps.setInt(1, id));
    }

    @Override
    public boolean exists(Integer id) {
        if (id == null || id <= 0) return false;
        return queryExists(SQL_EXISTS, ps -> ps.setInt(1, id));
    }

    @Override
    public Penalita create(Integer id) {
        Penalita p = new Penalita();
        if (id != null) p.setIdPenalita(id);
        return p;
    }

    @Override
    public List<Penalita> recuperaPenalitaUtente(int idUtente) {
        return queryList(SQL_FIND_BY_UTENTE, ps -> ps.setInt(1, idUtente), mapper);
    }

    private void insert(Penalita p) {
        try (Connection c = openConnection();
             PreparedStatement ps = c.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            bindPenalita(ps, p, 1);

            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    p.setIdPenalita(gk.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    private void update(Penalita p) {
        executeUpdate(SQL_UPDATE, ps -> {
            bindPenalita(ps, p, 1);
            ps.setInt(6, p.getIdPenalita());
        });
    }

    private void bindPenalita(PreparedStatement ps, Penalita p, int startIndex) throws SQLException {
        ps.setInt(startIndex, p.getIdUtente());
        if (p.getDataEmissione() != null) ps.setDate(startIndex + 1, Date.valueOf(p.getDataEmissione()));
        else ps.setNull(startIndex + 1, Types.DATE);

        if (p.getImporto() != null) ps.setBigDecimal(startIndex + 2, p.getImporto());
        else ps.setNull(startIndex + 2, Types.DECIMAL);

        ps.setString(startIndex + 3, p.getMotivazione());
        ps.setString(startIndex + 4, p.getStato() != null ? p.getStato().name() : null);
    }
}