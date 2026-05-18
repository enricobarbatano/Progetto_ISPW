package com.ispw.controller.logic.ctrl;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.DatiRegistrazioneBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.UtenteBean;
import com.ispw.controller.logic.ServiceFactory;
import com.ispw.controller.logic.interfaces.CtrlRegistrazione;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaRegistrazione;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.SystemLog;
import com.ispw.model.entity.UtenteFinale;
import com.ispw.model.enums.Ruolo;
import com.ispw.model.enums.StatoAccount;
import com.ispw.model.enums.TipoOperazione;

/**
 * Controller applicativo del caso d'uso "Registrazione".
 *
 * Il caso d'uso permette a un nuovo utente finale di registrarsi nel sistema.
 *
 * Il metodo principale:
 * - controlla i dati inseriti;
 * - normalizza l'email;
 * - verifica che l'email non sia già registrata;
 * - crea un nuovo utente finale;
 * - salva l'utente tramite DAO;
 * - registra l'operazione nel log;
 * - invia la notifica di conferma registrazione;
 * - simula l'attivazione immediata dell'account.
 *
 * Nota di progetto:
 * questa classe fa da "facciata applicativa" del caso d'uso.
 * Il layer grafico chiama solo questi metodi pubblici e non conosce i DAO
 * né i controller secondari usati internamente.
 */
public class LogicControllerRegistrazione implements CtrlRegistrazione {

    private static final String MSG_DATI_KO = "Dati registrazione o servizio notifica non validi";
    private static final String MSG_EMAIL_DUP = "Email gia registrata";
    private static final String MSG_REG_OK = "Registrazione avviata. Controlla la tua email per la conferma.";

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

    private GestioneNotificaRegistrazione notiCtrl() {
        return ServiceFactory.getNotificaRegistrazioneService();
    }

    // STEP 1: registrazione nuovo utente

    /**
     * Registra un nuovo utente finale.
     *
     * Il metodo:
     * - controlla i dati di registrazione;
     * - normalizza l'email;
     * - verifica l'unicità dell'email;
     * - crea un UtenteFinale;
     * - salva l'utente tramite DAO;
     * - registra il log di registrazione;
     * - invia la notifica di conferma;
     * - simula l'attivazione immediata;
     * - restituisce l'esito al layer grafico.
     */
    @Override
    public EsitoOperazioneBean registraNuovoUtente(DatiRegistrazioneBean datiInput) {
        //controllo che i dati minimi di registrazione siano validi e che il servizio notifica sia disponibile
        if (!isValid(datiInput) || notiCtrl() == null) {
            log().warning("[REG] Input non valido per registrazione");
            return LogicControllerHelper.esito(false, MSG_DATI_KO);
        }

        //normalizzo l'email prima di usarla per ricerca e salvataggio
        final String emailNorm = LogicControllerHelper.normalizeEmail(datiInput.getEmail());

        //verifico che non esista già un utente con la stessa email
        final GeneralUser existing = userDAO().findByEmail(emailNorm);
        if (existing != null) {
            log().log(Level.WARNING, "[REG] Email gia presente: {0}", emailNorm);
            return LogicControllerHelper.esito(false, MSG_EMAIL_DUP);
        }

        //creo un nuovo utente finale in stato DA_CONFERMARE
        final UtenteFinale nuovo = new UtenteFinale();
        nuovo.setNome(datiInput.getNome());
        nuovo.setCognome(datiInput.getCognome());
        nuovo.setEmail(emailNorm);
        nuovo.setPassword(datiInput.getPassword());
        nuovo.setStatoAccount(StatoAccount.DA_CONFERMARE);
        nuovo.setRuolo(Ruolo.UTENTE);

        /*
         * Store instradato dalla facade GeneralUserDAO.
         * Il controller non conosce il DAO concreto dell'utente finale.
         */
        userDAO().store(nuovo);

        //registro l'avvio della registrazione nel log applicativo
        appendLog(nuovo.getIdUtente(),
                TipoOperazione.REGISTRAZIONE_ACCOUNT,
                "Registrazione avviata; richiesta conferma inviata");

        //invio la notifica di conferma registrazione
        inviaNotificaConfermaRegistrazione(toBean(nuovo));

        /*
         * Simulazione conferma immediata.
         * Nel sistema reale questa parte potrebbe essere attivata da link email.
         */
        nuovo.setStatoAccount(StatoAccount.ATTIVO);
        userDAO().store(nuovo);

        //registro l'attivazione simulata nel log applicativo
        appendLog(nuovo.getIdUtente(),
                TipoOperazione.ACCOUNT_ATTIVATO,
                "Conferma registrazione completata (simulata)");

        log().log(Level.INFO, "[REG] Registrazione creata per utente #{0} ({1})",
                new Object[]{nuovo.getIdUtente(), nuovo.getEmail()});

        //ritorno l'esito al graphic controller
        return LogicControllerHelper.esito(true, MSG_REG_OK);
    }

