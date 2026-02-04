package com.ispw.controller.graphic.gui;

import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerRegistrazione;
import com.ispw.controller.logic.ctrl.LogicControllerRegistrazione;

/**
 * Adapter GUI per la registrazione.
 */
public class GUIGraphicControllerRegistrazione extends AbstractGraphicControllerRegistrazione {
    
    public GUIGraphicControllerRegistrazione(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    @Override
    public void inviaDatiRegistrazione(Map<String, Object> datiRegistrazione) {
        if (datiRegistrazione == null) {
            return;
        }

        var bean = buildRegistrazioneBean(
            (String) datiRegistrazione.get(GraphicControllerUtils.KEY_NOME),
            (String) datiRegistrazione.get(GraphicControllerUtils.KEY_COGNOME),
            (String) datiRegistrazione.get(GraphicControllerUtils.KEY_EMAIL),
            (String) datiRegistrazione.get(GraphicControllerUtils.KEY_PASSWORD));
        
        LogicControllerRegistrazione logicController = new LogicControllerRegistrazione();
        var esito = logicController.registraNuovoUtente(bean);
        
        if (esito != null && esito.isSuccesso()) {
            vaiAlLogin();
        }
    }

    @Override
    protected void goToLogin() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_LOGIN, null);
        }
    }
}
