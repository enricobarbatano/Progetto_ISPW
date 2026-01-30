package com.ispw.dao.impl.dbms.concrete;

import com.ispw.dao.impl.dbms.base.DbmsDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.model.entity.GeneralUser;

public class DbmsGeneralUserDAO extends DbmsDAO<Integer, GeneralUser> implements GeneralUserDAO {

    public DbmsGeneralUserDAO(ConnectionFactory cf) { super(cf); }

    @Override public GeneralUser load(Integer id) { throw new UnsupportedOperationException(); }
    @Override public void store(GeneralUser entity) { throw new UnsupportedOperationException(); }
    @Override public void delete(Integer id) { throw new UnsupportedOperationException(); }
    @Override public boolean exists(Integer id) { throw new UnsupportedOperationException(); }
    @Override public GeneralUser create(Integer id) { throw new UnsupportedOperationException(); }

    @Override public GeneralUser findByEmail(String email) { throw new UnsupportedOperationException(); }
    @Override public GeneralUser findById(int idUtente) { throw new UnsupportedOperationException(); }
}