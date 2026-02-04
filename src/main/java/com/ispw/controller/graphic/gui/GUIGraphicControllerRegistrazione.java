package com.ispw.controller.graphic.gui;

import java.util.Map;

import com.ispw.bean.DatiRegistrazioneBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerRegistrazione;
import com.ispw.controller.logic.ctrl.LogicControllerRegistrazione;

/**
 * Adapter GUI per la registrazione.
 */
public class GUIGraphicControllerRegistrazione implements GraphicControllerRegistrazione {
    
    private LogicControllerRegistrazione logicController;
    private GraphicControllerNavigation navigator;
    
    public GUIGraphicControllerRegistrazione() {
    }
    
    public GUIGraphicControllerRegistrazione(
        LogicControllerRegistrazione logicController,
        GraphicControllerNavigation navigator) {
        this.logicController = logicController;
        this.navigator = navigator;
    }
    
    public void setLogicController(LogicControllerRegistrazione controller) {
        this.logicController = controller;
    }
    
    @Override
    public String getRouteName() {
        return "registrazione";
    }

    @Override
    public void setNavigator(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    @Override
    public void onShow(Map<String, Object> params) {
    }

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
        
        EsitoOperazioneBean esito = logicController.registraNuovoUtente(bean, null);
        
        if (esito != null && esito.isSuccesso()) {
            vaiAlLogin();
        }
    }

    @Override
    public void vaiAlLogin() {
        if (navigator != null) {
            navigator.goTo("login", null);
        }
    }
}
