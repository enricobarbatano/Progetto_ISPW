package com.ispw.controller.graphic.gui;

import java.util.logging.Logger;

import com.ispw.bean.DatiAccountBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerAccount;
import com.ispw.controller.logic.ctrl.LogicControllerGestioneAccount;

public class GUIGraphicControllerAccount extends AbstractGraphicControllerAccount {

    public GUIGraphicControllerAccount(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    @Override
    protected Logger log() { return Logger.getLogger(getClass().getName()); }

    @Override
    protected void goToLogin() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_LOGIN, null);
        }
    }

    @Override
    protected DatiAccountBean recuperaInformazioniAccount(SessioneUtenteBean sessione) {
        return new LogicControllerGestioneAccount().recuperaInformazioniAccount(sessione);
    }

    @Override
    protected EsitoOperazioneBean aggiornaDatiAccountConNotifica(DatiAccountBean bean) {
        return new LogicControllerGestioneAccount().aggiornaDatiAccountConNotifica(bean);
    }

    @Override
    protected EsitoOperazioneBean cambiaPasswordConNotifica(String vecchiaPassword, String nuovaPassword,
                                                            SessioneUtenteBean sessione) {
        return new LogicControllerGestioneAccount().cambiaPasswordConNotifica(vecchiaPassword, nuovaPassword, sessione);
    }

}
