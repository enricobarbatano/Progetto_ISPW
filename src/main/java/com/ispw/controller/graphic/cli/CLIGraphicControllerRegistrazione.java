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
    
    private LogicControllerRegistrazione logicController;
    private GraphicControllerNavigation navigator;
    
    public CLIGraphicControllerRegistrazione() {
    }
    
    public CLIGraphicControllerRegistrazione(
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

    /**
     * Invia dati registrazione.
     * @param datiRegistrazione mappa con nome, cognome, email, password, ruolo
     */
    @Override
    public void inviaDatiRegistrazione(Map<String, Object> datiRegistrazione) {
        if (datiRegistrazione == null) {
            return;
        }
        
        // Adatta Map → DatiRegistrazioneBean
        DatiRegistrazioneBean bean = new DatiRegistrazioneBean();
        bean.setNome((String) datiRegistrazione.get("nome"));
        bean.setEmail((String) datiRegistrazione.get("email"));
        bean.setPassword((String) datiRegistrazione.get("password"));
        // Nota: DatiRegistrazioneBean non ha setCognome() o setRuolo()
        
        // Delega a LogicController
        EsitoOperazioneBean esito = logicController.registraNuovoUtente(bean, null);
        
        if (esito != null && esito.isSuccesso()) {
            // Registrazione riuscita - naviga a login
            vaiAlLogin();
        } else {
            // Notifica View: errore registrazione
        }
    }

    @Override
    public void vaiAlLogin() {
        if (navigator != null) {
            navigator.goTo("login");
        }
    }
}
