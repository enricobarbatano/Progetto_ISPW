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
        if (email == null || email.isBlank()) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_PENALITA,
                    GraphicControllerUtils.PREFIX_PENALITA, "Email utente non valida");
            return;
        }
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_PENALITA, Map.of("email", email.trim()));
        }
    }

    @Override
    public void applicaPenalita(int idUtente, float importo, String motivazione) {
        if (idUtente <= 0) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_PENALITA,
                    GraphicControllerUtils.PREFIX_PENALITA, "Id utente non valido");
            return;
        }
        if (motivazione == null || motivazione.isBlank() || importo <= 0) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_PENALITA,
                    GraphicControllerUtils.PREFIX_PENALITA, "Dati penalità non validi");
            return;
        }

        try {
            DatiPenalitaBean dati = new DatiPenalitaBean();
            dati.setIdUtente(idUtente);
            dati.setMotivazione(motivazione.trim());
            dati.setImporto(BigDecimal.valueOf(importo));

            LogicControllerApplicaPenalita logicController = new LogicControllerApplicaPenalita();
            EsitoOperazioneBean esito = logicController.applicaSanzione(dati);

            if (esito == null || !esito.isSuccesso()) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_PENALITA,
                    GraphicControllerUtils.PREFIX_PENALITA,
                    esito != null ? esito.getMessaggio() : "Operazione non riuscita");
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

}
