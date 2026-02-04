package com.ispw.controller.graphic.cli;

import java.util.Map;

import com.ispw.bean.DatiAccountBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.controller.graphic.GraphicControllerAccount;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.logic.ctrl.LogicControllerGestioneAccount;

/**
 * Adapter CLI per la gestione account.
 * 
 * Responsabilità:
 * - Adatta form account → Bean → LogicController
 * - Riceve DatiAccountBean, EsitoOperazioneBean dal LogicController
 * - Gestisce navigazione
 */
public class CLIGraphicControllerAccount implements GraphicControllerAccount {
    
    // ==================== Dependencies ====================
    private final GraphicControllerNavigation navigator;
    
    // ==================== Constructors ====================
    public CLIGraphicControllerAccount(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }
    
    
    @Override
    public String getRouteName() {
        return "account";
    }

    @Override
    public void onShow(Map<String, Object> params) {
        // Lifecycle: carica dati account quando view è mostrata
    }

    /**
     * Carica informazioni account dell'utente corrente.
     * View deve fornire SessioneUtenteBean.
     */
    @Override
    public void loadAccount() {
        // La View deve fornire sessioneCorrente
        // SessioneUtenteBean sessione = view.getSessioneCorrente();
        // DatiAccountBean dati = logicController.recuperaInformazioniAccount(sessione);
        // view.mostraDatiAccount(dati);
    }

    /**
     * Aggiorna dati account.
     * @param nuoviDati mappa con campi modificati (nome, email, etc.)
     */
    @Override
    public void aggiornaDatiAccount(Map<String, Object> nuoviDati) {
        if (nuoviDati == null) {
            return;
        }
        
        // Adatta Map → DatiAccountBean
        DatiAccountBean bean = new DatiAccountBean();
        if (nuoviDati.containsKey("nome")) {
            bean.setNome((String) nuoviDati.get("nome"));
        }
        if (nuoviDati.containsKey("cognome")) {
            bean.setCognome((String) nuoviDati.get("cognome"));
        }
        if (nuoviDati.containsKey("email")) {
            bean.setEmail((String) nuoviDati.get("email"));
        }
        
        // Delega a LogicController (creato on-demand)
        LogicControllerGestioneAccount logicController = new LogicControllerGestioneAccount();
        EsitoOperazioneBean esito = logicController.aggiornaDatiAccount(bean);
        
        if (esito != null && esito.isSuccesso()) {
            // Notifica View: aggiornamento riuscito
        } else {
            // Notifica View: errore
        }
    }

    /**
     * Cambia password.
     */
    @Override
    public void cambiaPassword(String vecchiaPassword, String nuovaPassword) {
        if (vecchiaPassword == null || nuovaPassword == null) {
            // Valida input
        }
        
        // La View deve fornire sessioneCorrente
        // SessioneUtenteBean sessione = view.getSessioneCorrente();
        // EsitoOperazioneBean esito = 
        //     logicController.cambiaPassword(vecchiaPassword, nuovaPassword, sessione);
        
        // if (esito.isSuccesso()) { logout(); }
    }

    /**
     * Logout - naviga a login.
     */
    @Override
    public void logout() {
        if (navigator != null) {
            navigator.goTo("login");
        }
    }
}
