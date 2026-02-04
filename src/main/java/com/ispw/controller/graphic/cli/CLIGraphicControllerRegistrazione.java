package com.ispw.controller.graphic.cli;

import java.util.Map;

import com.ispw.bean.DatiRegistrazioneBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerRegistrazione;
import com.ispw.controller.logic.ctrl.LogicControllerRegistrazione;

/**
 * Adapter CLI per la registrazione.
 */
public class CLIGraphicControllerRegistrazione implements GraphicControllerRegistrazione {
    
    private final GraphicControllerNavigation navigator;
    
    public CLIGraphicControllerRegistrazione(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }
    
    @Override
    public String getRouteName() {
        return "registrazione";
    }

    @Override
    public void onShow(Map<String, Object> params) {
    }

    /**
     * Invia dati registrazione.
     * @param datiRegistrazione mappa con nome, cognome, email, password, ruolo
     */
    @Override
    public void inviaDatiRegistrazione(Map<String, Object> datiRegistrazione) {
        if (datiRegistrazione == null) {
            return;
        }
        
        DatiRegistrazioneBean bean = new DatiRegistrazioneBean();
        bean.setNome((String) datiRegistrazione.get("nome"));
        bean.setCognome((String) datiRegistrazione.get("cognome"));
        bean.setEmail((String) datiRegistrazione.get("email"));
        bean.setPassword((String) datiRegistrazione.get("password"));
        
        LogicControllerRegistrazione logicController = new LogicControllerRegistrazione();
        EsitoOperazioneBean esito = logicController.registraNuovoUtente(bean, null);
        
        if (esito != null && esito.isSuccesso()) {
            vaiAlLogin();
        } else {
            // TODO: notificare View errore registrazione
        }
    }

    @Override
    public void vaiAlLogin() {
        if (navigator != null) {
            navigator.goTo("login");
        }
    }
}
