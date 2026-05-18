package com.ispw.controller.graphic.abstracts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.EsitoDisdettaBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.RiepilogoPrenotazioneBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.interfaces.GraphicControllerDisdetta;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.logic.LogicControllerFactory;
import com.ispw.controller.logic.interfaces.CtrlDisdetta;

/**
 * Controller grafico astratto del flusso utente del caso d'uso "Disdici prenotazione".
 *
 * Questo controller rappresenta la parte del caso d'uso usata dall'utente finale:
 * - visualizzare le prenotazioni cancellabili;
 * - selezionare una prenotazione;
 * - visualizzare l'anteprima della disdetta;
 * - inviare una richiesta di disdetta in stato PENDING.
 *
 * Nota di progetto:
 * il caso d'uso disdetta è condiviso tra due attori.
 * Questo controller grafico gestisce il lato utente, mentre un altro controller
 * grafico gestisce il lato gestore. Entrambi delegano allo stesso logic controller.
 */
public abstract class AbstractGraphicControllerDisdetta implements GraphicControllerDisdetta {

    // =====================================================================
    // COLLABORATORI
    // =====================================================================

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicControllerDisdetta(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    protected abstract Logger log();

    protected abstract void goToHome();

    // =====================================================================
    // LOGIC CONTROLLER
    // =====================================================================

    protected CtrlDisdetta logicController() {
        return LogicControllerFactory.getDisdettaController();
    }

    // =====================================================================
    // NAVIGAZIONE
    // =====================================================================

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_DISDETTA;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        GraphicControllerUtils.handleOnShow(log(), params, GraphicControllerUtils.PREFIX_DISDETTA);
    }

    // STEP 1: prenotazioni cancellabili

