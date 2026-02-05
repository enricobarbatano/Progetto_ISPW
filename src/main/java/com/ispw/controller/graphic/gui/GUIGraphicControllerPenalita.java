package com.ispw.controller.graphic.gui;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.DatiPenalitaBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerPenalita;
import com.ispw.controller.logic.ctrl.LogicControllerApplicaPenalita;

public class GUIGraphicControllerPenalita extends AbstractGraphicControllerPenalita {
    
    public GUIGraphicControllerPenalita(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    @SuppressWarnings("java:S1312")
    @Override
    protected Logger log() { return Logger.getLogger(getClass().getName()); }

    @Override
    protected void goToHome() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_HOME, null);
        }
    }

    @Override
    protected EsitoOperazioneBean applicaSanzione(DatiPenalitaBean dati) {
        return logicController().applicaSanzione(dati);
    }

    public void richiediListaUtenti() {
        try {
            List<String> utenti = logicController().listaUtentiPerPenalita();

            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_PENALITA,
                    Map.of(GraphicControllerUtils.KEY_UTENTI, utenti));
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore recupero lista utenti", e);
        }
    }

    private LogicControllerApplicaPenalita logicController() {
        return new LogicControllerApplicaPenalita();
    }

}
