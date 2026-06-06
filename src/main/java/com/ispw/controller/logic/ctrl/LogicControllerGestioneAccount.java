package com.ispw.controller.logic.ctrl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.DatiAccountBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.LogEntryBean;
import com.ispw.bean.LogsBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.bean.UtenteBean;
import com.ispw.controller.logic.ServiceFactory;
import com.ispw.controller.logic.interfaces.CtrlGestioneAccount;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaGestioneAccount;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.SystemLog;
import com.ispw.model.enums.StatoAccount;
import com.ispw.model.enums.TipoOperazione;

/**
 * Controller applicativo del caso d'uso "Gestione account".
 *
 * Il caso d'uso permette a un utente di:
 * - recuperare le informazioni del proprio account;
 * - aggiornare i dati personali disponibili;
 * - cambiare la password;
 * - confermare una modifica account.
 *
 * Il gestore può inoltre consultare gli ultimi log di sistema.
 *
 * Nota di progetto:
 * questa classe fa da "facciata applicativa" del caso d'uso.
 * Il layer grafico chiama solo questi metodi pubblici e non conosce i DAO
 * né i controller secondari usati internamente.
 */
public class LogicControllerGestioneAccount implements CtrlGestioneAccount {

    // Messaggi comuni
    private static final String MSG_SESSIONE_KO = "Sessione non valida";
    private static final String MSG_UTENTE_NOT_FOUND = "Utente non trovato";
    private static final String MSG_DATI_KO = "Dati non validi";
    private static final String MSG_EMAIL_DUP = "Email gia in uso";
    private static final String MSG_UPDATE_OK = "Dati account aggiornati";
    private static final String MSG_PWD_KO = "Password non valida";
    private static final String MSG_PWD_OLD_WRONG = "Vecchia password errata";
    private static final String MSG_PWD_OK = "Password aggiornata";

    @SuppressWarnings("java:S1312")
    private Logger log() {
        return Logger.getLogger(getClass().getName());
    }

    // =====================================================================
    // DAO
    // =====================================================================
    // I DAO vengono recuperati dalla DAOFactory.
    // In questo modo il controller lavora sulle interfacce e non conosce
    // se la persistenza è su DBMS, filesystem o in-memory.

    private GeneralUserDAO userDAO() {
        return DAOFactory.getInstance().getGeneralUserDAO();
    }

    private LogDAO logDAO() {
        return DAOFactory.getInstance().getLogDAO();
    }

    // =====================================================================
    // SERVICE CONTROLLER
    // =====================================================================
    // I controller secondari vengono recuperati dalla ServiceFactory.
    // In questo modo il controller principale resta stateless e non dipende
    // direttamente dalle implementazioni concrete dei servizi applicativi.

    private GestioneNotificaGestioneAccount notiCtrl() {
        return ServiceFactory.getNotificaGestioneAccountService();
    }

    // STEP 0: log di sistema

    /**
     * Restituisce gli ultimi log di sistema.
     *
     * Il metodo viene usato dal gestore per consultare le ultime operazioni
     * registrate nel sistema.
     */
    @Override
    public LogsBean listaUltimiLog(int limit) {
        //se il limite non è valido, uso un valore di default
        int safeLimit = limit > 0 ? limit : 20;

        LogsBean out = new LogsBean();

        //recupero gli ultimi log e li converto in bean per la UI
        out.setLogs(logDAO().findLast(safeLimit).stream()
                .map(this::toBean)
                .toList());

        return out;
    }

    /**
     * Converte un SystemLog nel bean usato dalla UI.
     */
    private LogEntryBean toBean(SystemLog log) {
        LogEntryBean bean = new LogEntryBean();

        if (log == null) {
            return bean;
        }

        bean.setTimestamp(log.getTimestamp());
        bean.setDescrizione(log.getDescrizione());
        bean.setIdUtenteCoinvolto(log.getIdUtenteCoinvolto());

        if (log.getTipoOperazione() != null) {
            bean.setTipoOperazione(log.getTipoOperazione().name());
        }

        return bean;
    }

