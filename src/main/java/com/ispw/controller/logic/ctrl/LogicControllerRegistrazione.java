
package com.ispw.controller.logic.ctrl;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.DatiRegistrazioneBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.UtenteBean;
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

public class LogicControllerRegistrazione {

    public EsitoOperazioneBean registraNuovoUtente(DatiRegistrazioneBean datiInput) {
        return registraNuovoUtente(datiInput, new LogicControllerGestioneNotifica());
    }

    EsitoOperazioneBean registraNuovoUtente(DatiRegistrazioneBean datiInput,
                                            GestioneNotificaRegistrazione notificaCtrl) {
        final EsitoOperazioneBean esito = new EsitoOperazioneBean();

        if (!isValid(datiInput) || notificaCtrl == null) {
            esito.setSuccesso(false);
            esito.setMessaggio("Dati registrazione o servizio notifica non validi");
            log().warning("[REG] Input non valido per registrazione");
            return esito;
        }

        // ✅ normalizzazione email (senza cambiare import)
        final String emailNorm = datiInput.getEmail().trim().toLowerCase();

        // 1) Unicità email via DAO (facade aggregatrice)
        final GeneralUserDAO userDAO = generalUserDAO();
        final GeneralUser existing = userDAO.findByEmail(emailNorm);
        if (existing != null) {
            esito.setSuccesso(false);
            esito.setMessaggio("Email gia registrata");
            log().log(Level.WARNING, "[REG] Email gia presente: {0}", emailNorm);
            return esito;
        }

        // 2) Creazione utente finale
        final UtenteFinale nuovo = new UtenteFinale();
        nuovo.setNome(datiInput.getNome());
        nuovo.setCognome(datiInput.getCognome());
        nuovo.setEmail(emailNorm);                 // ✅ salva normalizzata
        nuovo.setPassword(datiInput.getPassword());
        nuovo.setStatoAccount(StatoAccount.DA_CONFERMARE);
        nuovo.setRuolo(Ruolo.UTENTE);

        // store instradato dalla facade verso UtenteFinaleDAO
        userDAO.store(nuovo);

        // 3) Log registrazione
        appendLog(nuovo.getIdUtente(),
                TipoOperazione.REGISTRAZIONE_ACCOUNT,
                "Registrazione avviata; richiesta conferma inviata");

        // 4) Notifica
        notificaCtrl.inviaConfermaRegistrazione(toBean(nuovo));

        // 4b) Simula conferma immediata
        nuovo.setStatoAccount(StatoAccount.ATTIVO);
        userDAO.store(nuovo);

        appendLog(nuovo.getIdUtente(),
            TipoOperazione.ACCOUNT_ATTIVATO,
            "Conferma registrazione completata (simulata)");

        // 5) Esito
        esito.setSuccesso(true);
        esito.setMessaggio("Registrazione avviata. Controlla la tua email per la conferma.");
        log().log(Level.INFO, "[REG] Registrazione creata per utente #{0} ({1})",
                new Object[]{nuovo.getIdUtente(), nuovo.getEmail()});
        return esito;
    }

    public void confermaNuovoAccount(UtenteBean utente) {
        if (utente == null || !hasText(utente.getEmail())) {
            log().warning("[REG-CONF] UtenteBean nullo o email vuota");
            return;
        }

        final String emailNorm = utente.getEmail().trim().toLowerCase();

        final GeneralUserDAO userDAO = generalUserDAO();
        final GeneralUser u = userDAO.findByEmail(emailNorm);
        if (u == null) {
            log().log(Level.WARNING, "[REG-CONF] Nessun utente per email: {0}", emailNorm);
            return;
        }

        u.setStatoAccount(StatoAccount.ATTIVO);
        userDAO.store(u);

        appendLog(u.getIdUtente(),
                TipoOperazione.ACCOUNT_ATTIVATO,
                "Conferma registrazione completata");
        log().log(Level.INFO, "[REG-CONF] Account confermato per utente #{0}", u.getIdUtente());
    }

    public void finalizzaAttivazioneAccount(int idUtente) {
        if (idUtente <= 0) {
            log().warning("[REG-FINAL] idUtente non valido");
            return;
        }

        final GeneralUserDAO userDAO = generalUserDAO();
        final GeneralUser u = userDAO.findById(idUtente);
        if (u == null) {
            log().log(Level.WARNING, "[REG-FINAL] Utente non trovato: {0}", idUtente);
            return;
        }

        u.setStatoAccount(StatoAccount.ATTIVO);
        userDAO.store(u);

        appendLog(u.getIdUtente(),
                TipoOperazione.ACCOUNT_ATTIVATO,
                "Attivazione account completata");
        log().log(Level.INFO, "[REG-FINAL] Account attivato per utente #{0}", idUtente);
    }

    private void appendLog(int idUtente, TipoOperazione tipo, String descrizione) {
        final SystemLog log = new SystemLog();
        log.setTimestamp(LocalDateTime.now());
        log.setTipoOperazione(tipo);
        log.setIdUtenteCoinvolto(idUtente);
        log.setDescrizione(descrizione);
        logDAO().append(log);
    }

    private GeneralUserDAO generalUserDAO() { return DAOFactory.getInstance().getGeneralUserDAO(); }
    private LogDAO logDAO() { return DAOFactory.getInstance().getLogDAO(); }

    @SuppressWarnings("java:S1312")
    private Logger log() { return Logger.getLogger(getClass().getName()); }

    private boolean isValid(DatiRegistrazioneBean b) {
        return b != null && hasText(b.getNome()) && hasText(b.getCognome())
                && hasText(b.getEmail()) && hasText(b.getPassword());
    }

    private boolean hasText(String s) { return s != null && !s.trim().isEmpty(); }

    //Fix: include cognome (senza cambiare import)
    private UtenteBean toBean(GeneralUser u) {
        return new UtenteBean(
                Objects.toString(u.getNome(), null),
                Objects.toString(u.getCognome(), null),
                u.getEmail(),
                u.getRuolo()
        );
    }
}
