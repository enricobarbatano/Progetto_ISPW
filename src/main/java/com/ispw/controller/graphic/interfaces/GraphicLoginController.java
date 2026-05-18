package com.ispw.controller.graphic.interfaces;

public interface GraphicLoginController extends NavigableController {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: interfaccia del layer graphic (DIP).
    // A2) IO verso GUI/CLI: riceve valori grezzi dalla view.
    // A3) Logica delegata: ai controller grafici concreti, che creano DatiLoginBean.

    void effettuaLogin(String email, String password);

    void logout();

    void vaiARegistrazione();

    void vaiAHome();
}
