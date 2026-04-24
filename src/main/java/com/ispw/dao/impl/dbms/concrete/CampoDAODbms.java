
package com.ispw.dao.impl.dbms.concrete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.ispw.dao.impl.base.BaseCampoDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.model.entity.Campo;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.enums.StatoPrenotazione;

public class CampoDAODbms extends BaseCampoDAO {

    private static final String SQL_FIND_ALL =
        "SELECT id_campo, nome, tipo_sport, costo_orario, is_attivo, flag_manutenzione FROM campi ORDER BY id_campo";

    private static final String SQL_FIND_BY_ID =
        "SELECT id_campo, nome, tipo_sport, costo_orario, is_attivo, flag_manutenzione FROM campi WHERE id_campo=?";

    private static final String SQL_FIND_PRENOTAZIONI =
        "SELECT id_prenotazione, id_utente, data, ora_inizio, ora_fine, stato, notifica_richiesta " +
        "FROM prenotazioni WHERE id_campo=?";

    private static final String SQL_DELETE =
        "DELETE FROM campi WHERE id_campo=?";

    private static final String SQL_INSERT =
        "INSERT INTO campi (id_campo, nome, tipo_sport, costo_orario, is_attivo, flag_manutenzione) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
        "UPDATE campi SET nome=?, tipo_sport=?, costo_orario=?, is_attivo=?, flag_manutenzione=? WHERE id_campo=?";

    private final ConnectionFactory cf;

    public CampoDAODbms(ConnectionFactory cf) {
        super(true);
        this.cf = cf;
    }

    @Override
    protected Campo rawLoad(Integer id) {
        if (id == null) return null;

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Campo campo = mapCampo(rs);
                loadPrenotazioni(campo, c);
                return campo;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS Campo rawLoad", e);
        }
    }

    @Override
    protected List<Campo> rawFindAll() {
        List<Campo> out = new ArrayList<>();

        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Campo campo = mapCampo(rs);
                loadPrenotazioni(campo, c);
                out.add(campo);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS Campo rawFindAll", e);
        }
        return out;
    }

    @Override
    protected void rawStore(Campo c) {
        try (Connection conn = cf.getConnection()) {

            boolean exists;
            try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM campi WHERE id_campo=?")) {
                ps.setInt(1, c.getIdCampo());
                exists = ps.executeQuery().next();
            }

            if (exists) {
                try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
                    bind(ps, c);
                    ps.setInt(6, c.getIdCampo());
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
                    ps.setInt(1, c.getIdCampo());
                    bind(ps, c);
                    ps.executeUpdate();
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS Campo rawStore", e);
        }
    }

    @Override
    protected void rawDelete(Integer id) {
        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_DELETE)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS Campo rawDelete", e);
        }
    }

    /* ==================== helpers ==================== */

    private Campo mapCampo(ResultSet rs) throws SQLException {
        Campo c = new Campo();
        c.setIdCampo(rs.getInt("id_campo"));
        c.setNome(rs.getString("nome"));
        c.setTipoSport(rs.getString("tipo_sport"));
        float costo = rs.getFloat("costo_orario");
        if (!rs.wasNull()) c.setCostoOrario(costo);
        c.setAttivo(rs.getBoolean("is_attivo"));
        c.setFlagManutenzione(rs.getBoolean("flag_manutenzione"));
        return c;
    }

    private void loadPrenotazioni(Campo c, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_PRENOTAZIONI)) {
            ps.setInt(1, c.getIdCampo());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Prenotazione p = mapPrenotazione(rs);
                    if (p != null) c.aggiungiPrenotazione(p);
                }
            }
        }
    }

    private Prenotazione mapPrenotazione(ResultSet rs) throws SQLException {
        StatoPrenotazione stato = StatoPrenotazione.valueOf(rs.getString("stato"));
        if (stato == StatoPrenotazione.ANNULLATA) return null;

        Prenotazione p = new Prenotazione();
        p.setIdPrenotazione(rs.getInt("id_prenotazione"));
        p.setIdUtente(rs.getInt("id_utente"));
        p.setData(rs.getDate("data").toLocalDate());
        p.setOraInizio(rs.getTime("ora_inizio").toLocalTime());
        Time tEnd = rs.getTime("ora_fine");
        if (tEnd != null) p.setOraFine(tEnd.toLocalTime());
        p.setStato(stato);
        p.setNotificaRichiesta(rs.getBoolean("notifica_richiesta"));
        return p;
    }

    private void bind(PreparedStatement ps, Campo c) throws SQLException {
        ps.setString(1, c.getNome());
        ps.setString(2, c.getTipoSport());
        if (c.getCostoOrario() != null) ps.setFloat(3, c.getCostoOrario());
        else ps.setNull(3, Types.FLOAT);
        ps.setBoolean(4, c.isAttivo());
        ps.setBoolean(5, c.isFlagManutenzione());
    }
}
