package com.ispw.dao.impl.dbms.concrete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;

import com.ispw.dao.impl.dbms.base.DbmsDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.dao.interfaces.RegoleTempisticheDAO;
import com.ispw.model.entity.RegoleTempistiche;

public final class DbmsRegoleTempisticheDAO
        extends DbmsDAO<Integer, RegoleTempistiche>
        implements RegoleTempisticheDAO {

    private static final String SQL_SELECT_ONE =
        "SELECT durata_slot, ora_apertura, ora_chiusura, preavviso_minimo " +
        "FROM regole_tempistiche WHERE id = 1";

    private static final String SQL_DELETE_ONE =
        "DELETE FROM regole_tempistiche WHERE id = 1";

    private static final String SQL_INSERT_ONE =
        "INSERT INTO regole_tempistiche (id, durata_slot, ora_apertura, ora_chiusura, preavviso_minimo) " +
        "VALUES (1, ?, ?, ?, ?)";

    public DbmsRegoleTempisticheDAO(ConnectionFactory cf) { super(cf); }

    @Override
    public RegoleTempistiche get() {
        return queryOne(SQL_SELECT_ONE, null, (ResultSet rs) -> {
            RegoleTempistiche rt = new RegoleTempistiche();
            rt.setDurataSlot(rs.getInt("durata_slot"));
            Time open  = rs.getTime("ora_apertura");
            Time close = rs.getTime("ora_chiusura");
            if (open  != null)  rt.setOraApertura(open.toLocalTime());
            if (close != null)  rt.setOraChiusura(close.toLocalTime()); // vedi nota sui nomi
            rt.setPreavvisoMinimo(rs.getInt("preavviso_minimo"));
            return rt;
        }).orElse(null);
    }

    @Override
    public void save(RegoleTempistiche regole) {
        if (regole == null) return;
        try (Connection c = openConnection()) {
            c.setAutoCommit(false);

            try (PreparedStatement del = c.prepareStatement(SQL_DELETE_ONE)) {
                del.executeUpdate();
            }
            try (PreparedStatement ins = c.prepareStatement(SQL_INSERT_ONE)) {
                ins.setInt(1, regole.getDurataSlot());
                if (regole.getOraApertura() != null)  ins.setTime(2, Time.valueOf(regole.getOraApertura()));
                else                                  ins.setNull(2, Types.TIME);

                if (regole.getOraChiusura() != null)  ins.setTime(3, Time.valueOf(regole.getOraChiusura()));
                else                                  ins.setNull(3, Types.TIME);

                ins.setInt(4, regole.getPreavvisoMinimo());
                ins.executeUpdate();
            }

            c.commit();
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    // Non usati nello scenario "singleton row"
    @Override public RegoleTempistiche load(Integer id)                 { throw new UnsupportedOperationException(); }
    @Override public void store(RegoleTempistiche entity)               { throw new UnsupportedOperationException(); }
    @Override public void delete(Integer id)                            { throw new UnsupportedOperationException(); }
    @Override public boolean exists(Integer id)                         { throw new UnsupportedOperationException(); }
    @Override public RegoleTempistiche create(Integer id)               { throw new UnsupportedOperationException(); }
}
