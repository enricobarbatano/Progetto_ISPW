package com.ispw.dao.interfaces;

import java.util.List;

import com.ispw.model.entity.Prenotazione;
import com.ispw.model.enums.StatoPrenotazione;

public interface PrenotazioneDAO extends DAO<Integer, Prenotazione> {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: DAO per entita Prenotazione.
    // A2) IO: query per utente e stato.
    // ========================

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) findById/findByUtente/findByUtenteAndStato: query.
    // L2) updateStato: aggiornamento stato prenotazione.
    // ========================
    Prenotazione findById(int idPrenotazione);
    List<Prenotazione> findByUtente(int idUtente);
    List<Prenotazione> findByUtenteAndStato(int idUtente, StatoPrenotazione stato);
    void updateStato(int idPrenotazione, StatoPrenotazione nuovoStato);
}