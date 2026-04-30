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
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.SystemLog;
import com.ispw.model.enums.StatoAccount;
import com.ispw.model.enums.TipoOperazione;

/**
 * Logic controller per la gestione dell'accesso.
 *
 * Regola architetturale:
 * - la logica di risoluzione (Gestore -> UtenteFinale) è demandata alla Facade GeneralUserDAO
 *   (che a runtime è AggregatingGeneralUserDAO via DAOFactory).
 */
public class LogicControllerGestioneAccesso {

    /**
     * Verifica le credenziali e, se valide, crea una SessioneUtenteBean.
     *
     * @return SessioneUtenteBean se credenziali valide, null altrimenti
     */
    public SessioneUtenteBean verificaCredenziali(DatiLoginBean datiLogin) {
        if (datiLogin == null) return null;

        final String email = datiLogin.getEmail();
        final String password = datiLogin.getPassword();

        if (email == null || email.isBlank() ||
            password == null || password.isBlank()) {
            return null;
        }

        final String normEmail = email.trim().toLowerCase();

        // Risoluzione utente demandata alla Facade (AggregatingGeneralUserDAO)
        // NB: uso var per non cambiare/importare GeneralUserDAO
        var userDAO = DAOFactory.getInstance().getGeneralUserDAO();
        final GeneralUser user = userDAO.findByEmail(normEmail);
        if (user == null) return null;

        // Password (fase 1: plain compare; fase security: hashing)
        if (!Objects.equals(user.getPassword(), password)) return null;

        // Consentito solo se attivo
        if (user.getStatoAccount() != StatoAccount.ATTIVO) {
            throw new IllegalStateException("Non puoi accedere perché il tuo account non è attivo");
        }

        return creaSessione(user);
    }

    /**
     * Crea la sessione per un utente autenticato.
     */
    private SessioneUtenteBean creaSessione(GeneralUser user) {
        UtenteBean ub = new UtenteBean(
                user.getNome(),
                user.getCognome(),
                user.getEmail(),
                user.getRuolo()
        );

        SessioneUtenteBean sessione =
                new SessioneUtenteBean(
                        UUID.randomUUID().toString(),
                        ub,
                        new Date()
                );

        log().fine("[LOGIN] Sessione creata per " + user.getEmail());
        return sessione;
    }

    /**
     * Salva il log di accesso per l'utente in sessione.
     */
    public void saveLog(SessioneUtenteBean sessione) {
        if (sessione == null || sessione.getUtente() == null) return;

        final String email = sessione.getUtente().getEmail();
        if (email == null || email.isBlank()) return;

        final String normEmail = email.trim().toLowerCase();

        // Anche qui risoluzione demandata alla Facade
        var userDAO = DAOFactory.getInstance().getGeneralUserDAO();
        final GeneralUser user = userDAO.findByEmail(normEmail);
        if (user == null) return;

        LogDAO logDAO = DAOFactory.getInstance().getLogDAO();

        SystemLog sl = new SystemLog();
        sl.setTimestamp(LocalDateTime.now());
        sl.setTipoOperazione(TipoOperazione.ACCESSO_ESEGUITO);
        sl.setIdUtenteCoinvolto(user.getIdUtente());
        sl.setDescrizione("Login effettuato per utente " + email);

        logDAO.append(sl);

        // niente lambda -> nessun problema di "effectively final"
        log().fine("[LOGIN-LOG] Log di accesso salvato per utenteId=" + user.getIdUtente());
    }

    @SuppressWarnings("java:S1312")
    private Logger log() {
        return Logger.getLogger(getClass().getName());
    }
}
