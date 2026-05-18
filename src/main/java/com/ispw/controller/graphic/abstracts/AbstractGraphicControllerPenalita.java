package com.ispw.controller.graphic.abstracts;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.DatiPenalitaBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.UtenteSelezioneBean;
import com.ispw.bean.UtentiBean;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerPenalita;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.logic.LogicControllerFactory;
import com.ispw.controller.logic.interfaces.CtrlApplicaPenalita;

/**
 * Controller grafico astratto del caso d'uso "Applica penalità".
 *
 * Questa classe contiene la logica comune tra GUI e CLI:
 * - richiede la lista utenti selezionabili;
 * - permette la selezione di un utente;
 * - costruisce il bean della penalità;
 * - chiama il controller logico;
 * - aggiorna la route penalità tramite navigator.
 *
 * Nota di progetto:
 * il graphic controller dipende dall'interfaccia CtrlApplicaPenalita,
 * ottenuta tramite LogicControllerFactory.
 */
public abstract class AbstractGraphicControllerPenalita implements GraphicControllerPenalita {

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicControllerPenalita(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    protected abstract Logger log();

    protected abstract void goToHome();

    // =====================================================================
    // LOGIC CONTROLLER
    // =====================================================================

    protected CtrlApplicaPenalita logicController() {
        return LogicControllerFactory.getPenalitaController();
    }

    // =====================================================================
    // NAVIGAZIONE
    // =====================================================================

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_PENALITA;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        // In questa schermata non è necessario gestire parametri in modo comune.
    }

    // STEP 1: lista utenti

    /**
     * Richiede la lista degli utenti a cui è possibile applicare una penalità.
     */
    public void richiediListaUtenti() {
        try {
            List<String> utenti = formatUtenti(logicController().listaUtentiPerPenalita());

            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_PENALITA,
                        Map.of(GraphicControllerUtils.KEY_UTENTI, utenti));
            }
        } catch (RuntimeException ex) {
            log().log(Level.SEVERE, "Errore recupero lista utenti", ex);
        }
    }

    // STEP 2: selezione utente

    /**
     * Seleziona un utente tramite email.
     *
     * Il metodo non applica ancora la penalità:
     * aggiorna soltanto la route con l'utente selezionato.
     */
    @Override
    public void selezionaUtente(String email) {
        if (isEmailNonValida(email)) {
            return;
        }

        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_PENALITA,
                    Map.of(GraphicControllerUtils.KEY_EMAIL, email.trim()));
        }
    }

    // STEP 3: applicazione penalità

    /**
     * Applica una penalità a un utente.
     *
     * Il metodo:
     * - controlla id utente, importo e motivazione;
     * - costruisce DatiPenalitaBean;
     * - chiama il controller logico;
     * - naviga con successo o mostra errore.
     */
    @Override
    public void applicaPenalita(int idUtente, float importo, String motivazione) {
        if (isIdUtenteNonValido(idUtente) || isPenalitaNonValida(importo, motivazione)) {
            return;
        }

        try {
            DatiPenalitaBean dati = buildPenalitaBean(idUtente, importo, motivazione);

            EsitoOperazioneBean esito = logicController().applicaSanzione(dati, null, null);

            if (esito == null || !esito.isSuccesso()) {
                notifyPenalitaError(esito != null
                        ? esito.getMessaggio()
                        : GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
                return;
            }

            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_PENALITA,
                        Map.of(GraphicControllerUtils.KEY_SUCCESSO, esito.getMessaggio()));
            }
        } catch (RuntimeException ex) {
            log().log(Level.SEVERE, "Errore applicazione penalita", ex);
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
    // HELPERS PENALITÀ
    // =====================================================================

    private void notifyPenalitaError(String message) {
        GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_PENALITA,
                GraphicControllerUtils.PREFIX_PENALITA, message);
    }

    private boolean isEmailNonValida(String email) {
        if (email == null || email.isBlank()) {
            notifyPenalitaError(GraphicControllerUtils.MSG_EMAIL_UTENTE_NON_VALIDA);
            return true;
        }

        return false;
    }

    private boolean isIdUtenteNonValido(int idUtente) {
        if (idUtente <= 0) {
            notifyPenalitaError(GraphicControllerUtils.MSG_ID_UTENTE_NON_VALIDO);
            return true;
        }

        return false;
    }

    private boolean isPenalitaNonValida(float importo, String motivazione) {
        if (motivazione == null || motivazione.isBlank() || importo <= 0) {
            notifyPenalitaError(GraphicControllerUtils.MSG_DATI_PENALITA_NON_VALIDI);
            return true;
        }

        return false;
    }

    private DatiPenalitaBean buildPenalitaBean(int idUtente, float importo, String motivazione) {
        DatiPenalitaBean dati = new DatiPenalitaBean();
        dati.setIdUtente(idUtente);
        dati.setMotivazione(motivazione.trim());
        dati.setImporto(BigDecimal.valueOf(importo));

        return dati;
    }

    private List<String> formatUtenti(UtentiBean utenti) {
        if (utenti == null || utenti.getUtenti() == null || utenti.getUtenti().isEmpty()) {
            return List.of("Nessun utente disponibile");
        }

        return utenti.getUtenti().stream()
                .map(this::formatUtente)
                .toList();
    }

    private String formatUtente(UtenteSelezioneBean u) {
        if (u == null) {
            return "";
        }

        String email = u.getEmail() != null ? u.getEmail() : "";
        return String.format("#%d - %s (%s)", u.getIdUtente(), email, u.getRuolo());
    }
}