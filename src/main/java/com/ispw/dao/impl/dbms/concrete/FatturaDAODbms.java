package com.ispw.dao.impl.dbms.concrete;

import com.ispw.dao.impl.base.BaseFatturaDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.model.entity.Fattura;

/**
 * DBMS-backed Fattura DAO delegating raw I/O to DbmsFatturaDAO.
 */
public class FatturaDAODbms extends BaseFatturaDAO {

    private final DbmsFatturaDAO delegate;

    public FatturaDAODbms(ConnectionFactory cf) {
        super(true);
        this.delegate = new DbmsFatturaDAO(cf);
    }

    @Override
    protected Fattura rawLoad(Integer id) { return delegate.load(id); }

    @Override
    protected void rawStore(Fattura entity) { delegate.store(entity); }

    @Override
    protected void rawDelete(Integer id) { delegate.delete(id); }

    @Override
    protected Fattura rawFindLastByUtente(int idUtente) { return delegate.findLastByUtente(idUtente); }
}
