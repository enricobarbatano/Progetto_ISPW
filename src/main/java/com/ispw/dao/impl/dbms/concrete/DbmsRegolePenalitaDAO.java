package com.ispw.dao.impl.dbms.concrete;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.ispw.dao.impl.dbms.base.DbmsDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.dao.interfaces.RegolePenalitaDAO;
import com.ispw.model.entity.RegolePenalita;

public final class DbmsRegolePenalitaDAO
        extends DbmsDAO<Integer, RegolePenalita>
        implements RegolePenalitaDAO {

    private static final String SQL_SELECT_ONE =
        "SELECT valore_penalita, preavviso_minimo FROM regole_penalita WHERE id = 1";

    private static final String SQL_DELETE_ONE =
        "DELETE FROM regole_penalita WHERE id = 1";

    private static final String SQL_INSERT_ONE =
        "INSERT INTO regole_penalita (id, valore_penalita, preavviso_minimo) VALUES (1, ?, ?)";

    public DbmsRegolePenalitaDAO(ConnectionFactory cf) { super(cf); }

    @Override
    public RegolePenalita get() {
        return queryOne(SQL_SELECT_ONE, null, (ResultSet rs) -> {
            RegolePenalita rp = new RegolePenalita();
            // Entity vuole BigDecimal:
            rp.setValorePenalita(rs.getBigDecimal("valore_penalita"));
            rp.setPreavvisoMinimo(rs.getInt("preavviso_minimo"));
            return rp;
        }).orElse(null);
    }

    @Override
    public void save(RegolePenalita regole) {
        if (regole == null) return;
        try (Connection c = openConnection()) {
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
            throw wrap(e);
        }
    }

    // Non usati per lo scenario "singleton row"
    @Override public RegolePenalita load(Integer id)                 { throw new UnsupportedOperationException(); }
    @Override public void store(RegolePenalita entity)               { throw new UnsupportedOperationException(); }
    @Override public void delete(Integer id)                         { throw new UnsupportedOperationException(); }
    @Override public boolean exists(Integer id)                      { throw new UnsupportedOperationException(); }
    @Override public RegolePenalita create(Integer id)               { throw new UnsupportedOperationException(); }
}