    // STEP 2: conferma nuovo account

    /**
     * Conferma un nuovo account partendo dal bean utente.
     *
     * Il metodo:
     * - controlla che il bean utente sia valido;
     * - normalizza l'email;
     * - recupera l'utente dal DAO;
     * - imposta lo stato account ad ATTIVO;
     * - registra l'operazione nel log.
     */
    @Override
    public void confermaNuovoAccount(UtenteBean utente) {
        //controllo che il bean utente e l'email siano presenti
        if (utente == null || LogicControllerHelper.isBlank(utente.getEmail())) {
            log().warning("[REG-CONF] UtenteBean nullo o email vuota");
            return;
        }

        //normalizzo l'email prima della ricerca
        final String emailNorm = LogicControllerHelper.normalizeEmail(utente.getEmail());

        //recupero l'utente reale dal DAO
        final GeneralUser u = userDAO().findByEmail(emailNorm);
        if (u == null) {
            log().log(Level.WARNING, "[REG-CONF] Nessun utente per email: {0}", emailNorm);
            return;
        }

        //attivo l'account
        u.setStatoAccount(StatoAccount.ATTIVO);
        userDAO().store(u);

        //registro la conferma nel log applicativo
        appendLog(u.getIdUtente(),
                TipoOperazione.ACCOUNT_ATTIVATO,
                "Conferma registrazione completata");

        log().log(Level.INFO, "[REG-CONF] Account confermato per utente #{0}", u.getIdUtente());
    }

    // STEP 3: finalizza attivazione account

    /**
     * Finalizza l'attivazione di un account tramite id utente.
     *
     * Il metodo:
     * - controlla che l'id sia valido;
     * - recupera l'utente dal DAO;
     * - imposta lo stato account ad ATTIVO;
     * - registra l'operazione nel log.
     */
    @Override
    public void finalizzaAttivazioneAccount(int idUtente) {
        //controllo che l'id utente sia valido
        if (idUtente <= 0) {
            log().warning("[REG-FINAL] idUtente non valido");
            return;
        }

        //recupero l'utente tramite id
        final GeneralUser u = userDAO().findById(idUtente);
        if (u == null) {
            log().log(Level.WARNING, "[REG-FINAL] Utente non trovato: {0}", idUtente);
            return;
        }

        //attivo l'account
        u.setStatoAccount(StatoAccount.ATTIVO);
        userDAO().store(u);

        //registro l'attivazione nel log applicativo
        appendLog(u.getIdUtente(),
                TipoOperazione.ACCOUNT_ATTIVATO,
                "Attivazione account completata");

        log().log(Level.INFO, "[REG-FINAL] Account attivato per utente #{0}", idUtente);
    }

    // =====================================================================
    // NOTIFICHE
    // =====================================================================

    /**
     * Invia la notifica di conferma registrazione.
     *
     * Se il controller di notifica non è disponibile o fallisce,
     * il flusso principale non viene interrotto.
     */
    private void inviaNotificaConfermaRegistrazione(UtenteBean utente) {
        if (utente == null || notiCtrl() == null) {
            return;
        }

        try {
            notiCtrl().inviaConfermaRegistrazione(utente);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Notifica conferma registrazione fallita: {0}", ex.getMessage());
        }
    }

    // =====================================================================
    // VALIDAZIONE
    // =====================================================================

    /**
     * Controlla che i dati minimi di registrazione siano presenti.
     */
    private boolean isValid(DatiRegistrazioneBean b) {
        return b != null
                && LogicControllerHelper.hasText(b.getNome())
                && LogicControllerHelper.hasText(b.getCognome())
                && LogicControllerHelper.hasText(b.getEmail())
                && LogicControllerHelper.hasText(b.getPassword());
    }

    // =====================================================================
    // LOG
    // =====================================================================

    /**
     * Scrive un log applicativo della registrazione.
     */
    private void appendLog(int idUtente, TipoOperazione tipo, String descrizione) {
        final SystemLog logEntry = new SystemLog();
        logEntry.setTimestamp(LocalDateTime.now());
        logEntry.setTipoOperazione(tipo);
        logEntry.setIdUtenteCoinvolto(idUtente);
        logEntry.setDescrizione(LogicControllerHelper.safe(descrizione));

        logDAO().append(logEntry);
    }

    // =====================================================================
    // MAPPING ENTITY -> BEAN
    // =====================================================================

    /**
     * Converte l'utente di dominio nel bean usato dalla notifica.
     */
    private UtenteBean toBean(GeneralUser u) {
        return new UtenteBean(
                Objects.toString(u.getNome(), null),
                Objects.toString(u.getCognome(), null),
                u.getEmail(),
                u.getRuolo()
        );
    }
}