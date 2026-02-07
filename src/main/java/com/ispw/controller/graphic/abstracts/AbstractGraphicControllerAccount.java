package com.ispw.controller.graphic.abstracts;

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

public abstract class AbstractGraphicControllerAccount implements GraphicControllerAccount {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: implementa GraphicControllerAccount (interfaccia) e usa GraphicControllerNavigation.
    // A2) IO verso GUI/CLI: riceve/ritorna bean (DatiAccountBean, SessioneUtenteBean) e Map.
    // A3) Logica delegata: richiama LogicController via metodi astratti.

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicControllerAccount(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    protected abstract Logger log();

    protected abstract void goToLogin();

    protected abstract void goToHome(SessioneUtenteBean sessione);

    protected abstract DatiAccountBean recuperaInformazioniAccount(SessioneUtenteBean sessione);

    protected abstract EsitoOperazioneBean aggiornaDatiAccountConNotifica(DatiAccountBean bean);

    protected abstract EsitoOperazioneBean cambiaPasswordConNotifica(String vecchiaPassword, String nuovaPassword,
                                                                     SessioneUtenteBean sessione);

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_ACCOUNT;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        GraphicControllerUtils.handleOnShow(log(), params, GraphicControllerUtils.PREFIX_ACCOUNT);
    }

    @Override
    public void loadAccount(SessioneUtenteBean sessione) {
        if (isSessioneNonValida(sessione, GraphicControllerUtils.MSG_SESSIONE_NON_VALIDA)) {
            return;
        }

        try {
            DatiAccountBean dati = recuperaInformazioniAccount(sessione);

            if (dati == null) {
                notifyAccountError(GraphicControllerUtils.MSG_IMPOSSIBILE_RECUPERARE_DATI_ACCOUNT);
                return;
            }

            navigateAccountData(dati);
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore caricamento account", e);
        }
    }

    @Override
    public void aggiornaDatiAccount(Map<String, Object> nuoviDati) {
        if (nuoviDati == null) {
            notifyAccountError(GraphicControllerUtils.MSG_DATI_ACCOUNT_MANCANTI);
            return;
        }

        Object idUtente = nuoviDati.get(GraphicControllerUtils.KEY_ID_UTENTE);
        if (!(idUtente instanceof Integer id) || id <= 0) {
            notifyAccountError(GraphicControllerUtils.MSG_ID_UTENTE_NON_VALIDO);
            return;
        }

        DatiAccountBean bean = buildAccountBean(nuoviDati, id);

        EsitoOperazioneBean esito = aggiornaDatiAccountConNotifica(bean);
        SessioneUtenteBean sessione = updateSessionIfPresent(nuoviDati, bean);

        if (esito != null && esito.isSuccesso()) {
            navigateSuccess(esito.getMessaggio(), sessione);
        } else {
            notifyAccountError(esito != null ? esito.getMessaggio()
                : GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
        }
    }

    @Override
    public void cambiaPassword(String vecchiaPassword, String nuovaPassword, SessioneUtenteBean sessione) {
        if (vecchiaPassword == null || nuovaPassword == null) {
            notifyAccountError(GraphicControllerUtils.MSG_PASSWORD_NON_VALIDE);
            return;
        }
        if (isSessioneNonValida(sessione, GraphicControllerUtils.MSG_SESSIONE_NON_VALIDA)) {
            return;
        }

        EsitoOperazioneBean esito = cambiaPasswordConNotifica(vecchiaPassword, nuovaPassword, sessione);

        if (esito != null && esito.isSuccesso()) {
            navigateSuccess(esito.getMessaggio(), sessione);
        } else {
            notifyAccountError(esito != null ? esito.getMessaggio()
                : GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
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

    // SEZIONE LOGICA
    // Legenda metodi:
    // 1) notifyAccountError(...) - notifica errore e naviga.
    // 2) isSessioneNonValida(...) - valida sessione.
    // 3) navigateSuccess(...) - naviga con messaggio di successo.
    // 4) navigateAccountData(...) - prepara payload account.
    // 5) buildAccountBean(...) - costruisce bean account.
    // 6) updateSessionIfPresent(...) - aggiorna sessione se presente.
    // 7) hasText(...) - verifica stringhe.
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

    private void navigateSuccess(String message, SessioneUtenteBean sessione) {
        if (navigator != null) {
            if (sessione != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_ACCOUNT,
                    Map.of(GraphicControllerUtils.KEY_SUCCESSO, message,
                           GraphicControllerUtils.KEY_SESSIONE, sessione));
            } else {
                navigator.goTo(GraphicControllerUtils.ROUTE_ACCOUNT,
                    Map.of(GraphicControllerUtils.KEY_SUCCESSO, message));
            }
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

    private SessioneUtenteBean updateSessionIfPresent(Map<String, Object> nuoviDati, DatiAccountBean bean) {
        Object raw = nuoviDati.get(GraphicControllerUtils.KEY_SESSIONE);
        if (!(raw instanceof SessioneUtenteBean sessione)) {
            return null;
        }
        if (sessione.getUtente() == null || bean == null) {
            return sessione;
        }
        if (hasText(bean.getNome())) {
            sessione.getUtente().setNome(bean.getNome().trim());
        }
        if (hasText(bean.getCognome())) {
            sessione.getUtente().setCognome(bean.getCognome().trim());
        }
        if (hasText(bean.getEmail())) {
            sessione.getUtente().setEmail(bean.getEmail().trim());
        }
        return sessione;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
