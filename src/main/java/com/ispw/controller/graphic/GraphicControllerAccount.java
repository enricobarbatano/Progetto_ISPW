package com.ispw.controller.graphic;

import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;

/**
 * Graphic controller per UC di gestione account (recupero/aggiornamento dati, cambio password, logout).
 * Stateless: non mantiene stato tra invocazioni; eventuali dati sono in params o nel model/bean di sessione.
 */
public interface GraphicControllerAccount extends NavigableController {

    /**
     * Carica e mostra i dati account dell'utente corrente.
     * (es. delega a LogicControllerGestioneAccount.recuperaInformazioniAccount)
     */
    void loadAccount(SessioneUtenteBean sessione);

    /**
     * Richiesta di aggiornamento dei dati account.
     * @param nuoviDati mappa con i campi modificati (es. nome, email, ecc.) o bean serializzato in map.
     */
    void aggiornaDatiAccount(Map<String, Object> nuoviDati);

    /**
     * Cambia password dell’utente corrente.
     */
    void cambiaPassword(String vecchiaPassword, String nuovaPassword, SessioneUtenteBean sessione);

    /**
     * Logout dell’utente corrente e navigazione conseguente (tipicamente verso login).
     */
    void logout();
}