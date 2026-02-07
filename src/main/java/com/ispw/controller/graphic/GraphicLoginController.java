package com.ispw.controller.graphic;

import com.ispw.bean.DatiLoginBean;

public interface GraphicLoginController extends NavigableController {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: interfaccia del layer graphic (DIP).
    // A2) IO verso GUI/CLI: usa DatiLoginBean per credenziali.
    // A3) Logica delegata: ai controller grafici concreti.

    void effettuaLogin(DatiLoginBean credenziali);
    void logout();
    void vaiARegistrazione();
    void vaiAHome();
}
