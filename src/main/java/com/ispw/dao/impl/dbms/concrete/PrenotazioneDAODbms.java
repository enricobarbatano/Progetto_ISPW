package com.ispw.dao.impl.dbms.concrete;

import java.util.List;

import com.ispw.dao.impl.base.BasePrenotazioneDAO;
import com.ispw.dao.impl.dbms.connection.ConnectionFactory;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.enums.StatoPrenotazione;

/**
 * DBMS-backed Prenotazione DAO that implements only raw I/O by delegating
 * to the existing DbmsPrenotazioneDAO (which performs JDBC operations).
 */
public class PrenotazioneDAODbms extends BasePrenotazioneDAO {

    private final DbmsPrenotazioneDAO delegate;

    public PrenotazioneDAODbms(ConnectionFactory cf) {
        super(true);
        this.delegate = new DbmsPrenotazioneDAO(cf);
    }

    @Override
    protected Prenotazione rawLoad(Integer id) {
        return delegate.load(id);
    }

    @Override
    protected void rawStore(Prenotazione entity) {
        delegate.store(entity);
    }

    @Override
    protected void rawDelete(Integer id) {
        delegate.delete(id);
    }

    @Override
    protected List<Prenotazione> rawFindByUtente(int idUtente) {
        return delegate.findByUtente(idUtente);
    }

    @Override
    protected List<Prenotazione> rawFindByUtenteAndStato(int idUtente, StatoPrenotazione stato) {
        return delegate.findByUtenteAndStato(idUtente, stato);
    }

    @Override
    protected void rawUpdateStato(int idPrenotazione, StatoPrenotazione nuovoStato) {
        delegate.updateStato(idPrenotazione, nuovoStato);
    }
}
