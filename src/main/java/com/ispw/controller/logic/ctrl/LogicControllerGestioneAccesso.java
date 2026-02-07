package com.ispw.controller.logic.ctrl;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

import com.ispw.bean.DatiLoginBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.bean.UtenteBean;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.SystemLog;
import com.ispw.model.enums.StatoAccount;
import com.ispw.model.enums.TipoOperazione;

public class LogicControllerGestioneAccesso {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: usa interfacce DAO via DAOFactory (DIP).
    // A2) IO verso GUI/CLI: riceve DatiLoginBean, ritorna SessioneUtenteBean.
    // A3) Persistenza: usa DAO per utenti e log.

    /**
     * Verifica credenziali e, se valide, costruisce una SessioneUtenteBean (stateless).
     * Requisiti:
     *  - email/password non vuote
     *  - utente esistente
     *  - password coincidente (in questa fase plain, in seguito con hashing)
     *  - stato account == ATTIVO
     */
    public SessioneUtenteBean verificaCredenziali(DatiLoginBean datiLogin) {
        if (datiLogin == null) return null;

        final String email = datiLogin.getEmail();
        final String password = datiLogin.getPassword();
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return null;
        }

        // Convenzione: normalizziamo l'email a lowercase e trimmiamo
        final GeneralUser user = userDAO().findByEmail(email.trim().toLowerCase());
        if (user == null) return null;

        // Password (fase 1: plain compare; fase security: hashing e costante-time compare)
        if (!Objects.equals(user.getPassword(), password)) return null;

        // Consentito solo se attivo
        if (user.getStatoAccount() != StatoAccount.ATTIVO) {
            throw new IllegalStateException("Non puoi accedere perchÃ¨ sei sospeso");
        }

        // Costruzione UtenteBean (cognome assente nel modello â†’ stringa vuota)
        final UtenteBean ub = new UtenteBean(
                user.getNome(),
                user.getCognome(),
                user.getEmail(),
                user.getRuolo()
        );

        final SessioneUtenteBean sessione =
                new SessioneUtenteBean(UUID.randomUUID().toString(), ub, new Date());

        log().fine(() -> "[LOGIN] Sessione creata per " + user.getEmail());
        return sessione;
    }

    /**
     * Salva il log di accesso per l'utente in sessione.
     * Non modifica stato, nessun I/O esterno reale â†’ stateless friendly.
     * Se l'utente non Ã¨ risolvibile via email, la funzione Ã¨ no-op.
     */
    public void saveLog(SessioneUtenteBean sessione) {
        if (sessione == null || sessione.getUtente() == null) return;

        final String email = sessione.getUtente().getEmail();
        if (email == null || email.isBlank()) return;

        // Recuperiamo l'id utente (UtenteBean non contiene l'id)
        final GeneralUser user = userDAO().findByEmail(email.trim().toLowerCase());
        if (user == null) return;

        final LogDAO logDAO = DAOFactory.getInstance().getLogDAO();

        final SystemLog sl = new SystemLog();
        sl.setTimestamp(LocalDateTime.now());

        sl.setTipoOperazione(TipoOperazione.ACCESSO_ESEGUITO);

        sl.setIdUtenteCoinvolto(user.getIdUtente());
        sl.setDescrizione("Login effettuato per utente " + email);

        logDAO.append(sl);
        log().fine(() -> "[LOGIN-LOG] Log di accesso salvato per utenteId=" + user.getIdUtente());
    }

    // SEZIONE LOGICA
    // Legenda metodi:
    // 1) log() - logger on-demand.
    // 2) userDAO() - accesso DAO.

    // Logger on-demand (niente campo statico) â€” SonarCloud: soppressione locale S1312
    @SuppressWarnings("java:S1312")
    private Logger log() {
        return Logger.getLogger(getClass().getName());
    }

    // DAO on-demand (no SQL nei controller)
    private GeneralUserDAO userDAO() {
        return DAOFactory.getInstance().getGeneralUserDAO();
    }
}
