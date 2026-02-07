package com.ispw.dao.impl.dbms.concrete;

import java.util.Set;

import com.ispw.dao.impl.dbms.base.DbmsDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.dao.interfaces.GestoreDAO;
import com.ispw.model.entity.Gestore;
import com.ispw.model.enums.Permesso;

/**
 * SEZIONE ARCHITETTURALE
 * Ruolo: DAO DBMS per Gestore.
 * Responsabilita': gestire accesso al DB tramite SQL e mapping.
 *
 * SEZIONE LOGICA
 * Implementazione non disponibile: metodi non supportati.
 */
public class DbmsGestoreDAO extends DbmsDAO<Integer, Gestore> implements GestoreDAO {

    public DbmsGestoreDAO(ConnectionFactory cf) { super(cf); }

    @Override public Gestore load(Integer id) { throw new UnsupportedOperationException(); }
    @Override public void store(Gestore entity) { throw new UnsupportedOperationException(); }
    @Override public void delete(Integer id) { throw new UnsupportedOperationException(); }
    @Override public boolean exists(Integer id) { throw new UnsupportedOperationException(); }
    @Override public Gestore create(Integer id) { throw new UnsupportedOperationException(); }

    @Override public Gestore findById(int idGestore) { throw new UnsupportedOperationException(); }
    @Override public Gestore findByEmail(String email) { throw new UnsupportedOperationException(); }
    @Override public Set<Permesso> getPermessi(int idGestore) { throw new UnsupportedOperationException(); }
    @Override public boolean hasPermesso(int idGestore, Permesso permesso) { throw new UnsupportedOperationException(); }
    @Override public void assegnaPermesso(int idGestore, Permesso permesso) { throw new UnsupportedOperationException(); }
    @Override public void rimuoviPermesso(int idGestore, Permesso permesso) { throw new UnsupportedOperationException(); }
}