    // STEP 1: recupera informazioni account

    /**
     * Recupera le informazioni dell'account legato alla sessione.
     *
     * Il metodo:
     * - controlla che la sessione sia valida;
     * - normalizza l'email dell'utente in sessione;
     * - recupera l'utente reale tramite DAO;
     * - costruisce il bean con i dati account.
     */
    @Override
    public DatiAccountBean recuperaInformazioniAccount(SessioneUtenteBean sessione) {
        //controllo che la sessione, l'utente e l'email siano presenti
        if (sessione == null || sessione.getUtente() == null
                || LogicControllerHelper.isBlank(sessione.getUtente().getEmail())) {
            return null;
        }

        //normalizzo l'email prima della ricerca
        final String email = LogicControllerHelper.normalizeEmail(sessione.getUtente().getEmail());

        //recupero l'utente reale dal DAO
        final GeneralUser u = userDAO().findByEmail(email);
        if (u == null) {
            return null;
        }

        //costruisco il bean da restituire al layer grafico
        DatiAccountBean out = new DatiAccountBean();
        out.setIdUtente(u.getIdUtente());
        out.setNome(u.getNome());
        out.setCognome(u.getCognome());
        out.setEmail(u.getEmail());

        /*
         * Nota:
         * GeneralUser non espone telefono/indirizzo.
         * Per questo motivo quei campi restano non valorizzati.
         */
        return out;
    }

    // STEP 2: aggiorna dati account

    /**
     * Versione con notifica interna.
     *
     * Mantiene lo stesso significato della vecchia versione:
     * prima aggiorna i dati account, poi invia una notifica solo se
     * l'aggiornamento è andato a buon fine.
     */
    public EsitoOperazioneBean aggiornaDatiAccountConNotifica(DatiAccountBean nuovidati) {
        EsitoOperazioneBean esito = aggiornaDatiAccount(nuovidati);

        if (esito.isSuccesso()) {
            inviaNotificaAggiornamentoDaDatiAccount(nuovidati);
        }

        return esito;
    }

    /**
     * Aggiorna i dati dell'account.
     *
     * Il metodo:
     * - controlla i dati inseriti;
     * - recupera l'utente dal DAO;
     * - verifica eventuale duplicazione email;
     * - aggiorna nome, cognome ed email;
     * - salva l'utente aggiornato;
     * - registra l'operazione nel log.
     *
     * Questa firma è quella esposta dall'interfaccia del caso d'uso.
     */
    @Override
    public EsitoOperazioneBean aggiornaDatiAccount(DatiAccountBean nuovidati) {
        //controllo che i dati minimi dell'account siano validi
        if (!isValid(nuovidati)) {
            return LogicControllerHelper.esito(false, MSG_DATI_KO);
        }

        //recupero l'utente dal DAO tramite id
        final GeneralUser u = userDAO().findById(nuovidati.getIdUtente());
        if (u == null) {
            return LogicControllerHelper.esito(false, MSG_UTENTE_NOT_FOUND);
        }

        //se l'email è presente e diversa da quella corrente, verifico che non sia già usata
        if (!LogicControllerHelper.isBlank(nuovidati.getEmail())) {
            final String target = LogicControllerHelper.normalizeEmail(nuovidati.getEmail());
            final String curr = LogicControllerHelper.normalizeEmail(u.getEmail());

            if (!Objects.equals(curr, target)) {
                final GeneralUser dup = userDAO().findByEmail(target);

                if (dup != null && dup.getIdUtente() != u.getIdUtente()) {
                    return LogicControllerHelper.esito(false, MSG_EMAIL_DUP);
                }

                u.setEmail(target);
            }
        }

        //aggiorno il nome se presente
        if (!LogicControllerHelper.isBlank(nuovidati.getNome())) {
            u.setNome(nuovidati.getNome().trim());
        }

        //aggiorno il cognome se presente
        if (!LogicControllerHelper.isBlank(nuovidati.getCognome())) {
            u.setCognome(nuovidati.getCognome().trim());
        }

        /*
         * Telefono e indirizzo non sono presenti in GeneralUser.
         * Per questo motivo vengono ignorati senza far fallire il caso d'uso.
         */
        userDAO().store(u);

        //registro l'aggiornamento nel log applicativo
        appendLogSafe(u.getIdUtente(),
                "[ACCOUNT] Aggiornati dati account",
                TipoOperazione.AGGIORNAMENTO_DATI_PERSONALI);

        //ritorno l'esito al graphic controller
        return LogicControllerHelper.esito(true, MSG_UPDATE_OK);
    }

