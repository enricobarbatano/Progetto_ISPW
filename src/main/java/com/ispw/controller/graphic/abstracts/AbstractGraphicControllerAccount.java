package com.ispw.controller.graphic.abstracts;

import java.util.Map;
import java.util.logging.Logger;

import com.ispw.bean.DatiAccountBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.interfaces.GraphicControllerAccount;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.logic.LogicControllerFactory;
import com.ispw.controller.logic.interfaces.CtrlGestioneAccount;

/**
 * Controller grafico astratto per gestione account.
 *
 * RESPONSABILITÀ:
 * - riceve input grezzi dalla view
 * - costruisce bean
 * - chiama logic controller
 * - gestisce navigazione
 */
public abstract class AbstractGraphicControllerAccount implements GraphicControllerAccount {

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicControllerAccount(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    // logger delegato alle classi concrete (GUI / CLI)
    protected abstract Logger log();

    protected abstract void goToLogin();
    protected abstract void goToHome(SessioneUtenteBean sessione);

    protected CtrlGestioneAccount logicController() {
        return LogicControllerFactory.getGestioneAccountController();
    }

    // =========================================================
    // ROUTING
    // =========================================================

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_ACCOUNT;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        GraphicControllerUtils.handleOnShow(
                log(),
                params,
                GraphicControllerUtils.PREFIX_ACCOUNT
        );
    }

    // =========================================================
    // FIX IMPORTANTE ✅ (prima mancava → errore tuo)
    // =========================================================

    /**
     * Carica account:
     * NON va nella GUI/CLI → è logica comune → deve stare qui
     */
    @Override
    public void loadAccount(SessioneUtenteBean sessione) {

        if (sessione == null || sessione.getUtente() == null) {
            notifyError(GraphicControllerUtils.MSG_SESSIONE_NON_VALIDA);
            return;
        }

        navigator.goTo(
                GraphicControllerUtils.ROUTE_ACCOUNT,
                Map.of(GraphicControllerUtils.KEY_SESSIONE, sessione)
        );
    }

    // =========================================================
    // UPDATE ACCOUNT
    // =========================================================

    @Override
    public void aggiornaDatiAccount(int idUtente,
                                    String nome,
                                    String cognome,
                                    String email,
                                    SessioneUtenteBean sessione) {

        if (idUtente <= 0 || nome == null || cognome == null || email == null) {
            notifyError(GraphicControllerUtils.MSG_DATI_ACCOUNT_MANCANTI);
            return;
        }

        // ✅ bean creato QUI (corretto)
        DatiAccountBean bean = new DatiAccountBean();
        bean.setIdUtente(idUtente);
        bean.setNome(nome);
        bean.setCognome(cognome);
        bean.setEmail(email);

        EsitoOperazioneBean esito = logicController().aggiornaDatiAccount(bean);

        if (esito != null && esito.isSuccesso()) {
            navigator.goTo(
                    GraphicControllerUtils.ROUTE_ACCOUNT,
                    Map.of(GraphicControllerUtils.KEY_SUCCESSO, esito.getMessaggio())
            );
        } else {
            notifyError(GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
        }
    }

    @Override
    public void cambiaPassword(String vecchiaPassword,
                                 String nuovaPassword,
                                 SessioneUtenteBean sessione) {

        if (vecchiaPassword == null || nuovaPassword == null) {
            notifyError(GraphicControllerUtils.MSG_PASSWORD_NON_VALIDE);
            return;
        }

        EsitoOperazioneBean esito =
                logicController().cambiaPassword(vecchiaPassword, nuovaPassword, sessione);

        if (esito != null && esito.isSuccesso()) {
            navigator.goTo(
                    GraphicControllerUtils.ROUTE_ACCOUNT,
                    Map.of(GraphicControllerUtils.KEY_SUCCESSO, esito.getMessaggio())
            );
        } else {
            notifyError(GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
        }
    }

    @Override
    public void logout() {
        goToLogin();
    }

    @Override
    public void tornaAllaHome(SessioneUtenteBean sessione) {
        goToHome(sessione);
    }

    // =========================================================
    // HELPER
    // =========================================================

    private void notifyError(String message) {
        GraphicControllerUtils.notifyError(
                log(),
                navigator,
                GraphicControllerUtils.ROUTE_ACCOUNT,
                GraphicControllerUtils.PREFIX_ACCOUNT,
                message
        );
    }
}