package com.ispw.controller.graphic.cli;

import java.util.Map;
import java.util.logging.Logger;

import com.ispw.bean.DatiRegistrazioneBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerRegistrazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.model.enums.Ruolo;

/**
 * CLI controller registrazione
 *
 * ✅ riceve dati grezzi (NON Map)
 * ✅ crea bean nel controller
 */
public class CLIGraphicControllerRegistrazione extends AbstractGraphicControllerRegistrazione {

    private static final Logger LOGGER =
            Logger.getLogger(CLIGraphicControllerRegistrazione.class.getName());

    public CLIGraphicControllerRegistrazione(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    @Override
    public void onShow(Map<String, Object> params) {
        GraphicControllerUtils.handleOnShow(
                LOGGER,
                params,
                GraphicControllerUtils.PREFIX_REGISTRAZIONE
        );
    }

    /**
     * ✅ VERSIONE CORRETTA (senza Map)
     */
    @Override
    public void inviaDatiRegistrazione(
            String nome,
            String cognome,
            String email,
            String password,
            Ruolo ruolo
    ) {

        if (!hasText(nome) || !hasText(cognome) || !hasText(email) || !hasText(password)) {
            notifyError(GraphicControllerUtils.MSG_CAMPI_OBBLIGATORI_MANCANTI);
            return;
        }

        DatiRegistrazioneBean bean =
                buildRegistrazioneBean(nome, cognome, email, password);

        EsitoOperazioneBean esito = registraNuovoUtente(bean);

        if (esito != null && esito.isSuccesso()) {
            vaiAlLogin();
        } else {
            notifyError(GraphicControllerUtils.MSG_REGISTRAZIONE_NON_RIUSCITA);
        }
    }

    @Override
    protected void goToLogin() {
        navigator.goTo(GraphicControllerUtils.ROUTE_LOGIN);
    }

    private void notifyError(String message) {
        GraphicControllerUtils.notifyError(
                LOGGER,
                navigator,
                GraphicControllerUtils.ROUTE_REGISTRAZIONE,
                GraphicControllerUtils.PREFIX_REGISTRAZIONE,
                message
        );
    }

    private boolean hasText(String v) {
        return v != null && !v.isBlank();
    }
}
