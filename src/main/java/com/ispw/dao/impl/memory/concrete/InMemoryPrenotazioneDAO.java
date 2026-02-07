package com.ispw.dao.impl.memory.concrete;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.ispw.dao.impl.memory.InMemoryDAO;
import com.ispw.dao.interfaces.PrenotazioneDAO;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.enums.StatoPrenotazione;

/**
 * SEZIONE ARCHITETTURALE
 * Ruolo: DAO InMemory per Prenotazione.
 * Responsabilita': gestire persistenza volatile per test/uso locale.
 *
 * SEZIONE LOGICA
 * Delega a InMemoryDAO e applica filtri per utente/campo/stato.
 */
public class InMemoryPrenotazioneDAO extends InMemoryDAO<Integer, Prenotazione> implements PrenotazioneDAO {

    // Sequenza condivisa (store condiviso: super(true))
    private static final AtomicInteger SEQ = new AtomicInteger(0);

    public InMemoryPrenotazioneDAO() { super(true); }

    @Override
    protected Integer getId(Prenotazione entity) {
        // entity espone int â†’ autoboxing verso Integer per la mappa
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
    public Prenotazione findById(int idPrenotazione) {
        return load(idPrenotazione);
    }

    @Override
    public List<Prenotazione> findByUtente(int idUtente) {
        List<Prenotazione> out = filter(p -> p.getIdUtente() == idUtente);
        out.sort(ORDER_BY_DATA_ORA_ID);
        return out;
    }

    @Override
    public List<Prenotazione> findByUtenteAndStato(int idUtente, StatoPrenotazione stato) {
        List<Prenotazione> out = filter(p -> p.getIdUtente() == idUtente && p.getStato() == stato);
        out.sort(ORDER_BY_DATA_ORA_ID);
        return out;
    }

    @Override
    public void updateStato(int idPrenotazione, StatoPrenotazione nuovoStato) {
        Prenotazione p = load(idPrenotazione);
        if (p != null) {
            p.setStato(nuovoStato);
            store(p);
        }
    }

    private static final Comparator<Prenotazione> ORDER_BY_DATA_ORA_ID =
            Comparator.comparing(Prenotazione::getData, Comparator.nullsLast(Comparator.naturalOrder()))
                      .thenComparing(Prenotazione::getOraInizio, Comparator.nullsLast(Comparator.naturalOrder()))
                      .thenComparingInt(Prenotazione::getIdPrenotazione);

        
}
