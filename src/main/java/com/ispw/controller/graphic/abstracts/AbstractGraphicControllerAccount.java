package com.ispw.controller.graphic.abstracts;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
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
 * Controller grafico astratto del caso d'uso "Gestione account".
 *
 * Questa classe contiene la logica comune tra GUI e CLI:
 * - controlla gli input ricevuti dalla view;
 * - chiama il controller logico tramite interfaccia;
 * - costruisce i bean necessari;
 * - aggiorna la sessione quando serve;
 * - usa il navigator per cambiare schermata o aggiornare la stessa route.
 *
 * Le classi concrete GUI e CLI gestiscono solo le differenze specifiche
 * del frontend, come il ritorno alla home o al login.
 *
 * Nota di progetto:
 * il graphic controller non conosce l'implementazione concreta del logic controller.
 * Usa CtrlGestioneAccount ottenuto tramite LogicControllerFactory.
 */
public abstract class AbstractGraphicControllerAccount implements GraphicControllerAccount {

    // =====================================================================
    // COLLABORATORI
    // =====================================================================
    // Il navigator è il router del layer grafico.
    // Permette al controller di spostarsi tra schermate senza conoscere
    // direttamente le view concrete.

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicControllerAccount(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    protected abstract Logger log();

    protected abstract void goToLogin();

    protected abstract void goToHome(SessioneUtenteBean sessione);

    // =====================================================================
    // LOGIC CONTROLLER
    // =====================================================================
    // Il controller logico viene recuperato tramite factory e restituito
    // tramite interfaccia. In questo modo il graphic controller non dipende
    // dalla classe concreta LogicControllerGestioneAccount.

    protected CtrlGestioneAccount logicController() {
        return LogicControllerFactory.getGestioneAccountController();
    }

    /*
     * Questi metodi sono hook protetti.
     * Mantengono compatibilità con eventuali classi concrete che li ridefiniscono,
     * ma di default delegano al controller logico recuperato dalla factory.
     */

    protected DatiAccountBean recuperaInformazioniAccount(SessioneUtenteBean sessione) {
        return logicController().recuperaInformazioniAccount(sessione);
    }

    protected EsitoOperazioneBean aggiornaDatiAccountConNotifica(DatiAccountBean bean) {
        /*
         * L'interfaccia espone aggiornaDatiAccount(...).
         * Se nella classe concreta del logic controller esiste anche la variante
         * con notifica, viene usata dai graphic controller concreti che la invocano.
         * Qui manteniamo la firma già presente e deleghiamo alla firma standard.
         */
        return logicController().aggiornaDatiAccount(bean);
    }

    protected EsitoOperazioneBean cambiaPasswordConNotifica(String vecchiaPassword,
                                                            String nuovaPassword,
                                                            SessioneUtenteBean sessione) {
        /*
         * Come sopra, manteniamo l'hook compatibile con la struttura esistente
         * e deleghiamo al metodo del caso d'uso esposto dall'interfaccia.
         */
        return logicController().cambiaPassword(vecchiaPassword, nuovaPassword, sessione);
    }

    // =====================================================================
    // NAVIGAZIONE
    // =====================================================================

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_ACCOUNT;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        GraphicControllerUtils.handleOnShow(log(), params, GraphicControllerUtils.PREFIX_ACCOUNT);
    }

    // STEP 1: caricamento account

    /**
     * Carica i dati account dell'utente in sessione.
     *
     * Il metodo:
     * - controlla che la sessione sia valida;
     * - chiama il controller logico;
     * - costruisce il payload dei dati account;
     * - aggiorna la route account tramite navigator.
     */
    @Override
    public void loadAccount(SessioneUtenteBean sessione) {
        //controllo che la sessione sia valida
        if (isSessioneNonValida(sessione, GraphicControllerUtils.MSG_SESSIONE_NON_VALIDA)) {
            return;
        }

        try {
            //recupero i dati account tramite controller logico
            DatiAccountBean dati = recuperaInformazioniAccount(sessione);

            //se i dati non sono presenti, notifico errore
            if (dati == null) {
                notifyAccountError(GraphicControllerUtils.MSG_IMPOSSIBILE_RECUPERARE_DATI_ACCOUNT);
                return;
            }

            //navigo sulla route account passando i dati recuperati
            navigateAccountData(dati);
        } catch (RuntimeException ex) {
            log().log(Level.SEVERE, "Errore caricamento account", ex);
        }
    }

    // STEP 2: aggiornamento dati account

