package com.ispw.dao.impl.dbms.concrete;

import java.util.List;

import com.ispw.dao.impl.dbms.base.DbmsDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.model.entity.SystemLog;

public class DbmsLogDAO extends DbmsDAO<Integer, SystemLog> implements LogDAO {

    public DbmsLogDAO(ConnectionFactory cf) { super(cf); }

    @Override public SystemLog load(Integer id) { throw new UnsupportedOperationException(); }
    @Override public void store(SystemLog entity) { throw new UnsupportedOperationException(); }
    @Override public void delete(Integer id) { throw new UnsupportedOperationException(); }
    @Override public boolean exists(Integer id) { throw new UnsupportedOperationException(); }
    @Override public SystemLog create(Integer id) { throw new UnsupportedOperationException(); }

    @Override public void append(SystemLog log) { throw new UnsupportedOperationException(); }
    @Override public List<SystemLog> findByUtente(int idUtente) { throw new UnsupportedOperationException(); }
    @Override public List<SystemLog> findLast(int limit) { throw new UnsupportedOperationException(); }
}