package com.ispw.dao.impl.dbms.concrete;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.ispw.dao.impl.base.BaseRegolePenalitaDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.model.entity.RegolePenalita;

/**
 * Provider DBMS per RegolePenalita.
 * Gestisce UNA SOLA configurazione (id fisso = 1).
 */
public class RegolePenalitaDAODbms extends BaseRegolePenalitaDAO {

    private static final String SQL_SELECT_ONE =
        "SELECT valore_penalita, preavviso_minimo FROM regole_penalita WHERE id = 1";

    private static final String SQL_DELETE_ONE =
        "DELETE FROM regole_penalita WHERE id = 1";

    private static final String SQL_INSERT_ONE =
        "INSERT INTO regole_penalita (id, valore_penalita, preavviso_minimo) VALUES (1, ?, ?)";

    private final ConnectionFactory cf;

    public RegolePenalitaDAODbms(ConnectionFactory cf) {
        super(true); // persistent
        this.cf = cf;
    }

    @Override
    protected RegolePenalita rawLoad() {
        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_SELECT_ONE);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) return null;

            RegolePenalita rp = new RegolePenalita();
            rp.setIdConfig(1);
            rp.setValorePenalita(rs.getBigDecimal("valore_penalita"));
            rp.setPreavvisoMinimo(rs.getInt("preavviso_minimo"));
            return rp;

        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS RegolePenalita rawLoad", e);
        }
    }

    @Override
    protected void rawSave(RegolePenalita regole) {
        try (Connection c = cf.getConnection()) {
            c.setAutoCommit(false);

            try (PreparedStatement del = c.prepareStatement(SQL_DELETE_ONE)) {
                del.executeUpdate();
            }

            try (PreparedStatement ins = c.prepareStatement(SQL_INSERT_ONE)) {
                BigDecimal v = regole.getValorePenalita();
                if (v != null) ins.setBigDecimal(1, v);
                else           ins.setNull(1, Types.DECIMAL);

                ins.setInt(2, regole.getPreavvisoMinimo());
                ins.executeUpdate();
            }

            c.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Errore DBMS RegolePenalita rawSave", e);
        }
    }
}