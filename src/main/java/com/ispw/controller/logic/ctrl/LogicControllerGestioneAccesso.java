package com.ispw.controller.logic.ctrl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.DatiLoginBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.bean.UtenteBean;
import com.ispw.controller.logic.interfaces.CtrlAccesso;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.SystemLog;
import com.ispw.model.enums.StatoAccount;
import com.ispw.model.enums.TipoOperazione;

/**
 * Controller applicativo del caso d'uso "Gestione accesso".
 *
 * Il caso d'uso permette a un utente di autenticarsi nel sistema.
 *
 * Il metodo principale:
 * - controlla i dati di login inseriti;
 * - normalizza l'email;
 * - recupera l'utente tramite la facade GeneralUserDAO;
 * - verifica la password;
 * - controlla che l'account sia attivo;
 * - crea una sessione utente valida.
 *
 * Nota di progetto:
 * questa classe fa da "facciata applicativa" del caso d'uso.
 * Il layer grafico chiama solo questi metodi pubblici e non conosce i DAO
 * né la logica interna usata per recuperare l'utente reale.
 */
public class LogicControllerGestioneAccesso implements CtrlAccesso {

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
    //
    // La risoluzione dell'utente viene demandata alla facade GeneralUserDAO.
    // A runtime questa facade può aggregare più DAO concreti.

    private GeneralUserDAO userDAO() {
        return DAOFactory.getInstance().getGeneralUserDAO();
    }

    private LogDAO logDAO() {
        return DAOFactory.getInstance().getLogDAO();
    }

    // STEP 1: verifica credenziali

    /**
     * Verifica le credenziali inserite dall'utente.
     *
     * Il metodo:
     * - controlla che il bean di login sia valido;
     * - controlla che email e password siano presenti;
     * - normalizza l'email;
     * - recupera l'utente tramite DAO;
     * - confronta la password;
     * - verifica che l'account sia attivo;
     * - crea e restituisce la sessione utente.
     *
     * @return SessioneUtenteBean se credenziali valide, null altrimenti.
     */
    @Override
    public SessioneUtenteBean verificaCredenziali(DatiLoginBean datiLogin) {
        //controllo che il bean di login non sia nullo
        if (datiLogin == null) {
            return null;
        }

        //recupero email e password dal bean ricevuto dal layer grafico
        final String email = datiLogin.getEmail();
        final String password = datiLogin.getPassword();

        //controllo che email e password siano presenti
        if (LogicControllerHelper.isBlank(email) || LogicControllerHelper.isBlank(password)) {
            return null;
        }

        //normalizzo l'email prima di usarla per la ricerca
        final String normEmail = LogicControllerHelper.normalizeEmail(email);
        if (normEmail == null) {
            return null;
        }

        /*
         * Risoluzione utente demandata alla Facade GeneralUserDAO.
         * In questo modo il controller non deve sapere se l'utente è un gestore
         * o un utente finale: sarà la facade a risolvere il tipo corretto.
         */
        final GeneralUser user = userDAO().findByEmail(normEmail);
        if (user == null) {
            return null;
        }

        // Password (fase 1: plain compare; fase security: hashing)
        if (!Objects.equals(user.getPassword(), password)) {
            return null;
        }

        //consento l'accesso solo agli account attivi
        if (user.getStatoAccount() != StatoAccount.ATTIVO) {
            throw new IllegalStateException("Non puoi accedere perché il tuo account non è attivo");
        }

        //creo e restituisco la sessione dell'utente autenticato
        return creaSessione(user);
    }

    /**
     * Crea la sessione per un utente autenticato.
     *
     * Il metodo converte l'utente di dominio in UtenteBean e genera
     * un token di sessione tramite UUID.
     */
    private SessioneUtenteBean creaSessione(GeneralUser user) {
        //creo il bean utente da inserire nella sessione
        UtenteBean ub = new UtenteBean(
                user.getNome(),
                user.getCognome(),
                user.getEmail(),
                user.getRuolo()
        );

        //creo la sessione con token univoco e timestamp corrente
        SessioneUtenteBean sessione =
                new SessioneUtenteBean(
                        UUID.randomUUID().toString(),
                        ub,
                        new Date()
                );

        log().log(Level.FINE, "[LOGIN] Sessione creata per {0}", user.getEmail());
        return sessione;
    }

    // STEP 2: salvataggio log accesso

    /**
     * Salva il log di accesso per l'utente in sessione.
     *
     * Il metodo:
     * - controlla che la sessione sia valida;
     * - recupera l'email dell'utente;
     * - risolve l'utente reale tramite DAO;
     * - crea un SystemLog;
     * - salva il log tramite LogDAO.
     */
    @Override
    public void saveLog(SessioneUtenteBean sessione) {
        //controllo che la sessione e l'utente siano presenti
        if (sessione == null || sessione.getUtente() == null) {
            return;
        }

        //recupero l'email dell'utente in sessione
        final String email = sessione.getUtente().getEmail();
        if (LogicControllerHelper.isBlank(email)) {
            return;
        }

        //normalizzo l'email prima della ricerca
        final String normEmail = LogicControllerHelper.normalizeEmail(email);
        if (normEmail == null) {
            return;
        }

        /*
         * Anche qui la risoluzione dell'utente viene demandata alla facade.
         * Il controller non conosce il DAO concreto da usare.
         */
        final GeneralUser user = userDAO().findByEmail(normEmail);
        if (user == null) {
            return;
        }

        //creo il log applicativo dell'accesso eseguito
        SystemLog sl = new SystemLog();
        sl.setTimestamp(LocalDateTime.from(ZonedDateTime.now(ZoneId.systemDefault())));
        sl.setTipoOperazione(TipoOperazione.ACCESSO_ESEGUITO);
        sl.setIdUtenteCoinvolto(user.getIdUtente());
        sl.setDescrizione("Login effettuato per utente " + email);

        //salvo il log tramite DAO
        logDAO().append(sl);

        log().log(Level.FINE, "[LOGIN-LOG] Log di accesso salvato per utenteId={0}", user.getIdUtente());
    }
}