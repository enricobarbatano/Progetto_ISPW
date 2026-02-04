package com.ispw.controller.graphic.abstracts;

import java.util.Map;

import com.ispw.bean.DatiRegistrazioneBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerRegistrazione;
import com.ispw.controller.graphic.GraphicControllerUtils;

/**
 * Classe astratta che centralizza la logica comune dei controller grafici Registrazione
 * (CLI/GUI) per ridurre duplicazione. Non introduce nuove responsabilità né
 * modifica il disaccoppiamento: delega invariata ai LogicController e mantiene
 * la stessa navigazione verso la View tramite GraphicControllerNavigation.
 */
public abstract class AbstractGraphicControllerRegistrazione implements GraphicControllerRegistrazione {

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicControllerRegistrazione(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    protected abstract void goToLogin();

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_REGISTRAZIONE;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        // lifecycle hook (override if needed)
    }

    @Override
    public void vaiAlLogin() {
        goToLogin();
    }

    protected DatiRegistrazioneBean buildRegistrazioneBean(String nome, String cognome, String email, String password) {
        DatiRegistrazioneBean bean = new DatiRegistrazioneBean();
        bean.setNome(nome);
        bean.setCognome(cognome);
        bean.setEmail(email);
        bean.setPassword(password);
        return bean;
    }
}
