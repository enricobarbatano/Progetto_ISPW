package com.ispw.controller.logic.ctrl;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.DatiAccountBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.bean.UtenteBean;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaGestioneAccount;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.SystemLog;
import com.ispw.model.enums.StatoAccount;

/**
 * Controller applicativo principale per la "Gestione Account".
 * - Stateless; nessun riferimento a concreti.
 * - DIP by-parameter per la notifica (overload).
 * - Solo DAO via factory; nessun SQL nei controller.
 * - Early-return, messaggi centralizzati, logger on-demand (Sonar-friendly).
 */
public class LogicControllerGestioneAccount {

    // ========================
    // Messaggi centralizzati
    // ========================
    private static final String MSG_SESSIONE_KO       = "Sessione non valida";
    private static final String MSG_UTENTE_NOT_FOUND  = "Utente non trovato";
    private static final String MSG_DATI_KO           = "Dati non validi";
    private static final String MSG_EMAIL_DUP         = "Email già in uso";
    private static final String MSG_UPDATE_OK         = "Dati account aggiornati";
    private static final String MSG_PWD_KO            = "Password non valida";
    private static final String MSG_PWD_OLD_WRONG     = "Vecchia password errata";
    private static final String MSG_PWD_OK            = "Password aggiornata";

    // ========================
    // Logger on-demand (S1312)
    // ========================
    @SuppressWarnings("java:S1312")
    private Logger log() { return Logger.getLogger(getClass().getName()); }

    // ========================
    // DAO accessors (no concreti)
    // ========================
    private GeneralUserDAO userDAO() {
        return DAOFactory.getInstance().getGeneralUserDAO();
    }
    private LogDAO logDAO() {
        return DAOFactory.getInstance().getLogDAO();
    }

    // =====================================================================================
    // 1) Recupera informazioni account
    // =====================================================================================
    /** Ritorna i dati account dell'utente legato alla sessione; null se sessione/utente/email non validi o utente assente. */
    public DatiAccountBean recuperaInformazioniAccount(SessioneUtenteBean sessione) {
        if (sessione == null || sessione.getUtente() == null || isBlank(sessione.getUtente().getEmail())) {
            return null;
        }
        final String email = normEmail(sessione.getUtente().getEmail());
        final GeneralUser u = userDAO().findByEmail(email);
        if (u == null) return null;

        DatiAccountBean out = new DatiAccountBean();
        out.setIdUtente(u.getIdUtente());
        out.setNome(u.getNome());
        out.setEmail(u.getEmail());
        // Nota: GeneralUser non espone telefono/indirizzo → lasciamo null
        return out;
    }

    // =====================================================================================
    // 2) Aggiorna dati account
    // =====================================================================================
    /** Firma essenziale: aggiorna nome/email (telefono/indirizzo non gestiti perché assenti in GeneralUser). */
    public EsitoOperazioneBean aggiornaDatiAccount(DatiAccountBean nuovidati) {
        if (!isValid(nuovidati)) {
            return esito(false, MSG_DATI_KO);
        }
        final GeneralUser u = userDAO().findById(nuovidati.getIdUtente());
        if (u == null) {
            return esito(false, MSG_UTENTE_NOT_FOUND);
        }

        // Email: se presente e diversa, normalizza + verifica duplicato
        if (!isBlank(nuovidati.getEmail())) {
            final String target = normEmail(nuovidati.getEmail());
            final String curr   = normEmail(u.getEmail());
            if (!Objects.equals(curr, target)) {
                final GeneralUser dup = userDAO().findByEmail(target);
                if (dup != null && dup.getIdUtente() != u.getIdUtente()) {
                    return esito(false, MSG_EMAIL_DUP);
                }
                u.setEmail(target);
            }
        }

        // Nome (opzionale)
        if (!isBlank(nuovidati.getNome())) {
            u.setNome(nuovidati.getNome().trim());
        }

        // Telefono/Indirizzo NON presenti su GeneralUser → ignorati (non fallire)
        userDAO().store(u);
        appendLogSafe(u.getIdUtente(), "[ACCOUNT] Aggiornati dati account");
        return esito(true, MSG_UPDATE_OK);
    }

