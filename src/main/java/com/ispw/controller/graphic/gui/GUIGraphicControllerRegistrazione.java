package com.ispw.controller.graphic.gui;

import java.util.Map;

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
 * GUI controller registrazione.
 */
public class GUIGraphicControllerRegistrazione extends AbstractGraphicControllerRegistrazione {

    public GUIGraphicControllerRegistrazione(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    /**
     * Riceve i dati grezzi dalla view GUI, costruisce il bean e delega
     * la registrazione al logic controller.
     */
    @Override
    public void inviaDatiRegistrazione(
            String nome,
            String cognome,
            String email,
            String password,
            Ruolo ruolo
    ) {
        DatiRegistrazioneBean bean = buildRegistrazioneBean(nome, cognome, email, password);

        try {
            EsitoOperazioneBean esito = logicController().registraNuovoUtente(bean);

            if (esito != null && esito.isSuccesso()) {
                vaiAlLogin();
            }

        } catch (PasswordTooShortException | InvalidEmailFormatException | EmailAlreadyExistsException e) {
            showError(e.getMessage());

        } catch (RegistrationException e) {
            showError("Errore durante la registrazione");
        }
    }

    @Override
    protected void goToLogin() {
        navigator.goTo(GraphicControllerUtils.ROUTE_LOGIN, null);
    }

    private void showError(String message) {
        navigator.goTo(
                GraphicControllerUtils.ROUTE_REGISTRAZIONE,
                Map.of(GraphicControllerUtils.KEY_ERROR, message)
        );
    }
}