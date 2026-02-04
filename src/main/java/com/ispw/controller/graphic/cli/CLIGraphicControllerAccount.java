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
        if (isSessioneNonValida(sessione, "Sessione non valida")) {
            return;
        }

        try {
            LogicControllerGestioneAccount logicController = new LogicControllerGestioneAccount();
            DatiAccountBean dati = logicController.recuperaInformazioniAccount(sessione);

            if (dati == null) {
                notifyAccountError("Impossibile recuperare dati account");
                return;
            }

            navigateAccountData(dati);
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
            notifyAccountError("Dati account mancanti");
            return;
        }

        Object idUtente = nuoviDati.get(GraphicControllerUtils.KEY_ID_UTENTE);
        if (!(idUtente instanceof Integer) || ((Integer) idUtente) <= 0) {
            notifyAccountError("Id utente non valido");
            return;
        }
        
        DatiAccountBean bean = buildAccountBean(nuoviDati, (Integer) idUtente);

        LogicControllerGestioneAccount logicController = new LogicControllerGestioneAccount();
        EsitoOperazioneBean esito = logicController.aggiornaDatiAccountConNotifica(bean);
        
        if (esito != null && esito.isSuccesso()) {
            navigateSuccess(esito.getMessaggio());
        } else {
            notifyAccountError(esito != null ? esito.getMessaggio() : "Operazione non riuscita");
        }
    }

    /**
     * Cambia password.
     */
    @Override
    public void cambiaPassword(String vecchiaPassword, String nuovaPassword, SessioneUtenteBean sessione) {
        if (vecchiaPassword == null || nuovaPassword == null) {
            notifyAccountError("Password non valide");
            return;
        }
        if (isSessioneNonValida(sessione, "Sessione non valida")) {
            return;
        }

        LogicControllerGestioneAccount logicController = new LogicControllerGestioneAccount();
        EsitoOperazioneBean esito = logicController.cambiaPasswordConNotifica(vecchiaPassword, nuovaPassword, sessione);

        if (esito != null && esito.isSuccesso()) {
            navigateSuccess(esito.getMessaggio());
        } else {
            notifyAccountError(esito != null ? esito.getMessaggio() : "Operazione non riuscita");
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

    private void notifyAccountError(String message) {
        GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_ACCOUNT,
            GraphicControllerUtils.PREFIX_ACCOUNT, message);
    }

    private boolean isSessioneNonValida(SessioneUtenteBean sessione, String message) {
        if (sessione == null || sessione.getUtente() == null) {
            notifyAccountError(message);
            return true;
        }
        return false;
    }

    private void navigateSuccess(String message) {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_ACCOUNT,
                Map.of(GraphicControllerUtils.KEY_SUCCESSO, message));
        }
    }

    private void navigateAccountData(DatiAccountBean dati) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(GraphicControllerUtils.KEY_ID_UTENTE, dati.getIdUtente());
        payload.put(GraphicControllerUtils.KEY_NOME, dati.getNome());
        payload.put(GraphicControllerUtils.KEY_COGNOME, dati.getCognome());
        payload.put(GraphicControllerUtils.KEY_EMAIL, dati.getEmail());

        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_ACCOUNT,
                Map.of(GraphicControllerUtils.KEY_DATI_ACCOUNT, payload));
        }
    }

    private DatiAccountBean buildAccountBean(Map<String, Object> nuoviDati, int idUtente) {
        DatiAccountBean bean = new DatiAccountBean();
        bean.setIdUtente(idUtente);
        if (nuoviDati.containsKey(GraphicControllerUtils.KEY_NOME)) {
            bean.setNome((String) nuoviDati.get(GraphicControllerUtils.KEY_NOME));
        }
        if (nuoviDati.containsKey(GraphicControllerUtils.KEY_COGNOME)) {
            bean.setCognome((String) nuoviDati.get(GraphicControllerUtils.KEY_COGNOME));
        }
        if (nuoviDati.containsKey(GraphicControllerUtils.KEY_EMAIL)) {
            bean.setEmail((String) nuoviDati.get(GraphicControllerUtils.KEY_EMAIL));
        }
        return bean;
    }

}
