package com.ispw.controller.graphic.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.DatiAccountBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.GraphicControllerAccount;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
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
    
    @SuppressWarnings("java:S1312")
    private Logger log() { return Logger.getLogger(getClass().getName()); }

    // ==================== Dependencies ====================
    private final GraphicControllerNavigation navigator;
    
    // ==================== Constructors ====================
    public CLIGraphicControllerAccount(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }
    
    
    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_ACCOUNT;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        GraphicControllerUtils.handleOnShow(log(), params, GraphicControllerUtils.PREFIX_ACCOUNT);
    }

    /**
     * Carica informazioni account dell'utente corrente.
     * View deve fornire SessioneUtenteBean.
     */
    @Override
    public void loadAccount(SessioneUtenteBean sessione) {
        if (sessione == null || sessione.getUtente() == null) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_ACCOUNT,
                    GraphicControllerUtils.PREFIX_ACCOUNT, "Sessione non valida");
            return;
        }

        try {
            LogicControllerGestioneAccount logicController = new LogicControllerGestioneAccount();
            DatiAccountBean dati = logicController.recuperaInformazioniAccount(sessione);

            if (dati == null) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_ACCOUNT,
                    GraphicControllerUtils.PREFIX_ACCOUNT, "Impossibile recuperare dati account");
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("idUtente", dati.getIdUtente());
            payload.put("nome", dati.getNome());
            payload.put("cognome", dati.getCognome());
            payload.put("email", dati.getEmail());

            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_ACCOUNT, Map.of("datiAccount", payload));
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore caricamento account", e);
        }
    }

    /**
     * Aggiorna dati account.
     * @param nuoviDati mappa con campi modificati (nome, email, etc.)
     */
    @Override
    public void aggiornaDatiAccount(Map<String, Object> nuoviDati) {
        if (nuoviDati == null) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_ACCOUNT,
                    GraphicControllerUtils.PREFIX_ACCOUNT, "Dati account mancanti");
            return;
        }

        Object idUtente = nuoviDati.get("idUtente");
        if (!(idUtente instanceof Integer) || ((Integer) idUtente) <= 0) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_ACCOUNT,
                    GraphicControllerUtils.PREFIX_ACCOUNT, "Id utente non valido");
            return;
        }
        
        DatiAccountBean bean = new DatiAccountBean();
        bean.setIdUtente((Integer) idUtente);
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
        EsitoOperazioneBean esito = logicController.aggiornaDatiAccountConNotifica(bean);
        
        if (esito != null && esito.isSuccesso()) {
            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_ACCOUNT,
                        Map.of(GraphicControllerUtils.KEY_SUCCESSO, esito.getMessaggio()));
            }
        } else {
            GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_ACCOUNT,
                    GraphicControllerUtils.PREFIX_ACCOUNT,
                    esito != null ? esito.getMessaggio() : "Operazione non riuscita");
        }
    }

    /**
     * Cambia password.
     */
    @Override
    public void cambiaPassword(String vecchiaPassword, String nuovaPassword, SessioneUtenteBean sessione) {
        if (vecchiaPassword == null || nuovaPassword == null) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_ACCOUNT,
                    GraphicControllerUtils.PREFIX_ACCOUNT, "Password non valide");
            return;
        }
        if (sessione == null || sessione.getUtente() == null) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_ACCOUNT,
                    GraphicControllerUtils.PREFIX_ACCOUNT, "Sessione non valida");
            return;
        }

        LogicControllerGestioneAccount logicController = new LogicControllerGestioneAccount();
        EsitoOperazioneBean esito = logicController.cambiaPasswordConNotifica(vecchiaPassword, nuovaPassword, sessione);

        if (esito != null && esito.isSuccesso()) {
            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_ACCOUNT,
                        Map.of(GraphicControllerUtils.KEY_SUCCESSO, esito.getMessaggio()));
            }
        } else {
            GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_ACCOUNT,
                    GraphicControllerUtils.PREFIX_ACCOUNT,
                    esito != null ? esito.getMessaggio() : "Operazione non riuscita");
        }
    }

    /**
     * Logout - naviga a login.
     */
    @Override
    public void logout() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_LOGIN);
        }
    }

}
