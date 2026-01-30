package com.ispw.dao.interfaces;

import java.util.List;

import com.ispw.model.entity.Prenotazione;
import com.ispw.model.enums.StatoPrenotazione;

public interface PrenotazioneDAO extends DAO<Integer, Prenotazione> {
    List<Prenotazione> findByUtente(int idUtente);
    List<Prenotazione> findByUtenteAndStato(int idUtente, StatoPrenotazione stato);
    void updateStato(int idPrenotazione, StatoPrenotazione nuovoStato);
}