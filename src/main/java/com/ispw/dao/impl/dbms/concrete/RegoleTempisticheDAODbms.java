package com.ispw.dao.impl.dbms.concrete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;

import com.ispw.dao.exception.DaoException;
import com.ispw.dao.impl.base.BaseRegoleTempisticheDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.model.entity.RegoleTempistiche;

/**
 * Provider DBMS per RegoleTempistiche.
 *
 * Responsabilità:
 * - implementare solo il raw I/O JDBC;
 * - leggere e salvare l'unica configurazione temporale del sistema;
 * - lasciare cache-first e logica applicativa alla BaseRegoleTempisticheDAO.
 *
 * Nota:
 * esiste una sola riga logica, identificata con id = 1.
 */
public class RegoleTempisticheDAODbms extends BaseRegoleTempisticheDAO {

    private static final String SQL_SELECT_ONE =
        "SELECT durata_slot, ora_apertura, ora_chiusura, preavviso_minimo " +
        "FROM regole_tempistiche WHERE id = 1";

    private static final String SQL_DELETE_ONE =
        "DELETE FROM regole_tempistiche WHERE id = 1";

    private static final String SQL_INSERT_ONE =
        "INSERT INTO regole_tempistiche (id, durata_slot, ora_apertura, ora_chiusura, preavviso_minimo) " +
        "VALUES (1, ?, ?, ?, ?)";

    private final ConnectionFactory cf;

    public RegoleTempisticheDAODbms(ConnectionFactory cf) {
        super(true);
        this.cf = cf;
    }

    @Override
    protected RegoleTempistiche rawLoad() {
        try (Connection c = cf.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_SELECT_ONE);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                return null;
            }

            RegoleTempistiche rt = new RegoleTempistiche();

            rt.setIdConfig(1);
            rt.setDurataSlot(rs.getInt("durata_slot"));

            Time open = rs.getTime("ora_apertura");
            Time close = rs.getTime("ora_chiusura");

            if (open != null) {
                rt.setOraApertura(open.toLocalTime());
            }

            if (close != null) {
                rt.setOraChiusura(close.toLocalTime());
            }

            rt.setPreavvisoMinimo(rs.getInt("preavviso_minimo"));

            return rt;

        } catch (SQLException e) {
            throw new DaoException("Errore DBMS RegoleTempistiche rawLoad", e);
        }
    }

    @Override
    protected void rawSave(RegoleTempistiche regole) {
        if (regole == null) {
            return;
        }

        try (Connection c = cf.getConnection()) {
            c.setAutoCommit(false);

            try (PreparedStatement del = c.prepareStatement(SQL_DELETE_ONE)) {
                del.executeUpdate();
            }

            try (PreparedStatement ins = c.prepareStatement(SQL_INSERT_ONE)) {
                ins.setInt(1, regole.getDurataSlot());

                if (regole.getOraApertura() != null) {
                    ins.setTime(2, Time.valueOf(regole.getOraApertura()));
                } else {
                    ins.setNull(2, Types.TIME);
                }

                if (regole.getOraChiusura() != null) {
                    ins.setTime(3, Time.valueOf(regole.getOraChiusura()));
                } else {
                    ins.setNull(3, Types.TIME);
                }

                ins.setInt(4, regole.getPreavvisoMinimo());
                ins.executeUpdate();
            }

            c.commit();

        } catch (SQLException e) {
            throw new DaoException("Errore DBMS RegoleTempistiche rawSave", e);
        }
    }
}
