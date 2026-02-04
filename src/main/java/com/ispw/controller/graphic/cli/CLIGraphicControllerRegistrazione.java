package com.ispw.controller.graphic.cli;

import java.util.Map;
import java.util.logging.Logger;

import com.ispw.bean.DatiRegistrazioneBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerRegistrazione;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.logic.ctrl.LogicControllerRegistrazione;

/**
 * Adapter CLI per la registrazione.
 */
public class CLIGraphicControllerRegistrazione implements GraphicControllerRegistrazione {
    
    private static final Logger LOGGER = Logger.getLogger(CLIGraphicControllerRegistrazione.class.getName());
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
        GraphicControllerUtils.handleOnShow(LOGGER, params, "[REGISTRAZIONE]");
    }

    /**
     * Invia dati registrazione.
     * @param datiRegistrazione mappa con nome, cognome, email, password, ruolo
     */
    @Override
    public void inviaDatiRegistrazione(Map<String, Object> datiRegistrazione) {
        if (datiRegistrazione == null) {
                GraphicControllerUtils.notifyError(LOGGER, navigator, "registrazione", "[REGISTRAZIONE]",
                    "Dati registrazione mancanti");
            return;
        }
        
        String nome = safeTrim(datiRegistrazione.get("nome"));
        String cognome = safeTrim(datiRegistrazione.get("cognome"));
        String email = safeTrim(datiRegistrazione.get("email"));
        String password = safeTrim(datiRegistrazione.get("password"));

        if (!hasText(nome) || !hasText(cognome) || !hasText(email) || !hasText(password)) {
                GraphicControllerUtils.notifyError(LOGGER, navigator, "registrazione", "[REGISTRAZIONE]",
                    "Compila tutti i campi obbligatori");
            return;
        }

        DatiRegistrazioneBean bean = new DatiRegistrazioneBean();
        bean.setNome(nome);
        bean.setCognome(cognome);
        bean.setEmail(email);
        bean.setPassword(password);
        
        LogicControllerRegistrazione logicController = new LogicControllerRegistrazione();
        EsitoOperazioneBean esito = logicController.registraNuovoUtente(bean);
        
        if (esito != null && esito.isSuccesso()) {
            vaiAlLogin();
        } else {
            GraphicControllerUtils.notifyError(LOGGER, navigator, "registrazione", "[REGISTRAZIONE]",
                    esito != null ? esito.getMessaggio() : "Registrazione non riuscita");
        }
    }

    @Override
    public void vaiAlLogin() {
        if (navigator != null) {
            navigator.goTo("login");
        }
    }

    private String safeTrim(Object value) {
        return value == null ? null : value.toString().trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
