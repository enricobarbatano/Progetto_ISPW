package com.ispw.controller.graphic.cli;

import java.util.Map;
import java.util.logging.Logger;

import com.ispw.bean.DatiRegistrazioneBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerRegistrazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.exception.registration.EmailAlreadyExistsException;
import com.ispw.exception.registration.InvalidEmailFormatException;
import com.ispw.exception.registration.PasswordTooShortException;
import com.ispw.exception.registration.RegistrationException;
import com.ispw.model.enums.Ruolo;

/**
 * CLI controller registrazione.
 *
 * Riceve dati grezzi, crea il bean e delega al logic controller.
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

        DatiRegistrazioneBean bean = buildRegistrazioneBean(nome, cognome, email, password);

        try {
            EsitoOperazioneBean esito = logicController().registraNuovoUtente(bean);

            if (esito != null && esito.isSuccesso()) {
                vaiAlLogin();
            }

        } catch (PasswordTooShortException | InvalidEmailFormatException | EmailAlreadyExistsException e) {
            notifyError(e.getMessage());

        } catch (RegistrationException e) {
            notifyError("Errore durante la registrazione");
        }
    }

    @Override
    protected void goToLogin() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_LOGIN);
        }
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