package com.ispw.dao.impl.memory.concrete;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.ispw.dao.impl.memory.InMemoryDAO;
import com.ispw.dao.interfaces.PrenotazioneDAO;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.enums.StatoPrenotazione;

public class InMemoryPrenotazioneDAO extends InMemoryDAO<Integer, Prenotazione> implements PrenotazioneDAO {

    // Sequenza condivisa (store condiviso: super(true))
    private static final AtomicInteger SEQ = new AtomicInteger(0);

    public InMemoryPrenotazioneDAO() { super(true); }

    @Override
    protected Integer getId(Prenotazione entity) {
        // entity espone int → autoboxing verso Integer per la mappa
        return entity.getIdPrenotazione();
    }

    @Override
    public void store(Prenotazione entity) {
        if (entity.getIdPrenotazione() <= 0) {
            entity.setIdPrenotazione(SEQ.incrementAndGet());
        }
        super.store(entity);
    }

    @Override
    public List<Prenotazione> findByUtente(int idUtente) {
        return filter(p -> p.getIdUtente() == idUtente);
    }

    @Override
    public List<Prenotazione> findByUtenteAndStato(int idUtente, StatoPrenotazione stato) {
        return filter(p -> p.getIdUtente() == idUtente && p.getStato() == stato);
    }

    @Override
    public void updateStato(int idPrenotazione, StatoPrenotazione nuovoStato) {
        Prenotazione p = load(idPrenotazione);
        if (p != null) {
            p.setStato(nuovoStato);
            store(p);
        }
    }

        
}
