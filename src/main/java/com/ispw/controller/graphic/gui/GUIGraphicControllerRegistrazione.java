package com.ispw.controller.graphic.gui;

import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerRegistrazione;
import com.ispw.controller.logic.ctrl.LogicControllerRegistrazione;

public class GUIGraphicControllerRegistrazione extends AbstractGraphicControllerRegistrazione {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: estende AbstractGraphicControllerRegistrazione e usa GraphicControllerNavigation.
    // A2) IO verso GUI/CLI: riceve Map, costruisce bean, naviga su esito.
    // A3) Logica delegata: usa LogicControllerRegistrazione.
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

    // SEZIONE LOGICA
    // Legenda metodi: nessun helper privato.
}
