package com.ispw.dao.impl.dbms.concrete;

import java.util.List;

import com.ispw.dao.impl.base.BasePenalitaDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.model.entity.Penalita;

public class PenalitaDAODbms extends BasePenalitaDAO {

    private final DbmsPenalitaDAO delegate;

    public PenalitaDAODbms(ConnectionFactory cf) {
        super(true);
        this.delegate = new DbmsPenalitaDAO(cf);
    }

    @Override
    protected Penalita rawLoad(Integer id) { return delegate.load(id); }

    @Override
    protected void rawStore(Penalita entity) { delegate.store(entity); }

    @Override
    protected void rawDelete(Integer id) { delegate.delete(id); }

    @Override
    protected List<Penalita> rawFindByUtente(int idUtente) { return delegate.recuperaPenalitaUtente(idUtente); }
}
