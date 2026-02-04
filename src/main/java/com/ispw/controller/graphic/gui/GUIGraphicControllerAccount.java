package com.ispw.controller.graphic.gui;

import java.util.Map;

import com.ispw.bean.DatiAccountBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.controller.graphic.GraphicControllerAccount;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.logic.ctrl.LogicControllerGestioneAccount;

/**
 * Adapter GUI per la gestione account.
 */
public class GUIGraphicControllerAccount implements GraphicControllerAccount {
    
    private final GraphicControllerNavigation navigator;
    
    public GUIGraphicControllerAccount(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }
    
    @Override
    public String getRouteName() {
        return "account";
    }

    @Override
    public void onShow(Map<String, Object> params) {
    }

    @Override
    public void loadAccount() {
    }

    @Override
    public void aggiornaDatiAccount(Map<String, Object> nuoviDati) {
        if (nuoviDati == null) {
            return;
        }
        
        DatiAccountBean bean = new DatiAccountBean();
        if (nuoviDati.containsKey("nome")) {
            bean.setNome((String) nuoviDati.get("nome"));
        }
        if (nuoviDati.containsKey("cognome")) {
            bean.setCognome((String) nuoviDati.get("cognome"));
        }
        if (nuoviDati.containsKey("email")) {
            bean.setEmail((String) nuoviDati.get("email"));
        }
        
        // Delega a LogicController (creato on-demand)
        LogicControllerGestioneAccount logicController = new LogicControllerGestioneAccount();
        EsitoOperazioneBean esito = logicController.aggiornaDatiAccount(bean);
        
        if (esito != null && esito.isSuccesso()) {
            // TODO: mostrare dialog successo
        }
    }

    @Override
    public void cambiaPassword(String vecchiaPassword, String nuovaPassword) {
        if (vecchiaPassword == null || nuovaPassword == null) {
            // TODO: validare input GUI
        }
    }

    @Override
    public void logout() {
        if (navigator != null) {
            navigator.goTo("login", null);
        }
    }
}
