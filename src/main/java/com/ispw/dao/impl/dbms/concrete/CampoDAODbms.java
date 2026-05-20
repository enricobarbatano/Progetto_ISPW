package com.ispw.dao.impl.dbms.concrete;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.ispw.dao.exception.DaoException;
import com.ispw.dao.impl.base.BaseCampoDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.model.entity.Campo;

/**
 * Provider DBMS per Campo.
 * Implementa solo raw I/O JDBC.
 * Cache e logica cache-first restano in BaseCampoDAO.
 */
public class CampoDAODbms extends BaseCampoDAO {

    private static final String SQL_FIND_ALL =
        "SELECT id_campo, nome, tipo_sport, costo_orario, is_attivo, flag_manutenzione " +
        "FROM campi ORDER BY id_campo";

    private static final String SQL_FIND_BY_ID =
        "SELECT id_campo, nome, tipo_sport, costo_orario, is_attivo, flag_manutenzione " +
        "FROM campi WHERE id_campo=?";

    private static final String SQL_INSERT =
        "INSERT INTO campi (nome, tipo_sport, costo_orario, is_attivo, flag_manutenzione) " +
        "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
        "UPDATE campi SET nome=?, tipo_sport=?, costo_orario=?, is_attivo=?, flag_manutenzione=? " +
        "WHERE id_campo=?";

    private static final String SQL_DELETE =
        "DELETE FROM campi WHERE id_campo=?";

    private final ConnectionFactory cf;

    public CampoDAODbms(ConnectionFactory cf) {
        super(true);
        this.cf = cf;
    }

    @Override
    protected Campo rawLoad(Integer id) {
        if (id == null || id <= 0) {
            return null;
        }

        // Apre la connessione e prepara la query di lookup per id.
        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_ID)) {

            // Inserisce l'id nel parametro della query.
            ps.setInt(1, id);

            // Esegue la query e converte il record in entità Campo.
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapCampo(rs);
            }

        } catch (SQLException e) {
            throw new DaoException("Errore DBMS Campo rawLoad", e);
        }
    }

    @Override
    protected List<Campo> rawFindAll() {
        List<Campo> out = new ArrayList<>();

        // Legge tutti i campi dal DB.
        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            // Mappa ogni riga in un Campo.
            while (rs.next()) {
                out.add(mapCampo(rs));
            }

        } catch (SQLException e) {
            throw new DaoException("Errore DBMS Campo rawFindAll", e);
        }

        return out;
    }

    @Override
    protected void rawStore(Campo c) {
        if (c == null) {
            return;
        }

        try (Connection conn = cf.getConnection()) {

            if (c.getIdCampo() > 0) {
                // UPDATE se il campo ha già un id.
                try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
                    bind(ps, c);
                    ps.setInt(6, c.getIdCampo());
                    ps.executeUpdate();
                }
            } else {
                // INSERT se il campo è nuovo, con recupero generated key.
                try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
                    bind(ps, c);
                    ps.executeUpdate();

                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) {
                            c.setIdCampo(gk.getInt(1));
                        }
                    }
                }
            }

        } catch (SQLException e) {
            throw new DaoException("Errore DBMS Campo rawStore", e);
        }
    }

    @Override
    protected void rawDelete(Integer id) {
        if (id == null || id <= 0) {
            return;
        }

        // Cancella il campo con id specificato.
        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_DELETE)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DaoException("Errore DBMS Campo rawDelete", e);
        }
    }

    /**
     * Mapping record relazionale -> oggetto Campo.
     */
    private Campo mapCampo(ResultSet rs) throws SQLException {
        Campo c = new Campo();

        c.setIdCampo(rs.getInt("id_campo"));
        c.setNome(rs.getString("nome"));
        c.setTipoSport(rs.getString("tipo_sport"));

        float costo = rs.getFloat("costo_orario");
        if (!rs.wasNull()) {
            c.setCostoOrario(costo);
        }

        c.setAttivo(rs.getBoolean("is_attivo"));
        c.setFlagManutenzione(rs.getBoolean("flag_manutenzione"));

        return c;
    }

    /**
     * Mapping oggetto Campo -> parametri PreparedStatement.
     */
    private void bind(PreparedStatement ps, Campo c) throws SQLException {
        ps.setString(1, c.getNome());
        ps.setString(2, c.getTipoSport());

        if (c.getCostoOrario() != null) {
            ps.setFloat(3, c.getCostoOrario());
        } else {
            ps.setNull(3, Types.FLOAT);
        }

        ps.setBoolean(4, c.isAttivo());
        ps.setBoolean(5, c.isFlagManutenzione());
    }
}

