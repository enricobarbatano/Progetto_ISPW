package com.ispw.controller.graphic.abstracts;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.RichiestaDisdettaBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerRichiesteDisdetta;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.logic.LogicControllerFactory;
import com.ispw.controller.logic.interfaces.CtrlDisdetta;
import com.ispw.model.enums.Ruolo;

/**
 * Controller grafico astratto del flusso gestore del caso d'uso "Disdici prenotazione".
 *
 * Questo controller rappresenta la parte del caso d'uso usata dal gestore:
 * - caricare le richieste di disdetta in stato PENDING;
 * - approvare una richiesta;
 * - rifiutare una richiesta.
 *
 * Nota di progetto:
 * il caso d'uso disdetta è condiviso tra due attori.
 * Questo controller grafico gestisce il lato gestore, mentre
 * AbstractGraphicControllerDisdetta gestisce il lato utente.
 * Entrambi delegano allo stesso logic controller tramite CtrlDisdetta.
 */
public abstract class AbstractGraphicControllerRichiesteDisdetta implements GraphicControllerRichiesteDisdetta {

    // =====================================================================
    // COLLABORATORI
    // =====================================================================

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicControllerRichiesteDisdetta(GraphicControllerNavigation navigator) {
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
        return GraphicControllerUtils.ROUTE_RICHIESTE_DISDETTA;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        GraphicControllerUtils.handleOnShow(
                log(),
                params,
                GraphicControllerUtils.PREFIX_RICHIESTE_DISDETTA
        );
    }

    // STEP 1: caricamento richieste pending

    /**
     * Carica le richieste di disdetta ancora da valutare.
     *
     * Il metodo:
     * - controlla che la sessione appartenga a un gestore;
     * - chiama il controller logico;
     * - formatta le richieste;
     * - aggiorna la route richieste disdetta.
     */
    @Override
    public void caricaRichiestePending(SessioneUtenteBean sessioneGestore) {
        if (isSessioneNonValida(sessioneGestore, GraphicControllerUtils.MSG_SESSIONE_NON_VALIDA)) {
            return;
        }

        try {
            List<RichiestaDisdettaBean> pending =
                    logicController().listaRichiestePending(sessioneGestore);

            List<String> elenco = pending.stream()
                    .map(Object::toString)
                    .toList();

            if (navigator != null) {
                navigator.goTo(
                        GraphicControllerUtils.ROUTE_RICHIESTE_DISDETTA,
                        Map.of(GraphicControllerUtils.KEY_RICHIESTE, elenco)
                );
            }
        } catch (RuntimeException ex) {
            log().log(Level.SEVERE, "Errore caricamento richieste disdetta", ex);
            notifyError("Errore caricamento richieste disdetta");
        }
    }

    // STEP 2: approvazione richiesta

    /**
     * Approva una richiesta di disdetta.
     */
    @Override
    public void approva(int idRichiesta, String nota, SessioneUtenteBean sessioneGestore) {
        valuta(idRichiesta, true, nota, sessioneGestore);
    }

    // STEP 3: rifiuto richiesta

    /**
     * Rifiuta una richiesta di disdetta.
     */
    @Override
    public void rifiuta(int idRichiesta, String nota, SessioneUtenteBean sessioneGestore) {
        valuta(idRichiesta, false, nota, sessioneGestore);
    }

    /**
     * Valuta una richiesta di disdetta.
     *
     * Il parametro approva stabilisce se la richiesta deve essere approvata o rifiutata.
     */
    private void valuta(int idRichiesta, boolean approva, String nota, SessioneUtenteBean sessioneGestore) {
        if (isIdNonValido(idRichiesta, "Id richiesta non valido")
                || isSessioneNonValida(sessioneGestore, GraphicControllerUtils.MSG_SESSIONE_NON_VALIDA)) {
            return;
        }

        try {
            EsitoOperazioneBean esito =
                    logicController().valutaRichiestaDisdetta(idRichiesta, approva, nota, sessioneGestore);

            if (esito == null || !esito.isSuccesso()) {
                notifyError(esito != null
                        ? esito.getMessaggio()
                        : GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
                return;
            }

            if (navigator != null) {
                navigator.goTo(
                        GraphicControllerUtils.ROUTE_RICHIESTE_DISDETTA,
                        Map.of(GraphicControllerUtils.KEY_SUCCESSO, esito.getMessaggio())
                );
            }
        } catch (RuntimeException ex) {
            log().log(Level.SEVERE, "Errore valutazione richiesta disdetta", ex);
            notifyError("Errore valutazione richiesta disdetta");
        }
    }

    // STEP 4: ritorno home

    /**
     * Torna alla home delegando il comportamento concreto alla classe GUI o CLI.
     */
    @Override
    public void tornaAllaHome() {
        goToHome();
    }

    // =====================================================================
    // HELPERS RICHIESTE DISDETTA
    // =====================================================================

    /**
     * Notifica un errore relativo alla gestione delle richieste disdetta.
     */
    private void notifyError(String message) {
        GraphicControllerUtils.notifyError(
                log(),
                navigator,
                GraphicControllerUtils.ROUTE_RICHIESTE_DISDETTA,
                GraphicControllerUtils.PREFIX_RICHIESTE_DISDETTA,
                message
        );
    }

    /**
     * Controlla che la sessione sia valida e appartenga a un gestore.
     */
    private boolean isSessioneNonValida(SessioneUtenteBean sessione, String message) {
        if (sessione == null || sessione.getUtente() == null) {
            notifyError(message);
            return true;
        }

        if (sessione.getUtente().getRuolo() != Ruolo.GESTORE) {
            notifyError("Operazione riservata al gestore");
            return true;
        }

        return false;
    }

    /**
     * Controlla che l'id richiesta sia valido.
     */
    private boolean isIdNonValido(int idRichiesta, String message) {
        if (idRichiesta <= 0) {
            notifyError(message);
            return true;
        }

        return false;
    }
}
