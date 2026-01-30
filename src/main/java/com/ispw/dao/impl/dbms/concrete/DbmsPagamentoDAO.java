package com.ispw.dao.impl.dbms.concrete;

import com.ispw.dao.impl.dbms.base.DbmsDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.dao.interfaces.PagamentoDAO;
import com.ispw.model.entity.Pagamento;

public class DbmsPagamentoDAO extends DbmsDAO<Integer, Pagamento> implements PagamentoDAO {

    public DbmsPagamentoDAO(ConnectionFactory cf) { super(cf); }

    @Override public Pagamento load(Integer id) { throw new UnsupportedOperationException(); }
    @Override public void store(Pagamento entity) { throw new UnsupportedOperationException(); }
    @Override public void delete(Integer id) { throw new UnsupportedOperationException(); }
    @Override public boolean exists(Integer id) { throw new UnsupportedOperationException(); }
    @Override public Pagamento create(Integer id) { throw new UnsupportedOperationException(); }

    @Override public Pagamento findByPrenotazione(int idPrenotazione) { throw new UnsupportedOperationException(); }
}