package com.ispw.controller.graphic;

import java.util.Map;

public interface GraphicControllerRegistrazione extends NavigableController {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: interfaccia del layer graphic (DIP).
    // A2) IO verso GUI/CLI: usa Map<String, Object> per dati registrazione.
    // A3) Logica delegata: ai controller grafici concreti.
    // ========================

    void inviaDatiRegistrazione(Map<String, Object> datiRegistrazione);
    void vaiAlLogin();
}