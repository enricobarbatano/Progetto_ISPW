package com.ispw.controller.graphic.gui;

import com.ispw.bean.DatiRegistrazioneBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerRegistrazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.model.enums.Ruolo;

/**
 * GUI controller registrazione
 */
public class GUIGraphicControllerRegistrazione extends AbstractGraphicControllerRegistrazione {

    public GUIGraphicControllerRegistrazione(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    /**
     * ✅ versione corretta senza Map
     */
    @Override
    public void inviaDatiRegistrazione(
            String nome,
            String cognome,
            String email,
            String password,
            Ruolo ruolo
    ) {

        DatiRegistrazioneBean bean =
                buildRegistrazioneBean(nome, cognome, email, password);

        EsitoOperazioneBean esito = registraNuovoUtente(bean);

        if (esito != null && esito.isSuccesso()) {
            vaiAlLogin();
        }
    }

    @Override
    protected void goToLogin() {
        navigator.goTo(GraphicControllerUtils.ROUTE_LOGIN, null);
    }
}
