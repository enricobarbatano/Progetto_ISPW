package com.ispw.controller.graphic.interfaces;

import com.ispw.model.enums.Ruolo;

public interface GraphicControllerRegistrazione extends NavigableController {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: interfaccia del layer graphic (DIP).
    // A2) IO verso GUI/CLI: riceve valori grezzi dalla view.
    // A3) Logica delegata: ai controller grafici concreti, che creano DatiRegistrazioneBean.

    void inviaDatiRegistrazione(
        String nome,
        String cognome,
        String email,
        String password,
        Ruolo ruolo
    );

    void vaiAlLogin();
}