    /**
     * Aggiorna i dati account partendo da una mappa di parametri.
     *
     * Il metodo:
     * - valida i parametri ricevuti;
     * - costruisce il bean DatiAccountBean;
     * - chiama il controller logico;
     * - aggiorna la sessione se presente;
     * - naviga con successo o mostra errore.
     */
    @Override
    public void aggiornaDatiAccount(Map<String, Object> nuoviDati) {
        //controllo che la mappa dei nuovi dati sia presente
        if (nuoviDati == null) {
            notifyAccountError(GraphicControllerUtils.MSG_DATI_ACCOUNT_MANCANTI);
            return;
        }

        //controllo che l'id utente sia valido
        Object idUtente = nuoviDati.get(GraphicControllerUtils.KEY_ID_UTENTE);
        if (!(idUtente instanceof Integer id) || id <= 0) {
            notifyAccountError(GraphicControllerUtils.MSG_ID_UTENTE_NON_VALIDO);
            return;
        }

        //costruisco il bean account a partire dalla mappa
        DatiAccountBean bean = buildAccountBean(nuoviDati, id);

        //richiamo il controller logico
        EsitoOperazioneBean esito = aggiornaDatiAccountConNotifica(bean);

        //aggiorno la sessione, se presente nel payload
        SessioneUtenteBean sessione = updateSessionIfPresent(nuoviDati, bean);

        //navigo in base all'esito
        if (esito != null && esito.isSuccesso()) {
            navigateSuccess(esito.getMessaggio(), sessione);
        } else {
            notifyAccountError(esito != null
                    ? esito.getMessaggio()
                    : GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
        }
    }

    // STEP 3: cambio password

    /**
     * Cambia la password dell'utente in sessione.
     *
     * Il metodo:
     * - controlla le password ricevute;
     * - controlla la sessione;
     * - chiama il controller logico;
     * - naviga con successo o mostra errore.
     */
    @Override
    public void cambiaPassword(String vecchiaPassword, String nuovaPassword, SessioneUtenteBean sessione) {
        //controllo che le password siano presenti
        if (vecchiaPassword == null || nuovaPassword == null) {
            notifyAccountError(GraphicControllerUtils.MSG_PASSWORD_NON_VALIDE);
            return;
        }

        //controllo che la sessione sia valida
        if (isSessioneNonValida(sessione, GraphicControllerUtils.MSG_SESSIONE_NON_VALIDA)) {
            return;
        }

        //richiamo il controller logico
        EsitoOperazioneBean esito = cambiaPasswordConNotifica(vecchiaPassword, nuovaPassword, sessione);

        //navigo in base all'esito
        if (esito != null && esito.isSuccesso()) {
            navigateSuccess(esito.getMessaggio(), sessione);
        } else {
            notifyAccountError(esito != null
                    ? esito.getMessaggio()
                    : GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
        }
    }

    // STEP 4: logout e home

    /**
     * Esegue il logout tornando alla schermata di login.
     */
    @Override
    public void logout() {
        goToLogin();
    }

    /**
     * Torna alla home mantenendo la sessione corrente.
     */
    @Override
    public void tornaAllaHome(SessioneUtenteBean sessione) {
        goToHome(sessione);
    }

    // =====================================================================
    // HELPERS ACCOUNT
    // =====================================================================

    /**
     * Notifica un errore account e resta sulla route account.
     */
    private void notifyAccountError(String message) {
        GraphicControllerUtils.notifyError(
                log(),
                navigator,
                GraphicControllerUtils.ROUTE_ACCOUNT,
                GraphicControllerUtils.PREFIX_ACCOUNT,
                message
        );
    }

    /**
     * Controlla se la sessione è mancante o non valida.
     */
    private boolean isSessioneNonValida(SessioneUtenteBean sessione, String message) {
        if (sessione == null || sessione.getUtente() == null) {
            notifyAccountError(message);
            return true;
        }

        return false;
    }

    /**
     * Naviga sulla route account mostrando un messaggio di successo.
     */
    private void navigateSuccess(String message, SessioneUtenteBean sessione) {
        if (navigator == null) {
            return;
        }

        if (sessione != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_ACCOUNT,
                    Map.of(GraphicControllerUtils.KEY_SUCCESSO, message,
                            GraphicControllerUtils.KEY_SESSIONE, sessione));
            return;
        }

        navigator.goTo(GraphicControllerUtils.ROUTE_ACCOUNT,
                Map.of(GraphicControllerUtils.KEY_SUCCESSO, message));
    }

    /**
     * Prepara il payload dei dati account e aggiorna la route account.
     */
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

    /**
     * Costruisce il bean account partendo dalla mappa ricevuta dal frontend.
     */
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

    /**
     * Aggiorna i dati utente nella sessione, se la sessione è presente nel payload.
     */
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

    /**
     * Controlla che una stringa contenga almeno un carattere non spazio.
     */
    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}