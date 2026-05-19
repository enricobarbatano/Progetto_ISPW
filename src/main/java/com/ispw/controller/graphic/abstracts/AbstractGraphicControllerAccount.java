package com.ispw.controller.graphic.abstracts;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.DatiAccountBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.bean.UtenteBean;
import com.ispw.controller.graphic.interfaces.GraphicControllerAccount;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.logic.LogicControllerFactory;
import com.ispw.controller.logic.interfaces.CtrlGestioneAccount;

/**
 * Controller grafico astratto per la gestione account.
 *
 * RESPONSABILITÀ:
 * - ricevere input grezzi dalla view;
 * - recuperare i dati account tramite logic controller;
 * - costruire i bean necessari al logic controller;
 * - preparare i dati da renderizzare nella view;
 * - gestire la navigazione tramite navigator.
 *
 * NON:
 * - conosce componenti JavaFX;
 * - crea elementi grafici;
 * - accede direttamente a DAO o persistenza;
 * - delega alla view la costruzione dei bean.
 *
 * Nota:
 * l'id utente non è contenuto in UtenteBean.
 * Per questo motivo viene recuperato tramite recuperaInformazioniAccount(...),
 * che restituisce un DatiAccountBean completo.
 */
public abstract class AbstractGraphicControllerAccount implements GraphicControllerAccount {

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicControllerAccount(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    /**
     * Restituisce il logger della classe concreta.
     */
    protected abstract Logger log();

    /**
     * Naviga alla schermata di login.
     */
    protected abstract void goToLogin();

    /**
     * Naviga alla home mantenendo, se possibile, la sessione.
     */
    protected abstract void goToHome(SessioneUtenteBean sessione);

    /**
     * Recupera il controller logico della gestione account.
     */
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
    // CARICAMENTO ACCOUNT
    // =========================================================

    /**
     * Carica le informazioni dell'account corrente.
     *
     * La view non recupera dati e non costruisce Map applicative.
     * Il controller grafico chiama il logic controller, riceve un bean
     * completo e prepara il payload per il rendering.
     */
    @Override
    public void loadAccount(SessioneUtenteBean sessione) {
        if (isSessioneNonValida(sessione)) {
            notifyError(GraphicControllerUtils.MSG_SESSIONE_NON_VALIDA);
            return;
        }

        try {
            DatiAccountBean dati = logicController().recuperaInformazioniAccount(sessione);

            if (dati == null || dati.getIdUtente() <= 0) {
                notifyError(GraphicControllerUtils.MSG_DATI_ACCOUNT_MANCANTI);
                return;
            }

            Map<String, Object> params = new HashMap<>();
            params.put(GraphicControllerUtils.KEY_SESSIONE, sessione);
            params.put(GraphicControllerUtils.KEY_DATI_ACCOUNT, buildAccountPayload(dati));

            goToAccount(params);

        } catch (RuntimeException ex) {
            log().log(Level.SEVERE, "Errore caricamento dati account", ex);
            notifyError(GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
        }
    }

    // =========================================================
    // AGGIORNAMENTO DATI ACCOUNT
    // =========================================================

    /**
     * Aggiorna i dati dell'account.
     *
     * La view passa solo valori semplici.
     * Il bean DatiAccountBean viene costruito in questo controller grafico.
     */
    @Override
    public void aggiornaDatiAccount(int idUtente,
                                    String nome,
                                    String cognome,
                                    String email,
                                    SessioneUtenteBean sessione) {

        if (idUtente <= 0 || isBlank(nome) || isBlank(cognome) || isBlank(email)) {
            notifyError(GraphicControllerUtils.MSG_DATI_ACCOUNT_MANCANTI);
            return;
        }

        DatiAccountBean bean = buildDatiAccountBean(
                idUtente,
                nome.trim(),
                cognome.trim(),
                email.trim()
        );

        try {
            EsitoOperazioneBean esito = logicController().aggiornaDatiAccount(bean);

            if (esito == null || !esito.isSuccesso()) {
                notifyError(esito != null
                        ? esito.getMessaggio()
                        : GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
                return;
            }

            updateSessioneAccountData(sessione, bean);

            Map<String, Object> params = new HashMap<>();
            params.put(GraphicControllerUtils.KEY_SUCCESSO, esito.getMessaggio());
            params.put(GraphicControllerUtils.KEY_DATI_ACCOUNT, buildAccountPayload(bean));

            if (sessione != null) {
                params.put(GraphicControllerUtils.KEY_SESSIONE, sessione);
            }

            goToAccount(params);

        } catch (RuntimeException ex) {
            log().log(Level.SEVERE, "Errore aggiornamento dati account", ex);
            notifyError(GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
        }
    }

    // =========================================================
    // CAMBIO PASSWORD
    // =========================================================

    /**
     * Cambia la password dell'utente corrente.
     *
     * La view passa soltanto le due stringhe.
     * La verifica effettiva viene delegata al logic controller.
     */
    @Override
    public void cambiaPassword(String vecchiaPassword,
                               String nuovaPassword,
                               SessioneUtenteBean sessione) {

        if (isBlank(vecchiaPassword) || isBlank(nuovaPassword)) {
            notifyError(GraphicControllerUtils.MSG_PASSWORD_NON_VALIDE);
            return;
        }

        if (isSessioneNonValida(sessione)) {
            notifyError(GraphicControllerUtils.MSG_SESSIONE_NON_VALIDA);
            return;
        }

        try {
            EsitoOperazioneBean esito =
                    logicController().cambiaPassword(vecchiaPassword, nuovaPassword, sessione);

            if (esito == null || !esito.isSuccesso()) {
                notifyError(esito != null
                        ? esito.getMessaggio()
                        : GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
                return;
            }

            Map<String, Object> params = new HashMap<>();
            params.put(GraphicControllerUtils.KEY_SUCCESSO, esito.getMessaggio());
            params.put(GraphicControllerUtils.KEY_SESSIONE, sessione);

            DatiAccountBean dati = logicController().recuperaInformazioniAccount(sessione);
            if (dati != null) {
                params.put(GraphicControllerUtils.KEY_DATI_ACCOUNT, buildAccountPayload(dati));
            }

            goToAccount(params);

        } catch (RuntimeException ex) {
            log().log(Level.SEVERE, "Errore cambio password", ex);
            notifyError(GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
        }
    }

    // =========================================================
    // NAVIGAZIONE
    // =========================================================

    /**
     * Effettua il logout.
     */
    @Override
    public void logout() {
        goToLogin();
    }

    /**
     * Torna alla home.
     */
    @Override
    public void tornaAllaHome(SessioneUtenteBean sessione) {
        goToHome(sessione);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    /**
     * Costruisce il bean usato dal logic controller per aggiornare i dati.
     */
    private DatiAccountBean buildDatiAccountBean(int idUtente,
                                                 String nome,
                                                 String cognome,
                                                 String email) {
        DatiAccountBean bean = new DatiAccountBean();
        bean.setIdUtente(idUtente);
        bean.setNome(nome);
        bean.setCognome(cognome);
        bean.setEmail(email);
        return bean;
    }

    /**
     * Costruisce il payload usato dalla view per renderizzare i dati account.
     */
    private Map<String, Object> buildAccountPayload(DatiAccountBean dati) {
        Map<String, Object> payload = new HashMap<>();

        payload.put(GraphicControllerUtils.KEY_ID_UTENTE, dati.getIdUtente());
        payload.put(GraphicControllerUtils.KEY_NOME, safe(dati.getNome()));
        payload.put(GraphicControllerUtils.KEY_COGNOME, safe(dati.getCognome()));
        payload.put(GraphicControllerUtils.KEY_EMAIL, safe(dati.getEmail()));

        return payload;
    }

    /**
     * Aggiorna anche i dati anagrafici contenuti nella sessione.
     *
     * Serve soprattutto dopo la modifica email, nome o cognome,
     * così la sessione resta coerente con i dati appena salvati.
     */
    private void updateSessioneAccountData(SessioneUtenteBean sessione, DatiAccountBean dati) {
        if (sessione == null || sessione.getUtente() == null || dati == null) {
            return;
        }

        UtenteBean utente = sessione.getUtente();
        utente.setNome(dati.getNome());
        utente.setCognome(dati.getCognome());
        utente.setEmail(dati.getEmail());
    }

    /**
     * Naviga alla route account con i parametri indicati.
     */
    private void goToAccount(Map<String, Object> params) {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_ACCOUNT, params);
        }
    }

    /**
     * Notifica un errore sulla route account.
     */
    private void notifyError(String message) {
        GraphicControllerUtils.notifyError(
                log(),
                navigator,
                GraphicControllerUtils.ROUTE_ACCOUNT,
                GraphicControllerUtils.PREFIX_ACCOUNT,
                message
        );
    }

    /**
     * Controlla se la sessione è assente o incompleta.
     */
    private boolean isSessioneNonValida(SessioneUtenteBean sessione) {
        return sessione == null || sessione.getUtente() == null;
    }

    /**
     * Controlla se una stringa è nulla o vuota.
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Evita valori nulli nel payload passato alla view.
     */
    private String safe(String value) {
        return value != null ? value : "";
    }
}