    /**
     * Richiede le prenotazioni che l'utente può ancora disdire.
     *
     * Il metodo:
     * - controlla la sessione;
     * - chiama il logic controller;
     * - formatta le prenotazioni;
     * - aggiorna la route disdetta.
     */
    @Override
    public void richiediPrenotazioniCancellabili(SessioneUtenteBean sessione) {
        if (isSessioneNonValida(sessione, GraphicControllerUtils.MSG_SESSIONE_UTENTE_MANCANTE)) {
            return;
        }

        try {
            List<RiepilogoPrenotazioneBean> prenotazioni =
                    logicController().ottieniPrenotazioniCancellabili(sessione.getUtente());

            List<String> elenco = prenotazioni.stream()
                    .map(RiepilogoPrenotazioneBean::toString)
                    .toList();

            if (navigator != null) {
                navigator.goTo(
                        GraphicControllerUtils.ROUTE_DISDETTA,
                        Map.of(GraphicControllerUtils.KEY_PRENOTAZIONI, elenco)
                );
            }
        } catch (RuntimeException ex) {
            log().log(Level.SEVERE, "Errore richiesta prenotazioni cancellabili", ex);
            notifyDisdettaError(GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
        }
    }

    // STEP 2: selezione prenotazione

    /**
     * Seleziona una prenotazione da disdire.
     *
     * Il metodo non modifica dati persistenti:
     * aggiorna soltanto la route con l'id prenotazione selezionato.
     */
    @Override
    public void selezionaPrenotazione(int idPrenotazione) {
        if (isIdNonValido(idPrenotazione, GraphicControllerUtils.MSG_ID_PRENOTAZIONE_NON_VALIDO)) {
            return;
        }

        if (navigator != null) {
            navigator.goTo(
                    GraphicControllerUtils.ROUTE_DISDETTA,
                    Map.of(GraphicControllerUtils.KEY_ID_PRENOTAZIONE, idPrenotazione)
            );
        }
    }

    // STEP 3: anteprima disdetta

    /**
     * Richiede l'anteprima della disdetta.
     *
     * Il metodo:
     * - controlla id prenotazione e sessione;
     * - chiama il logic controller;
     * - se la disdetta è possibile, mostra penale e stato;
     * - altrimenti notifica errore.
     */
    @Override
    public void richiediAnteprimaDisdetta(int idPrenotazione, SessioneUtenteBean sessione) {
        if (isIdNonValido(idPrenotazione, GraphicControllerUtils.MSG_ID_PRENOTAZIONE_NON_VALIDO)
                || isSessioneNonValida(sessione, GraphicControllerUtils.MSG_SESSIONE_UTENTE_MANCANTE)) {
            return;
        }

        try {
            EsitoDisdettaBean esito = logicController().anteprimaDisdetta(idPrenotazione, sessione);

            if (esito == null || !esito.isPossibile()) {
                notifyDisdettaError(GraphicControllerUtils.MSG_DISDETTA_NON_CONSENTITA);
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put(GraphicControllerUtils.KEY_POSSIBILE, esito.isPossibile());
            payload.put(GraphicControllerUtils.KEY_PENALE, esito.getPenale());

            if (navigator != null) {
                navigator.goTo(
                        GraphicControllerUtils.ROUTE_DISDETTA,
                        Map.of(GraphicControllerUtils.KEY_ANTEPRIMA, payload)
                );
            }
        } catch (RuntimeException ex) {
            log().log(Level.SEVERE, "Errore anteprima disdetta", ex);
            notifyDisdettaError(GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
        }
    }

    // STEP 4: conferma disdetta

    /**
     * Conferma la richiesta di disdetta.
     *
     * Il metodo non annulla immediatamente la prenotazione:
     * invia una richiesta PENDING che sarà valutata dal gestore.
     */
    @Override
    public void confermaDisdetta(int idPrenotazione, SessioneUtenteBean sessione) {
        if (isIdNonValido(idPrenotazione, GraphicControllerUtils.MSG_ID_PRENOTAZIONE_NON_VALIDO)
                || isSessioneNonValida(sessione, GraphicControllerUtils.MSG_SESSIONE_UTENTE_MANCANTE)) {
            return;
        }

        try {
            // UTENTE invia richiesta PENDING, senza annullare subito la prenotazione
            EsitoOperazioneBean esito = logicController().richiediDisdetta(idPrenotazione, null, sessione);

            if (esito == null || !esito.isSuccesso()) {
                notifyDisdettaError(esito != null
                        ? esito.getMessaggio()
                        : GraphicControllerUtils.MSG_DISDETTA_NON_RIUSCITA);
                return;
            }

            if (navigator != null) {
                navigator.goTo(
                        GraphicControllerUtils.ROUTE_DISDETTA,
                        Map.of(GraphicControllerUtils.KEY_SUCCESSO, esito.getMessaggio())
                );
            }
        } catch (RuntimeException ex) {
            log().log(Level.SEVERE, "Errore invio richiesta disdetta", ex);
            notifyDisdettaError(GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
        }
    }

    // STEP 5: ritorno home

    /**
     * Torna alla home delegando il comportamento concreto alla classe GUI o CLI.
     */
    @Override
    public void tornaAllaHome() {
        goToHome();
    }

    // =====================================================================
    // HELPERS DISDETTA
    // =====================================================================

    /**
     * Notifica un errore relativo alla disdetta e resta sulla route disdetta.
     */
    private void notifyDisdettaError(String message) {
        GraphicControllerUtils.notifyError(
                log(),
                navigator,
                GraphicControllerUtils.ROUTE_DISDETTA,
                GraphicControllerUtils.PREFIX_DISDETTA,
                message
        );
    }

    /**
     * Controlla se la sessione è mancante o non valida.
     */
    private boolean isSessioneNonValida(SessioneUtenteBean sessione, String message) {
        if (sessione == null || sessione.getUtente() == null) {
            notifyDisdettaError(message);
            return true;
        }

        return false;
    }

    /**
     * Controlla se l'id prenotazione è valido.
     */
    private boolean isIdNonValido(int idPrenotazione, String message) {
        if (idPrenotazione <= 0) {
            notifyDisdettaError(message);
            return true;
        }

        return false;
    }
}