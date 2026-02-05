package com.ispw.controller.graphic.cli;

import java.util.List;
import java.util.logging.Logger;

import com.ispw.bean.DatiPenalitaBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerPenalita;
import com.ispw.controller.logic.ctrl.LogicControllerApplicaPenalita;

public class CLIGraphicControllerPenalita extends AbstractGraphicControllerPenalita {
    
    public CLIGraphicControllerPenalita(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    @SuppressWarnings("java:S1312")
    @Override
    protected Logger log() { return Logger.getLogger(getClass().getName()); }

    @Override
    protected void goToHome() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_HOME);
        }
    }

    @Override
    protected EsitoOperazioneBean applicaSanzione(DatiPenalitaBean dati) {
        return logicController().applicaSanzione(dati);
    }

    @Override
    protected List<String> listaUtentiPerPenalita() {
        return logicController().listaUtentiPerPenalita();
    }

    private LogicControllerApplicaPenalita logicController() {
        return new LogicControllerApplicaPenalita();
    }

}
