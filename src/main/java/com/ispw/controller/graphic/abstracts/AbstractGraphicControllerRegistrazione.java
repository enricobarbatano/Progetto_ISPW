package com.ispw.controller.graphic.abstracts;

import java.util.Map;

import com.ispw.bean.DatiRegistrazioneBean;
import com.ispw.bean.UtenteBean;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerRegistrazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.logic.LogicControllerFactory;
import com.ispw.controller.logic.interfaces.CtrlRegistrazione;


/**
 * Controller grafico astratto del caso d'uso "Registrazione".
 *
 * Questa classe contiene la logica comune tra GUI e CLI:
 * - espone la route della registrazione;
 * - gestisce il ritorno alla schermata di login;
 * - costruisce il bean di registrazione;
 * - delega le operazioni al controller logico tramite interfaccia.
 *
 * Le classi concrete GUI e CLI gestiscono solo le differenze specifiche
 * del frontend.
 *
 * Nota di progetto:
 * il graphic controller non conosce l'implementazione concreta del logic controller.
 * Usa CtrlRegistrazione ottenuto tramite LogicControllerFactory.
 */
public abstract class AbstractGraphicControllerRegistrazione implements GraphicControllerRegistrazione {

    // =====================================================================
    // COLLABORATORI
    // =====================================================================
    // Il navigator è il router del layer grafico.
    // Permette al controller di spostarsi tra schermate senza conoscere
    // direttamente le view concrete.

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicControllerRegistrazione(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    protected abstract void goToLogin();

    // =====================================================================
    // LOGIC CONTROLLER
    // =====================================================================
    // Il controller logico viene recuperato tramite factory e restituito
    // tramite interfaccia. In questo modo il graphic controller non dipende
    // dalla classe concreta LogicControllerRegistrazione.

    protected CtrlRegistrazione logicController() {
        return LogicControllerFactory.getRegistrazioneController();
    }

    // =====================================================================
    // NAVIGAZIONE
    // =====================================================================

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_REGISTRAZIONE;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        // In questa schermata non è necessario gestire parametri in modo comune.
    }

    // STEP 1: ritorno al login

    /**
     * Torna alla schermata di login.
     *
     * Il comportamento concreto viene delegato alla classe GUI o CLI.
     */
    @Override
    public void vaiAlLogin() {
        goToLogin();
    }

    // =====================================================================
    // OPERAZIONI LOGICHE DELEGATE
    // =====================================================================

    
    /**
     * Conferma un nuovo account tramite controller logico.
     * 
     */

    protected void confermaNuovoAccount(UtenteBean utente) {
        logicController().confermaNuovoAccount(utente);
    }

    /**
     * Finalizza l'attivazione di un account tramite controller logico.
     */
    protected void finalizzaAttivazioneAccount(int idUtente) {
        logicController().finalizzaAttivazioneAccount(idUtente);
    }

    // =====================================================================
    // MAPPING
    // =====================================================================

    /**
     * Costruisce il bean di registrazione partendo dai dati raccolti dalla view.
     */
    protected DatiRegistrazioneBean buildRegistrazioneBean(String nome,
                                                           String cognome,
                                                           String email,
                                                           String password) {
        DatiRegistrazioneBean bean = new DatiRegistrazioneBean();
        bean.setNome(nome);
        bean.setCognome(cognome);
        bean.setEmail(email);
        bean.setPassword(password);
        return bean;
    }
}