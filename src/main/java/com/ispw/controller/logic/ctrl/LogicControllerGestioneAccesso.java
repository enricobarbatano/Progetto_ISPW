package com.ispw.controller.logic.ctrl;


import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.bean.DatiLoginBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.bean.UtenteBean;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.enums.StatoAccount;

import java.util.Date;
import java.util.UUID;

public class LogicControllerGestioneAccesso {

    private final GeneralUserDAO userDAO;

    public LogicControllerGestioneAccesso(GeneralUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public SessioneUtenteBean verificaCredenziali(DatiLoginBean datiLogin) {
        if (datiLogin == null || datiLogin.getEmail() == null || datiLogin.getPassword() == null) {
            return null;
        }

        GeneralUser user = userDAO.findByEmail(datiLogin.getEmail());
        if (user == null) return null;

        // verifica password
        if (!user.getPassword().equals(datiLogin.getPassword())) return null;

        // esempio minimo: consentire login solo se ATTIVO
        if (user.getStatoAccount() != StatoAccount.ATTIVO) return null;

        // costruisci UtenteBean come nel diagramma (nome/cognome non esiste in GeneralUser UML: metto cognome vuoto)
        UtenteBean ub = new UtenteBean(
                user.getNome(),
                "",                 // cognome non presente in GeneralUser nel diagramma
                user.getEmail(),
                user.getRuolo()
        );

        // idSessione = String (UUID)
        String idSessione = UUID.randomUUID().toString();
        Date ts = new Date();

        return new SessioneUtenteBean(idSessione, ub, ts);
    }

    public Boolean logout(SessioneUtenteBean sessione) {
        if (sessione == null) return false;
        // puoi invalidare la sessione “svuotandola” o gestendo una session store
        sessione.setIdSessione(null);
        return true;
    }
}
