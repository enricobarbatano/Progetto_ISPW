package com.ispw.controller.graphic.cli;

import java.util.Map;
import java.util.logging.Logger;

import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerRegistrazione;
import com.ispw.controller.logic.ctrl.LogicControllerRegistrazione;

public class CLIGraphicControllerRegistrazione extends AbstractGraphicControllerRegistrazione {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: estende AbstractGraphicControllerRegistrazione e usa GraphicControllerNavigation.
    // A2) IO verso GUI/CLI: riceve Map, costruisce bean, naviga su esito.
    // A3) Logica delegata: usa LogicControllerRegistrazione.
    // ========================

    private static final Logger LOGGER = Logger.getLogger(CLIGraphicControllerRegistrazione.class.getName());
    public CLIGraphicControllerRegistrazione(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    @Override
    public void onShow(Map<String, Object> params) {
        GraphicControllerUtils.handleOnShow(LOGGER, params, GraphicControllerUtils.PREFIX_REGISTRAZIONE);
    }

    @Override
    public void inviaDatiRegistrazione(Map<String, Object> datiRegistrazione) {
        if (datiRegistrazione == null) {
            GraphicControllerUtils.notifyError(LOGGER, navigator, GraphicControllerUtils.ROUTE_REGISTRAZIONE,
                GraphicControllerUtils.PREFIX_REGISTRAZIONE,
                GraphicControllerUtils.MSG_DATI_REGISTRAZIONE_MANCANTI);
            return;
        }
        
        String nome = safeTrim(datiRegistrazione.get(GraphicControllerUtils.KEY_NOME));
        String cognome = safeTrim(datiRegistrazione.get(GraphicControllerUtils.KEY_COGNOME));
        String email = safeTrim(datiRegistrazione.get(GraphicControllerUtils.KEY_EMAIL));
        String password = safeTrim(datiRegistrazione.get(GraphicControllerUtils.KEY_PASSWORD));

        if (!hasText(nome) || !hasText(cognome) || !hasText(email) || !hasText(password)) {
            GraphicControllerUtils.notifyError(LOGGER, navigator, GraphicControllerUtils.ROUTE_REGISTRAZIONE,
                GraphicControllerUtils.PREFIX_REGISTRAZIONE,
                GraphicControllerUtils.MSG_CAMPI_OBBLIGATORI_MANCANTI);
            return;
        }

        var bean = buildRegistrazioneBean(nome, cognome, email, password);
        
        LogicControllerRegistrazione logicController = new LogicControllerRegistrazione();
        var esito = logicController.registraNuovoUtente(bean);
        
        if (esito != null && esito.isSuccesso()) {
            vaiAlLogin();
        } else {
            GraphicControllerUtils.notifyError(LOGGER, navigator, GraphicControllerUtils.ROUTE_REGISTRAZIONE,
                GraphicControllerUtils.PREFIX_REGISTRAZIONE,
                esito != null ? esito.getMessaggio() : GraphicControllerUtils.MSG_REGISTRAZIONE_NON_RIUSCITA);
        }
    }

    @Override
    protected void goToLogin() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_LOGIN);
        }
    }

    // ========================
    // SEZIONE LOGICA
    // Legenda metodi:
    // 1) safeTrim(...) - normalizza input string.
    // 2) hasText(...) - verifica stringhe.
    // ========================
    private String safeTrim(Object value) {
        return value == null ? null : value.toString().trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
