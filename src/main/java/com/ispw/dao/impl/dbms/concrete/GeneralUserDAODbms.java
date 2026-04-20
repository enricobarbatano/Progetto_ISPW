package com.ispw.dao.impl.dbms.concrete;

import java.util.List;

import com.ispw.dao.impl.base.BaseGeneralUserDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.model.entity.GeneralUser;

/**
 * DBMS-backed GeneralUser DAO that implements only raw I/O by delegating
 * to the existing DbmsGeneralUserDAO (which performs JDBC operations).
 */
public class GeneralUserDAODbms extends BaseGeneralUserDAO {

    private final DbmsGeneralUserDAO delegate;

    public GeneralUserDAODbms(ConnectionFactory cf) {
        super(true);
        this.delegate = new DbmsGeneralUserDAO(cf);
    }

    @Override
    protected GeneralUser rawLoad(Integer id) {
        return delegate.load(id);
    }

    @Override
    protected void rawStore(GeneralUser entity) {
        delegate.store(entity);
    }

    @Override
    protected void rawDelete(Integer id) {
        delegate.delete(id);
    }

    @Override
    protected List<GeneralUser> rawFindAll() {
        return delegate.findAll();
    }

    @Override
    protected GeneralUser rawFindByEmail(String email) {
        return delegate.findByEmail(email);
    }
}
