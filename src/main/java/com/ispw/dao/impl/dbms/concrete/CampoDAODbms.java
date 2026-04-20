package com.ispw.dao.impl.dbms.concrete;

import java.util.List;

import com.ispw.dao.impl.base.BaseCampoDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.model.entity.Campo;

/**
 * DBMS-backed Campo DAO that delegates raw I/O to the existing DbmsCampoDAO.
 */
public class CampoDAODbms extends BaseCampoDAO {

    private final DbmsCampoDAO delegate;

    public CampoDAODbms(ConnectionFactory cf) {
        super(true);
        this.delegate = new DbmsCampoDAO(cf);
    }

    @Override
    protected Campo rawLoad(Integer id) { return delegate.load(id); }

    @Override
    protected void rawStore(Campo entity) { delegate.store(entity); }

    @Override
    protected void rawDelete(Integer id) { delegate.delete(id); }

    @Override
    protected List<Campo> rawFindAll() { return delegate.findAll(); }
}
