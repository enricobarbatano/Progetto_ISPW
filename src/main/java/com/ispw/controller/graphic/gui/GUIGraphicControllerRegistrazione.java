package com.ispw.controller.graphic.gui;

import java.util.Map;

import com.ispw.bean.DatiRegistrazioneBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerRegistrazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

/**
 * Controller grafico GUI del caso d'uso "Registrazione".
 *
 * Questa classe contiene la parte specifica GUI:
 * - lettura dei dati dalla mappa ricevuta dalla view;
 * - costruzione del bean di registrazione;
 * - navigazione verso login in caso di registrazione riuscita.
 *
 * La registrazione vera e propria viene delegata al controller logico
 * tramite la classe astratta e la LogicControllerFactory.
 */
public class GUIGraphicControllerRegistrazione extends AbstractGraphicControllerRegistrazione {

    public GUIGraphicControllerRegistrazione(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    /**
     * Invia i dati di registrazione al caso d'uso.
     *
     * Il metodo mantiene la logica già presente:
     * se i dati sono nulli termina senza navigare;
     * se la registrazione va a buon fine torna al login.
     */
    @Override
    public void inviaDatiRegistrazione(Map<String, Object> datiRegistrazione) {
        if (datiRegistrazione == null) {
            return;
        }

        DatiRegistrazioneBean bean = buildRegistrazioneBean(
                (String) datiRegistrazione.get(GraphicControllerUtils.KEY_NOME),
                (String) datiRegistrazione.get(GraphicControllerUtils.KEY_COGNOME),
                (String) datiRegistrazione.get(GraphicControllerUtils.KEY_EMAIL),
                (String) datiRegistrazione.get(GraphicControllerUtils.KEY_PASSWORD)
        );

        EsitoOperazioneBean esito = registraNuovoUtente(bean);

        if (esito != null && esito.isSuccesso()) {
            vaiAlLogin();
        }
    }

    /**
     * Torna alla schermata di login.
     */
    @Override
    protected void goToLogin() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_LOGIN, null);
        }
    }
}