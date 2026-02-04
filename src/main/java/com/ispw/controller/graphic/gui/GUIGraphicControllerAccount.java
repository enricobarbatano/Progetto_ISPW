package com.ispw.controller.graphic.gui;

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
 * Adapter GUI per la gestione account.
 */
public class GUIGraphicControllerAccount implements GraphicControllerAccount {
    
    @SuppressWarnings("java:S1312")
    private Logger log() { return Logger.getLogger(getClass().getName()); }

    private final GraphicControllerNavigation navigator;
    
    public GUIGraphicControllerAccount(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }
    
    @Override
    public String getRouteName() {
        return "account";
    }

    @Override
    public void onShow(Map<String, Object> params) {
        GraphicControllerUtils.handleOnShow(log(), params, "[ACCOUNT]");
    }

    @Override
    public void loadAccount(SessioneUtenteBean sessione) {
        if (sessione == null || sessione.getUtente() == null) {
            GraphicControllerUtils.notifyError(log(), navigator, "account", "[ACCOUNT]", "Sessione non valida");
            return;
        }

        try {
            LogicControllerGestioneAccount logicController = new LogicControllerGestioneAccount();
            DatiAccountBean dati = logicController.recuperaInformazioniAccount(sessione);

            if (dati == null) {
                GraphicControllerUtils.notifyError(log(), navigator, "account", "[ACCOUNT]", "Impossibile recuperare dati account");
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("idUtente", dati.getIdUtente());
            payload.put("nome", dati.getNome());
            payload.put("cognome", dati.getCognome());
            payload.put("email", dati.getEmail());

            if (navigator != null) {
                navigator.goTo("account", Map.of("datiAccount", payload));
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore caricamento account", e);
        }
    }

    @Override
    public void aggiornaDatiAccount(Map<String, Object> nuoviDati) {
        if (nuoviDati == null) {
            GraphicControllerUtils.notifyError(log(), navigator, "account", "[ACCOUNT]", "Dati account mancanti");
            return;
        }

        Object idUtente = nuoviDati.get("idUtente");
        if (!(idUtente instanceof Integer) || ((Integer) idUtente) <= 0) {
            GraphicControllerUtils.notifyError(log(), navigator, "account", "[ACCOUNT]", "Id utente non valido");
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
        
        // Delega a LogicController (creato on-demand)
        LogicControllerGestioneAccount logicController = new LogicControllerGestioneAccount();
        EsitoOperazioneBean esito = logicController.aggiornaDatiAccountConNotifica(bean);
        
        if (esito != null && esito.isSuccesso()) {
            if (navigator != null) {
                navigator.goTo("account", Map.of("successo", esito.getMessaggio()));
            }
        } else {
            GraphicControllerUtils.notifyError(log(), navigator, "account", "[ACCOUNT]",
                    esito != null ? esito.getMessaggio() : "Operazione non riuscita");
        }
    }

    @Override
    public void cambiaPassword(String vecchiaPassword, String nuovaPassword, SessioneUtenteBean sessione) {
        if (vecchiaPassword == null || nuovaPassword == null) {
            GraphicControllerUtils.notifyError(log(), navigator, "account", "[ACCOUNT]", "Password non valide");
            return;
        }
        if (sessione == null || sessione.getUtente() == null) {
            GraphicControllerUtils.notifyError(log(), navigator, "account", "[ACCOUNT]", "Sessione non valida");
            return;
        }

        LogicControllerGestioneAccount logicController = new LogicControllerGestioneAccount();
        EsitoOperazioneBean esito = logicController.cambiaPasswordConNotifica(vecchiaPassword, nuovaPassword, sessione);

        if (esito != null && esito.isSuccesso()) {
            if (navigator != null) {
                navigator.goTo("account", Map.of("successo", esito.getMessaggio()));
            }
        } else {
            GraphicControllerUtils.notifyError(log(), navigator, "account", "[ACCOUNT]",
                    esito != null ? esito.getMessaggio() : "Operazione non riuscita");
        }
    }

    @Override
    public void logout() {
        if (navigator != null) {
            navigator.goTo("login", null);
        }
    }

}
