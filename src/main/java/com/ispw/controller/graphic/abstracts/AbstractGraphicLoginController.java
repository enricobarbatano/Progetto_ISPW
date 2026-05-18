package com.ispw.controller.graphic.abstracts;

import java.util.Map;
import java.util.logging.Logger;

import com.ispw.bean.DatiLoginBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.GraphicLoginController;
import com.ispw.controller.logic.LogicControllerFactory;
import com.ispw.controller.logic.interfaces.CtrlAccesso;

/**
 * Controller grafico astratto del caso d'uso "Login".
 *
 * Questa classe contiene la logica comune tra GUI e CLI:
 * - controlla le credenziali ricevute dalla view;
 * - chiama il controller logico di accesso;
 * - salva il log di accesso;
 * - naviga verso home, login o registrazione.
 *
 * Le classi concrete GUI e CLI gestiscono solo le differenze specifiche
 * del frontend, come la visualizzazione degli errori.
 *
 * Nota di progetto:
 * il graphic controller non conosce l'implementazione concreta del logic controller.
 * Usa CtrlAccesso ottenuto tramite LogicControllerFactory.
 */
public abstract class AbstractGraphicLoginController implements GraphicLoginController {

    // =====================================================================
    // COLLABORATORI
    // =====================================================================

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicLoginController(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    protected abstract Logger log();

    protected abstract void goToLoginWithError(String message);

    protected abstract void goToLogin();

    protected abstract void goToRegistrazione();

    protected abstract void goToHome(SessioneUtenteBean sessione);

    // =====================================================================
    // LOGIC CONTROLLER
    // =====================================================================

    protected CtrlAccesso logicController() {
        return LogicControllerFactory.getAccessoController();
    }

    /*
     * Hook protetti mantenuti per compatibilità con le classi concrete.
     * Di default delegano al controller logico ottenuto tramite factory.
     */

    protected SessioneUtenteBean verificaCredenziali(DatiLoginBean credenziali) {
        return logicController().verificaCredenziali(credenziali);
    }

    protected void salvaLog(SessioneUtenteBean sessione) {
        logicController().saveLog(sessione);
    }

    // =====================================================================
    // NAVIGAZIONE
    // =====================================================================

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_LOGIN;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        // In questa schermata non è necessario gestire parametri in modo comune.
    }

    // STEP 1: login

    /**
     * Effettua il login.
     *
     * Il metodo:
     * - controlla che le credenziali siano presenti;
     * - chiama il controller logico;
     * - se il login riesce, salva il log e va alla home;
     * - se fallisce, torna alla schermata login con errore.
     */
    @Override
    public void effettuaLogin(DatiLoginBean credenziali) {
        if (credenziali == null) {
            goToLoginWithError(GraphicControllerUtils.MSG_CREDENZIALI_MANCANTI);
            return;
        }

        try {
            SessioneUtenteBean sessione = verificaCredenziali(credenziali);

            if (sessione != null) {
                salvaLog(sessione);
                goToHome(sessione);
            } else {
                goToLoginWithError(GraphicControllerUtils.MSG_CREDENZIALI_NON_VALIDE);
            }
        } catch (IllegalStateException ex) {
            goToLoginWithError(ex.getMessage());
        }
    }

    // STEP 2: logout

    /**
     * Esegue il logout tornando alla schermata di login.
     */
    @Override
    public void logout() {
        goToLogin();
    }

    // STEP 3: registrazione

    /**
     * Naviga verso la schermata di registrazione.
     */
    @Override
    public void vaiARegistrazione() {
        goToRegistrazione();
    }

    // STEP 4: home

    /**
     * Naviga verso la home senza una sessione specifica.
     */
    @Override
    public void vaiAHome() {
        goToHome(null);
    }

    // =====================================================================
    // ADAPTER
    // =====================================================================

    /**
     * Adapter per input grezzi del login.
     *
     * Costruisce DatiLoginBean e delega al metodo principale.
     */
    public void effettuaLoginRaw(String email, String password) {
        if (email == null && password == null) {
            effettuaLogin(null);
            return;
        }

        effettuaLogin(new DatiLoginBean(email, password));
    }
}