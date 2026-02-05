package com.ispw.controller.graphic.cli;

import java.util.logging.Logger;

import com.ispw.bean.EsitoDisdettaBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.RiepilogoPrenotazioneBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerDisdetta;
import com.ispw.controller.logic.ctrl.LogicControllerDisdettaPrenotazione;

public class CLIGraphicControllerDisdetta extends AbstractGraphicControllerDisdetta {
    
    public CLIGraphicControllerDisdetta(GraphicControllerNavigation navigator) {
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
    protected java.util.List<RiepilogoPrenotazioneBean> ottieniPrenotazioniCancellabili(SessioneUtenteBean sessione) {
        return new LogicControllerDisdettaPrenotazione().ottieniPrenotazioniCancellabili(sessione.getUtente());
    }

    @Override
    protected EsitoDisdettaBean anteprimaDisdetta(int idPrenotazione, SessioneUtenteBean sessione) {
        return new LogicControllerDisdettaPrenotazione().anteprimaDisdetta(idPrenotazione, sessione);
    }

    @Override
    protected EsitoOperazioneBean eseguiAnnullamento(int idPrenotazione, SessioneUtenteBean sessione) {
        return new LogicControllerDisdettaPrenotazione().eseguiAnnullamento(idPrenotazione, sessione);
    }

}
