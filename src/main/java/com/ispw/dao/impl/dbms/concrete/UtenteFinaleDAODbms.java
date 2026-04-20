package com.ispw.dao.impl.dbms.concrete;

import com.ispw.dao.impl.base.BaseUtenteFinaleDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.model.entity.UtenteFinale;

/**
 * DBMS-backed UtenteFinale DAO that delegates raw I/O to existing DbmsUtenteFinaleDAO.
 */
public class UtenteFinaleDAODbms extends BaseUtenteFinaleDAO {

    private final DbmsUtenteFinaleDAO delegate;

    public UtenteFinaleDAODbms(ConnectionFactory cf) {
        super(true);
        this.delegate = new DbmsUtenteFinaleDAO(cf);
    }

    @Override
    protected UtenteFinale rawLoad(Integer id) { return delegate.load(id); }

    @Override
    protected void rawStore(UtenteFinale entity) { delegate.store(entity); }

    @Override
    protected void rawDelete(Integer id) { delegate.delete(id); }

    @Override
    protected UtenteFinale rawFindByEmail(String email) { return delegate.findByEmail(email); }
}
