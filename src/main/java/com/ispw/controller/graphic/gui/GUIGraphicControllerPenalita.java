package com.ispw.controller.graphic.gui;

import java.math.BigDecimal;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.DatiPenalitaBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerPenalita;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.logic.ctrl.LogicControllerApplicaPenalita;

/**
 * Adapter GUI per gestione applicazione penalità.
 */
public class GUIGraphicControllerPenalita implements GraphicControllerPenalita {
    
    @SuppressWarnings("java:S1312")
    private Logger log() { return Logger.getLogger(getClass().getName()); }

    private final GraphicControllerNavigation navigator;
    
    public GUIGraphicControllerPenalita(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }
    
    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_PENALITA;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        // Metodo intenzionalmente vuoto: lifecycle non ancora implementato
    }

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

    @Override
    public void applicaPenalita(int idUtente, float importo, String motivazione) {
        if (isIdUtenteNonValido(idUtente) || isPenalitaNonValida(importo, motivazione)) {
            return;
        }

        try {
            DatiPenalitaBean dati = buildPenalitaBean(idUtente, importo, motivazione);

            LogicControllerApplicaPenalita logicController = new LogicControllerApplicaPenalita();
            EsitoOperazioneBean esito = logicController.applicaSanzione(dati);

            if (esito == null || !esito.isSuccesso()) {
                notifyPenalitaError(esito != null ? esito.getMessaggio()
                    : GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
                return;
            }

            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_PENALITA,
                    Map.of(GraphicControllerUtils.KEY_SUCCESSO, esito.getMessaggio()));
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore applicazione penalità", e);
        }
    }

    @Override
    public void tornaAllaHome() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_HOME, null);
        }
    }

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

}
