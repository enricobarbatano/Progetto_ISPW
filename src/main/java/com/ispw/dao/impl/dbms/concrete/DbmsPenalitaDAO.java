package com.ispw.dao.impl.dbms.concrete;

import java.util.List;

import com.ispw.dao.impl.dbms.base.DbmsDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.dao.interfaces.PenalitaDAO;
import com.ispw.model.entity.Penalita;

public class DbmsPenalitaDAO extends DbmsDAO<Integer, Penalita> implements PenalitaDAO {

    public DbmsPenalitaDAO(ConnectionFactory cf) { super(cf); }

    @Override public Penalita load(Integer id) { throw new UnsupportedOperationException(); }
    @Override public void store(Penalita entity) { throw new UnsupportedOperationException(); }
    @Override public void delete(Integer id) { throw new UnsupportedOperationException(); }
    @Override public boolean exists(Integer id) { throw new UnsupportedOperationException(); }
    @Override public Penalita create(Integer id) { throw new UnsupportedOperationException(); }

    @Override public List<Penalita> recuperaPenalitaUtente(int idUtente) { throw new UnsupportedOperationException(); }
}