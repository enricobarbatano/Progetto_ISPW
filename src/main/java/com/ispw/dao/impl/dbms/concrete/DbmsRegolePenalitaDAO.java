package com.ispw.dao.impl.dbms.concrete;

import com.ispw.dao.impl.dbms.connection.DbmsConnectionFactory;
import com.ispw.dao.interfaces.RegolePenalitaDAO;
import com.ispw.model.entity.RegolePenalita;

public final class DbmsRegolePenalitaDAO implements RegolePenalitaDAO {

    private static volatile DbmsRegolePenalitaDAO instance;

    private final DbmsConnectionFactory cf;

    private DbmsRegolePenalitaDAO() {
        this.cf = DbmsConnectionFactory.getInstance();
    }

    public static DbmsRegolePenalitaDAO getInstance() {
        if (instance == null) {
            synchronized (DbmsRegolePenalitaDAO.class) {
                if (instance == null) {
                    instance = new DbmsRegolePenalitaDAO();
                }
            }
        }
        return instance;
    }

    @Override
    public RegolePenalita get() {
        // TODO: implementare accesso DB
        throw new UnsupportedOperationException("TODO: implementare get() su DBMS per RegolePenalita");
    }

    @Override
    public void save(RegolePenalita regole) {
        // TODO: implementare accesso DB
        throw new UnsupportedOperationException("TODO: implementare save(...) su DBMS per RegolePenalita");
    }
}