package com.ispw.dao.impl.dbms.concrete;

import java.util.List;

import com.ispw.dao.impl.dbms.base.DbmsDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.model.entity.Campo;

public class DbmsCampoDAO extends DbmsDAO<Integer, Campo> implements CampoDAO {

    public DbmsCampoDAO(ConnectionFactory cf) {
        super(cf);
    }

    @Override public Campo load(Integer id) { throw new UnsupportedOperationException("TODO DBMS load Campo"); }
    @Override public void store(Campo entity) { throw new UnsupportedOperationException("TODO DBMS store Campo"); }
    @Override public void delete(Integer id) { throw new UnsupportedOperationException("TODO DBMS delete Campo"); }
    @Override public boolean exists(Integer id) { throw new UnsupportedOperationException("TODO DBMS exists Campo"); }
    @Override public Campo create(Integer id) { throw new UnsupportedOperationException("TODO DBMS create Campo"); }

    @Override public List<Campo> findAll() { throw new UnsupportedOperationException("TODO DBMS findAll"); }
    @Override public Campo findById(int idCampo) { throw new UnsupportedOperationException("TODO DBMS findById"); }
}
