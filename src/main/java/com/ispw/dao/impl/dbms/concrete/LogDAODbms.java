package com.ispw.dao.impl.dbms.concrete;

import java.util.List;

import com.ispw.dao.impl.base.BaseLogDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.model.entity.SystemLog;

public class LogDAODbms extends BaseLogDAO {

    private final DbmsLogDAO delegate;

    public LogDAODbms(ConnectionFactory cf) {
        super(true);
        this.delegate = new DbmsLogDAO(cf);
    }

    @Override
    protected SystemLog rawLoad(Integer id) { return delegate.load(id); }

    @Override
    protected void rawAppend(SystemLog log) { delegate.append(log); }

    @Override
    protected List<SystemLog> rawFindByUtente(int idUtente) { return delegate.findByUtente(idUtente); }

    @Override
    protected List<SystemLog> rawFindLast(int limit) { return delegate.findLast(limit); }
}
