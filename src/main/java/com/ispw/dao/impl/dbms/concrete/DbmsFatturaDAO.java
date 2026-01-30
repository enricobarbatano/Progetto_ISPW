package com.ispw.dao.impl.dbms.concrete;

import com.ispw.dao.impl.dbms.base.DbmsDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.dao.interfaces.FatturaDAO;
import com.ispw.model.entity.Fattura;

public class DbmsFatturaDAO extends DbmsDAO<Integer, Fattura> implements FatturaDAO {

    public DbmsFatturaDAO(ConnectionFactory cf) { super(cf); }

    @Override public Fattura load(Integer id) { throw new UnsupportedOperationException(); }
    @Override public void store(Fattura entity) { throw new UnsupportedOperationException(); }
    @Override public void delete(Integer id) { throw new UnsupportedOperationException(); }
    @Override public boolean exists(Integer id) { throw new UnsupportedOperationException(); }
    @Override public Fattura create(Integer id) { throw new UnsupportedOperationException(); }

    @Override public Fattura findLastByUtente(int idUtente) { throw new UnsupportedOperationException(); }
}