    // STEP 3: cambia password

    /**
     * Versione con notifica interna.
     *
     * Mantiene lo stesso significato della vecchia versione:
     * prima cambia la password, poi invia una notifica solo se
     * l'operazione è andata a buon fine.
     */
    public EsitoOperazioneBean cambiaPasswordConNotifica(String vecchiaPwd,
                                                         String nuovaPwd,
                                                         SessioneUtenteBean sessione) {
        EsitoOperazioneBean esito = cambiaPassword(vecchiaPwd, nuovaPwd, sessione);

        if (esito.isSuccesso()) {
            UtenteBean ub = sessione != null ? sessione.getUtente() : null;
            inviaNotificaAggiornamentoAccount(ub);
        }

        return esito;
    }

    /**
     * Cambia la password dell'utente in sessione.
     *
     * Il metodo:
     * - controlla che la sessione sia valida;
     * - controlla vecchia e nuova password;
     * - recupera l'utente dal DAO;
     * - verifica che la vecchia password sia corretta;
     * - aggiorna la password;
     * - registra l'operazione nel log.
     *
     * Questa firma è quella esposta dall'interfaccia del caso d'uso.
     */
    @Override
    public EsitoOperazioneBean cambiaPassword(String vecchiaPwd,
                                              String nuovaPwd,
                                              SessioneUtenteBean sessione) {
        //controllo che la sessione e l'email siano valide
        if (sessione == null || sessione.getUtente() == null
                || LogicControllerHelper.isBlank(sessione.getUtente().getEmail())) {
            return LogicControllerHelper.esito(false, MSG_SESSIONE_KO);
        }

        //controllo che vecchia e nuova password siano valide
        if (LogicControllerHelper.isBlank(vecchiaPwd)
                || LogicControllerHelper.isBlank(nuovaPwd)
                || nuovaPwd.trim().length() < 6) {
            return LogicControllerHelper.esito(false, MSG_PWD_KO);
        }

        //normalizzo l'email prima della ricerca
        final String email = LogicControllerHelper.normalizeEmail(sessione.getUtente().getEmail());

        //recupero l'utente reale dal DAO
        final GeneralUser u = userDAO().findByEmail(email);
        if (u == null) {
            return LogicControllerHelper.esito(false, MSG_UTENTE_NOT_FOUND);
        }

        //verifico che la vecchia password corrisponda a quella salvata
        final String currPwd = u.getPassword();
        if (currPwd == null || !currPwd.equals(vecchiaPwd)) {
            return LogicControllerHelper.esito(false, MSG_PWD_OLD_WRONG);
        }

        /*
         * In produzione questa parte dovrebbe usare hashing.
         * In questa versione rimane coerente con la persistenza In-Memory.
         */
        u.setPassword(nuovaPwd.trim());
        userDAO().store(u);

        //registro il cambio password nel log applicativo
        appendLogSafe(u.getIdUtente(),
                "[ACCOUNT] Password aggiornata",
                TipoOperazione.PASSWORD_AGGIORNAMENTO);

        //ritorno l'esito al graphic controller
        return LogicControllerHelper.esito(true, MSG_PWD_OK);
    }

    // STEP 4: conferma modifica account