    /** Overload con notifica DIP: invia conferma aggiornamento se l'update va a buon fine. */
    public EsitoOperazioneBean aggiornaDatiAccount(DatiAccountBean nuovidati,
                                                   GestioneNotificaGestioneAccount notiCtrl) {
        EsitoOperazioneBean esito = aggiornaDatiAccount(nuovidati);
        if (esito.isSuccesso() && notiCtrl != null) {
            try {
                final GeneralUser u = userDAO().findById(nuovidati.getIdUtente());
                if (u != null) {
                    // Se GeneralUser non espone il cognome, puoi passare null o stringa vuota al posto di u.getCognome()
                    UtenteBean ub = new UtenteBean(u.getNome(), u.getCognome(), u.getEmail(), u.getRuolo());
                    notiCtrl.inviaConfermaAggiornamentoAccount(ub);
                }
            } catch (RuntimeException ex) {
                log().log(Level.FINE, "Notifica aggiornamento account fallita: {0}", ex.getMessage());
            }
        }
        return esito;
    }

    // =====================================================================================
    // 3) Cambia password
    // =====================================================================================
    /** Firma essenziale: cambia password, verificando la vecchia. */
    public EsitoOperazioneBean cambiaPassword(String vecchiaPwd, String nuovaPwd, SessioneUtenteBean sessione) {
        if (sessione == null || sessione.getUtente() == null || isBlank(sessione.getUtente().getEmail())) {
            return esito(false, MSG_SESSIONE_KO);
        }
        if (isBlank(vecchiaPwd) || isBlank(nuovaPwd) || nuovaPwd.trim().length() < 6) {
            return esito(false, MSG_PWD_KO);
        }

        final String email = normEmail(sessione.getUtente().getEmail());
        final GeneralUser u = userDAO().findByEmail(email);
        if (u == null) {
            return esito(false, MSG_UTENTE_NOT_FOUND);
        }

        final String currPwd = u.getPassword();
        if (currPwd == null || !currPwd.equals(vecchiaPwd)) {
            return esito(false, MSG_PWD_OLD_WRONG);
        }

        // In produzione: hashing; qui coerente con In-Memory
        u.setPassword(nuovaPwd.trim());
        userDAO().store(u);
        appendLogSafe(u.getIdUtente(), "[ACCOUNT] Password aggiornata");

        return esito(true, MSG_PWD_OK);
    }

    /** Overload con notifica DIP: invia conferma aggiornamento se il cambio va a buon fine. */
    public EsitoOperazioneBean cambiaPassword(String vecchiaPwd, String nuovaPwd,
                                              SessioneUtenteBean sessione,
                                              GestioneNotificaGestioneAccount notiCtrl) {
        EsitoOperazioneBean esito = cambiaPassword(vecchiaPwd, nuovaPwd, sessione);
        if (esito.isSuccesso() && notiCtrl != null) {
            try {
                final UtenteBean ub = sessione != null ? sessione.getUtente() : null;
                if (ub != null) {
                    notiCtrl.inviaConfermaAggiornamentoAccount(ub);
                }
            } catch (RuntimeException ex) {
                log().log(Level.FINE, "Notifica cambio password fallita: {0}", ex.getMessage());
            }
        }
        return esito;
    }

    // =====================================================================================
    // 4) Conferma modifica account (es. verifica via link email)
    // =====================================================================================
    /** Best-effort: porta lo stato a ATTIVO e logga; non lancia eccezioni al chiamante. */
    public void confermaModificaAccount(UtenteBean utente) {
        if (utente == null || isBlank(utente.getEmail())) return;

        final String email = normEmail(utente.getEmail());
        final GeneralUser u = userDAO().findByEmail(email);
        if (u == null) return;

        try {
            u.setStatoAccount(StatoAccount.ATTIVO);
            userDAO().store(u);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Aggiornamento stato account fallito: {0}", ex.getMessage());
        }

        appendLogSafe(u.getIdUtente(), "[ACCOUNT] Modifica account confermata");
    }

    // ========================
    // Helper (validazioni, logging)
    // ========================
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String normEmail(String s) {
        return isBlank(s) ? null : s.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isValid(DatiAccountBean b) {
    return b != null
        && b.getIdUtente() > 0
        && (b.getEmail() == null || !isBlank(b.getEmail()));
    }

    private EsitoOperazioneBean esito(boolean ok, String msg) {
        EsitoOperazioneBean e = new EsitoOperazioneBean();
        e.setSuccesso(ok);
        e.setMessaggio(msg);
        return e;
    }

    private void appendLogSafe(int idUtente, String descr) {
        try {
            SystemLog l = new SystemLog();
            l.setTimestamp(LocalDateTime.now());
            l.setIdUtenteCoinvolto(idUtente);
            l.setDescrizione(descr != null ? descr : "");
            logDAO().append(l);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Append log ACCOUNT fallito: {0}", ex.getMessage());
        }
    }
}