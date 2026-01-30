package com.ispw.dao.impl.dbms.concrete;

import com.ispw.dao.impl.dbms.base.DbmsDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.dao.interfaces.UtenteFinaleDAO;
import com.ispw.model.entity.UtenteFinale;

public class DbmsUtenteFinaleDAO extends DbmsDAO<Integer, UtenteFinale> implements UtenteFinaleDAO {

    public DbmsUtenteFinaleDAO(ConnectionFactory cf) { super(cf); }

    @Override public UtenteFinale load(Integer id) { throw new UnsupportedOperationException(); }
    @Override public void store(UtenteFinale entity) { throw new UnsupportedOperationException(); }
    @Override public void delete(Integer id) { throw new UnsupportedOperationException(); }
    @Override public boolean exists(Integer id) { throw new UnsupportedOperationException(); }
    @Override public UtenteFinale create(Integer id) { throw new UnsupportedOperationException(); }

    @Override public UtenteFinale findById(int idUtente) { throw new UnsupportedOperationException(); }
    @Override public UtenteFinale findByEmail(String email) { throw new UnsupportedOperationException(); }
}