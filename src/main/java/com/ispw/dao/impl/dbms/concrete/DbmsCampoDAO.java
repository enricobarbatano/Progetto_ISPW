package com.ispw.dao.impl.dbms.concrete;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.util.List;

import com.ispw.dao.impl.dbms.base.DbmsDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.model.entity.Campo;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.enums.StatoPrenotazione;

/**
 * SEZIONE ARCHITETTURALE
 * Ruolo: DAO DBMS per Campo.
 * Responsabilita': gestire accesso al DB tramite SQL e mapping.
 *
 * SEZIONE LOGICA
 * Usa DbmsDAO per eseguire query e mappare i record in entita'.
 */
public class DbmsCampoDAO extends DbmsDAO<Integer, Campo> implements CampoDAO {

    
    private static final String SQL_FIND_ALL =
        "SELECT id_campo, nome, tipo_sport, costo_orario, is_attivo, flag_manutenzione " +
        "FROM campi ORDER BY id_campo";

    private static final String SQL_FIND_BY_ID =
        "SELECT id_campo, nome, tipo_sport, costo_orario, is_attivo, flag_manutenzione " +
        "FROM campi WHERE id_campo = ?";

    private static final String SQL_FIND_PRENOTAZIONI_BY_CAMPO =
        "SELECT id_prenotazione, id_utente, id_campo, data, ora_inizio, ora_fine, stato, notifica_richiesta " +
        "FROM prenotazioni WHERE id_campo = ?";

    private static final String SQL_EXISTS =
        "SELECT 1 FROM campi WHERE id_campo = ?";

    private static final String SQL_INSERT =
        "INSERT INTO campi (id_campo, nome, tipo_sport, costo_orario, is_attivo, flag_manutenzione) " +
        "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
        "UPDATE campi SET nome=?, tipo_sport=?, costo_orario=?, is_attivo=?, flag_manutenzione=? " +
        "WHERE id_campo=?";

    private static final String SQL_DELETE =
        "DELETE FROM campi WHERE id_campo=?";

    public DbmsCampoDAO(ConnectionFactory cf) {
        super(cf);
    }

   
    private final RowMapper<Campo> mapper = rs -> {
        Campo c = new Campo();
        c.setIdCampo(rs.getInt("id_campo"));
        c.setNome(rs.getString("nome"));
        c.setTipoSport(rs.getString("tipo_sport"));
        float costo = rs.getFloat("costo_orario");
        if (!rs.wasNull()) c.setCostoOrario(costo);
        c.setAttivo(rs.getBoolean("is_attivo"));
        c.setFlagManutenzione(rs.getBoolean("flag_manutenzione"));
        loadPrenotazioniForCampo(c);
        return c;
    };

    
    @Override
    public Campo load(Integer id) {
        if (id == null) return null;
        return queryOne(SQL_FIND_BY_ID, ps -> ps.setInt(1, id), mapper).orElse(null);
    }

    @Override
    public void store(Campo entity) {
        if (entity == null) return;
        if (exists(entity.getIdCampo())) {
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
        if (id == null) return false;
        return queryExists(SQL_EXISTS, ps -> ps.setInt(1, id));
    }

    @Override
    public Campo create(Integer id) {
        Campo c = new Campo();
        if (id != null) c.setIdCampo(id);
        return c; // crea solo in memoria (persisti poi con store)
    }

   
    @Override
    public List<Campo> findAll() {
        return queryList(SQL_FIND_ALL, null, mapper);
    }

    @Override
    public Campo findById(int idCampo) {
        return load(idCampo);
    }


    private void insert(Campo c) {
        executeUpdate(SQL_INSERT, ps -> {
            ps.setInt(1, c.getIdCampo());
            ps.setString(2, c.getNome());
            ps.setString(3, c.getTipoSport());
            if (c.getCostoOrario() != null) ps.setFloat(4, c.getCostoOrario());
            else ps.setNull(4, Types.FLOAT);
            ps.setBoolean(5, c.isAttivo());
            ps.setBoolean(6, c.isFlagManutenzione());
        });
    }

    private void update(Campo c) {
        executeUpdate(SQL_UPDATE, ps -> {
            ps.setString(1, c.getNome());
            ps.setString(2, c.getTipoSport());
            if (c.getCostoOrario() != null) ps.setFloat(3, c.getCostoOrario());
            else ps.setNull(3, Types.FLOAT);
            ps.setBoolean(4, c.isAttivo());
            ps.setBoolean(5, c.isFlagManutenzione());
            ps.setInt(6, c.getIdCampo());
        });
    }

    private void loadPrenotazioniForCampo(Campo c) {
        if (c == null) return;

        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_PRENOTAZIONI_BY_CAMPO)) {

            ps.setInt(1, c.getIdCampo());
            loadPrenotazioniFromStatement(ps, c);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    private void loadPrenotazioniFromStatement(PreparedStatement ps, Campo c) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Prenotazione p = mapPrenotazione(rs);
                if (p != null) {
                    c.aggiungiPrenotazione(p);
                }
            }
        }
    }

    private Prenotazione mapPrenotazione(ResultSet rs) throws SQLException {
        Prenotazione p = new Prenotazione();
        p.setIdPrenotazione(rs.getInt("id_prenotazione"));
        p.setIdUtente(rs.getInt("id_utente"));
        p.setIdCampo(rs.getInt("id_campo"));

        fillDateTime(p, rs);

        StatoPrenotazione statoEnum = parseStatoPrenotazione(rs.getString("stato"));
        if (statoEnum == StatoPrenotazione.ANNULLATA) {
            return null;
        }
        if (statoEnum != null) {
            p.setStato(statoEnum);
        }

        p.setNotificaRichiesta(rs.getBoolean("notifica_richiesta"));
        return p;
    }

    private void fillDateTime(Prenotazione p, ResultSet rs) throws SQLException {
        Date d = rs.getDate("data");
        Time tStart = rs.getTime("ora_inizio");
        Time tEnd = rs.getTime("ora_fine");
        if (d != null) p.setData(d.toLocalDate());
        if (tStart != null) p.setOraInizio(tStart.toLocalTime());
        if (tEnd != null) p.setOraFine(tEnd.toLocalTime());
    }

    private StatoPrenotazione parseStatoPrenotazione(String stato) {
        if (stato == null) return null;
        try {
            return StatoPrenotazione.valueOf(stato);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}