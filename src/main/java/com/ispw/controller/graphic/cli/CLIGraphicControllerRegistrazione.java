package com.ispw.controller.graphic.cli;

import java.util.Map;
import java.util.logging.Logger;

import com.ispw.bean.DatiRegistrazioneBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerRegistrazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

/**
 * Controller grafico CLI del caso d'uso "Registrazione".
 *
 * Questa classe contiene la parte specifica CLI:
 * - lettura dei dati da mappa;
 * - validazione minima dei campi obbligatori;
 * - gestione della navigazione in base all'esito.
 *
 * La registrazione vera e propria viene delegata al controller logico
 * tramite la classe astratta e la LogicControllerFactory.
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
     * Invia i dati di registrazione al caso d'uso.
     *
     * Il metodo:
     * - controlla che la mappa sia presente;
     * - estrae i campi necessari;
     * - valida i campi obbligatori;
     * - costruisce il bean;
     * - delega la registrazione alla classe astratta;
     * - naviga verso login in caso di successo.
     */
    @Override
    public void inviaDatiRegistrazione(Map<String, Object> datiRegistrazione) {
        if (datiRegistrazione == null) {
            notifyRegistrazioneError(GraphicControllerUtils.MSG_DATI_REGISTRAZIONE_MANCANTI);
            return;
        }

        String nome = safeTrim(datiRegistrazione.get(GraphicControllerUtils.KEY_NOME));
        String cognome = safeTrim(datiRegistrazione.get(GraphicControllerUtils.KEY_COGNOME));
        String email = safeTrim(datiRegistrazione.get(GraphicControllerUtils.KEY_EMAIL));
        String password = safeTrim(datiRegistrazione.get(GraphicControllerUtils.KEY_PASSWORD));

        if (!hasText(nome) || !hasText(cognome) || !hasText(email) || !hasText(password)) {
            notifyRegistrazioneError(GraphicControllerUtils.MSG_CAMPI_OBBLIGATORI_MANCANTI);
            return;
        }

        DatiRegistrazioneBean bean = buildRegistrazioneBean(nome, cognome, email, password);

        EsitoOperazioneBean esito = registraNuovoUtente(bean);

        if (esito != null && esito.isSuccesso()) {
            vaiAlLogin();
            return;
        }

        notifyRegistrazioneError(esito != null
                ? esito.getMessaggio()
                : GraphicControllerUtils.MSG_REGISTRAZIONE_NON_RIUSCITA);
    }

    /**
     * Torna alla schermata di login.
     */
    @Override
    protected void goToLogin() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_LOGIN);
        }
    }

    // =====================================================================
    // HELPERS REGISTRAZIONE CLI
    // =====================================================================

    /**
     * Notifica un errore relativo alla registrazione.
     */
    private void notifyRegistrazioneError(String message) {
        GraphicControllerUtils.notifyError(
                LOGGER,
                navigator,
                GraphicControllerUtils.ROUTE_REGISTRAZIONE,
                GraphicControllerUtils.PREFIX_REGISTRAZIONE,
                message
        );
    }

    /**
     * Converte un valore generico in stringa trimmed.
     */
    private String safeTrim(Object value) {
        if (value == null) {
            return null;
        }

        return value.toString().trim();
    }

    /**
     * Controlla che una stringa contenga almeno un carattere non spazio.
     */
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
