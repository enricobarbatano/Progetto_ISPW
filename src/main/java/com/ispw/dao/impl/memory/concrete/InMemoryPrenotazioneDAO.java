package com.ispw.dao.impl.memory.concrete;

import java.util.List;

import com.ispw.dao.impl.memory.In_MemoryDAO;
import com.ispw.dao.interfaces.PrenotazioneDAO;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.enums.StatoPrenotazione;

public class InMemoryPrenotazioneDAO extends In_MemoryDAO<Integer, Prenotazione> implements PrenotazioneDAO {

    public InMemoryPrenotazioneDAO() {
        super(true);
    }

    @Override
    protected Integer getId(Prenotazione entity) {
        // TODO: return entity.getIdPrenotazione();
        throw new UnsupportedOperationException("TODO: Prenotazione.getIdPrenotazione()");
    }

    @Override
    public List<Prenotazione> findByUtente(int idUtente) {
        throw new UnsupportedOperationException("TODO: findByUtente");
    }

    @Override
    public List<Prenotazione> findByUtenteAndStato(int idUtente, StatoPrenotazione stato) {
        throw new UnsupportedOperationException("TODO: findByUtenteAndStato");
    }

    @Override
    public void updateStato(int idPrenotazione, StatoPrenotazione nuovoStato) {
        throw new UnsupportedOperationException("TODO: updateStato");
    }
}
