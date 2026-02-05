package com.ispw.controller.logic.ctrl;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.UtenteBean;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaConfiguraRegole;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaDisdetta;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaGestioneAccount;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaPenalita;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaPrenotazione;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaRegistrazione;


public class LogicControllerGestioneNotifica implements
        GestioneNotificaConfiguraRegole,
        GestioneNotificaPrenotazione,
        GestioneNotificaDisdetta,
        GestioneNotificaRegistrazione,
        GestioneNotificaGestioneAccount,
        GestioneNotificaPenalita {

    // =======================
    //  NOTIFICHE DISDETTA
    // =======================
        private static final String UTENTE_NULL = "utente=null";
    /** Invia (simulato) conferma cancellazione prenotazione. */
    @Override
    public void inviaConfermaCancellazione(UtenteBean utente, String dettaglio) {
        
        if (utente == null) {
            warn("Conferma cancellazione", UTENTE_NULL, dettaglio);
            return;
        }
        info("Conferma cancellazione", destinatario(utente), dettaglio);
    }

    // =======================
    //  NOTIFICHE REGISTRAZIONE
    // =======================

    /** Invia (simulato) conferma registrazione account. */
    @Override
    public void inviaConfermaRegistrazione(UtenteBean utente) {
        if (utente == null) {
            warn("Conferma registrazione", UTENTE_NULL, null);
            return;
        }
        info("Conferma registrazione", destinatario(utente), null);
    }

    // =======================
    //  NOTIFICHE ACCOUNT
    // =======================

    /** Invia (simulato) conferma aggiornamento dati account. */
    @Override
    public void inviaConfermaAggiornamentoAccount(UtenteBean utente) {
        if (utente == null) {
            warn("Aggiornamento account", UTENTE_NULL, null);
            return;
        }
        info("Aggiornamento account", destinatario(utente), null);
    }

    // =======================
    //  NOTIFICHE PENALITÀ
    // =======================

    /** Invia (simulato) notifica penalità all'utente indicato (id in String). */
    @Override
    public void inviaNotificaPenalita(String idUtente) {
        final String dest = (idUtente == null || idUtente.trim().isEmpty())
                ? "idUtente=VUOTO"
                : "idUtente=" + idUtente.trim();
        info("Notifica penalità", dest, null);
    }

    // =======================
    //  NOTIFICHE PRENOTAZIONE
    // =======================

    /** Invia (simulato) conferma prenotazione con eventuale dettaglio. */
    @Override
    public void inviaConfermaPrenotazione(UtenteBean utente, String dettaglio) {
        if (utente == null) {
            warn("Conferma prenotazione", UTENTE_NULL, dettaglio);
            return;
        }
        info("Conferma prenotazione", destinatario(utente), dettaglio);
    }

    /**
     * Imposta (simulato) un promemoria X minuti prima della prenotazione.
     * Stampa su log una riga di scheduling (nessun job reale).
     */
    @Override
    public void impostaPromemoria(int idPrenotazione, int minutiAnticipo) {
        if (idPrenotazione <= 0) {
            warn("Imposta promemoria", "idPrenotazione non valido: " + idPrenotazione, null);
            return;
        }
        if (minutiAnticipo <= 0) {
            warn("Imposta promemoria", "minutiAnticipo non valido: " + minutiAnticipo, null);
            return;
        }
        log().log(Level.FINE,
            "[PROMEMORIA] Prenotazione#{0} -> scheduling promemoria {1} minuti prima ... riuscito",
            new Object[]{idPrenotazione, minutiAnticipo});
    }

    // =======================
    //  NOTIFICHE REGOLE
    // =======================

    /** Invia (simulato) broadcast di aggiornamento regolamenti. */
    @Override
    public void inviaNotificaAggiornamentoRegole() {
        info("Aggiornamento regole", "BROADCAST: utenti interessati", null);
    }

    // =======================
    //  Helper privati (stateless)
    // =======================

    /** Log INFO standardizzato. */
    private void info(String tipo, String destinatario, String dettaglio) {
        final String msg = compose("[NOTIFICA]", tipo, destinatario, dettaglio);
        log().fine(msg);
    }

    /** Log WARNING standardizzato. */
    private void warn(String contesto, String problema, String dettaglio) {
        final String msg = compose("[NOTIFICA][WARN]", contesto, problema, dettaglio);
        log().warning(msg);
    }

    /** Costruisce il messaggio evitando stringhe duplicate (S1192). */
    private String compose(String prefix, String a, String b, String maybeDettaglio) {
        final StringBuilder sb = new StringBuilder(96)
                .append(prefix).append(' ')
                .append(Objects.toString(a, ""))
                .append(" -> ")
                .append(Objects.toString(b, ""));
        if (maybeDettaglio != null && !maybeDettaglio.isBlank()) {
            sb.append(" | dettaglio=\"").append(maybeDettaglio).append('"');
        }
        sb.append(" ... riuscito");
        return sb.toString();
    }

    /** Descrittore leggibile del destinatario a partire dal bean utente. */
    private String destinatario(UtenteBean utente) {
        // Se UtenteBean espone getEmail()/getNome(), puoi raffinare:
        
        return (utente != null) ? utente.toString() : UTENTE_NULL;
    }

    @SuppressWarnings("java:S1312")
    private Logger log() {
        return Logger.getLogger(getClass().getName());
    }
}
