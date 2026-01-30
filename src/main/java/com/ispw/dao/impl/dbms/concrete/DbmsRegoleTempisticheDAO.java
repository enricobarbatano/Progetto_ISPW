package com.ispw.dao.impl.dbms.concrete;

import com.ispw.dao.impl.dbms.connection.DbmsConnectionFactory;
import com.ispw.dao.interfaces.RegoleTempisticheDAO;
import com.ispw.model.entity.RegoleTempistiche;

public final class DbmsRegoleTempisticheDAO implements RegoleTempisticheDAO {

    private static volatile DbmsRegoleTempisticheDAO instance;

    private final DbmsConnectionFactory cf;

    private DbmsRegoleTempisticheDAO() {
        this.cf = DbmsConnectionFactory.getInstance();
    }

    public static DbmsRegoleTempisticheDAO getInstance() {
        if (instance == null) {
            synchronized (DbmsRegoleTempisticheDAO.class) {
                if (instance == null) {
                    instance = new DbmsRegoleTempisticheDAO();
                }
            }
        }
        return instance;
    }

    @Override
    public RegoleTempistiche get() {
        // TODO: implementare accesso DB
        throw new UnsupportedOperationException("TODO: implementare get() su DBMS per RegoleTempistiche");
    }

    @Override
    public void save(RegoleTempistiche regole) {
        // TODO: implementare accesso DB
        throw new UnsupportedOperationException("TODO: implementare save(...) su DBMS per RegoleTempistiche");
    }
}