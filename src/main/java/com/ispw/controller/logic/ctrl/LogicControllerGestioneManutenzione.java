package com.ispw.controller.logic.ctrl;

import com.ispw.controller.logic.interfaces.manutenzione.GestioneManutenzioneConfiguraRegole;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller secondario STATeless per la gestione della manutenzione.
 * Simula l'invio di un alert al manutentore rispettando le regole SonarCloud:
 * - nessun System.out (S106);
 * - logger ottenuto on-demand (una soppressione locale S1312 per evitare campi);
 * - metodi corti con validazioni ed early-return.
 */
public final class LogicControllerGestioneManutenzione implements GestioneManutenzioneConfiguraRegole {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Interazioni con altri componenti (notifica/manutenzione/logging).
    // ========================

    /**
     * Invia (simulato) un alert al manutentore per il campo indicato.
     * Se l'idCampo non è valido, registra un warning ed esce senza effetti collaterali.
     */
    @Override
    public void inviaAlertManutentore(int idCampo) {
        if (idCampo <= 0) {
            log().log(Level.WARNING, "[MANUTENZIONE][WARN] idCampo non valido: {0}", idCampo);
            return;
        }
        // Simulazione dell'invio alert verso sistema esterno
        log().log(Level.INFO, "[MANUTENZIONE] Invio alert manutentore per Campo#{0} ... riuscito", idCampo);
    }

    // ========================
    // SEZIONE LOGICA
    // Logica interna della classe (supporto e utilità).
    // ========================
    /** Logger on-demand per evitare campi (controller stateless). */
    @SuppressWarnings("java:S1312")
    private Logger log() {
        return Logger.getLogger(getClass().getName());
    }
}