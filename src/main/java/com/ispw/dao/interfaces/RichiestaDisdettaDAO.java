package com.ispw.dao.interfaces;

import java.util.List;

import com.ispw.model.entity.RichiestaDisdettaRimborso;
import com.ispw.model.enums.StatoRichiestaDisdetta;

public interface RichiestaDisdettaDAO extends DAO<Integer, RichiestaDisdettaRimborso> {

    /** Restituisce tutte le richieste */
    List<RichiestaDisdettaRimborso> findAll();

    /** Filtra per stato (es. PENDING) */
    List<RichiestaDisdettaRimborso> findByStato(StatoRichiestaDisdetta stato);

    /** Ritorna l'eventuale richiesta associata a una prenotazione (se esiste) */
    RichiestaDisdettaRimborso findByPrenotazione(int idPrenotazione);

    /** Aggiornamento di stato + metadati decisionali */
    void updateStato(int idRichiesta,
                     StatoRichiestaDisdetta stato,
                     Integer idGestore,
                     String notaGestore);
}