    /**
     * Conferma una modifica account.
     *
     * Il metodo:
     * - controlla che il bean utente sia valido;
     * - recupera l'utente tramite email;
     * - porta lo stato account ad ATTIVO;
     * - registra la conferma nel log.
     *
     * È best-effort: eventuali errori non vengono propagati al chiamante.
     */
    @Override
    public void confermaModificaAccount(UtenteBean utente) {
        //controllo che il bean utente e l'email siano presenti
        if (utente == null || LogicControllerHelper.isBlank(utente.getEmail())) {
            return;
        }

        //normalizzo l'email prima della ricerca
        final String email = LogicControllerHelper.normalizeEmail(utente.getEmail());

        //recupero l'utente reale dal DAO
        final GeneralUser u = userDAO().findByEmail(email);
        if (u == null) {
            return;
        }

        //porto l'account in stato attivo
        try {
            u.setStatoAccount(StatoAccount.ATTIVO);
            userDAO().store(u);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Aggiornamento stato account fallito: {0}", ex.getMessage());
        }

        //registro la conferma nel log applicativo
        appendLogSafe(u.getIdUtente(),
                "[ACCOUNT] Modifica account confermata",
                TipoOperazione.ACCOUNT_ATTIVATO);
    }

    // =====================================================================
    // NOTIFICHE
    // =====================================================================

    /**
     * Invia la notifica di aggiornamento account partendo dai dati account.
     *
     * Questo metodo viene usato dalla variante aggiornaDatiAccountConNotifica(...).
     */
    private void inviaNotificaAggiornamentoDaDatiAccount(DatiAccountBean dati) {
        if (dati == null || dati.getIdUtente() <= 0) {
            return;
        }

        try {
            final GeneralUser u = userDAO().findById(dati.getIdUtente());
            if (u != null) {
                inviaNotificaAggiornamentoAccount(toUtenteBean(u));
            }
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Notifica aggiornamento account fallita: {0}", ex.getMessage());
        }
    }

    /**
     * Invia la notifica di aggiornamento account partendo dal bean utente.
     *
     * Se il controller di notifica non è disponibile o fallisce,
     * il flusso principale non viene interrotto.
     */
    private void inviaNotificaAggiornamentoAccount(UtenteBean ub) {
        if (ub == null || notiCtrl() == null) {
            return;
        }

        try {
            notiCtrl().inviaConfermaAggiornamentoAccount(ub);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Notifica aggiornamento account fallita: {0}", ex.getMessage());
        }
    }

    // =====================================================================
    // VALIDAZIONE
    // =====================================================================

    /**
     * Controlla che i dati account siano validi.
     */
    private boolean isValid(DatiAccountBean b) {
        return b != null
                && b.getIdUtente() > 0
                && (b.getEmail() == null || !LogicControllerHelper.isBlank(b.getEmail()));
    }

    // =====================================================================
    // LOG
    // =====================================================================

    /**
     * Scrive un log senza interrompere il flusso in caso di errore.
     */
    private void appendLogSafe(int idUtente, String descr, TipoOperazione tipo) {
        try {
            SystemLog l = new SystemLog();
            l.setTimestamp(LocalDateTime.from(ZonedDateTime.now(ZoneId.systemDefault())));
            l.setIdUtenteCoinvolto(idUtente);
            l.setDescrizione(LogicControllerHelper.safe(descr));
            l.setTipoOperazione(tipo);

            logDAO().append(l);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Append log ACCOUNT fallito: {0}", ex.getMessage());
        }
    }

    // =====================================================================
    // MAPPING ENTITY -> BEAN
    // =====================================================================

    /**
     * Converte un GeneralUser nel bean usato dal layer grafico e dalle notifiche.
     */
    private UtenteBean toUtenteBean(GeneralUser u) {
        return new UtenteBean(
                u.getNome(),
                u.getCognome(),
                u.getEmail(),
                u.getRuolo()
        );
    }
}