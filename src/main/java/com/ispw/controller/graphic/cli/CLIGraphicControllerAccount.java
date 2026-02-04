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
        // TODO: implementare lifecycle (caricare dati account)
    }

    /**
     * Carica informazioni account dell'utente corrente.
     * View deve fornire SessioneUtenteBean.
     */
    @Override
    public void loadAccount() {
        // TODO: implementare se necessario
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
        
        LogicControllerGestioneAccount logicController = new LogicControllerGestioneAccount();
        EsitoOperazioneBean esito = logicController.aggiornaDatiAccount(bean);
        
        if (esito != null && esito.isSuccesso()) {
            // TODO: notificare View aggiornamento riuscito
        } else {
            // TODO: notificare View errore
        }
    }

    /**
     * Cambia password.
     */
    @Override
    public void cambiaPassword(String vecchiaPassword, String nuovaPassword) {
        if (vecchiaPassword == null || nuovaPassword == null) {
            return;
        }
        // TODO: implementare cambio password
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
