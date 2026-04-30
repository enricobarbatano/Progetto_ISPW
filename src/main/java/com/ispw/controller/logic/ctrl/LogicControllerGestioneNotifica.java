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

    private static final String UTENTE_NULL = "utente=null";

    // ===================== DISDETTA =====================

    @Override
    public void inviaConfermaCancellazione(UtenteBean utente, String dettaglio) {
        if (utente == null) {
            warn("Conferma cancellazione", UTENTE_NULL, dettaglio);
            return;
        }
        info("Conferma cancellazione", destinatario(utente), dettaglio);
    }

    // ===================== REGISTRAZIONE =====================

    @Override
    public void inviaConfermaRegistrazione(UtenteBean utente) {
        if (utente == null) {
            warn("Conferma registrazione", UTENTE_NULL, null);
            return;
        }
        info("Conferma registrazione", destinatario(utente), null);
    }

    // ===================== ACCOUNT =====================

    @Override
    public void inviaConfermaAggiornamentoAccount(UtenteBean utente) {
        if (utente == null) {
            warn("Aggiornamento account", UTENTE_NULL, null);
            return;
        }
        info("Aggiornamento account", destinatario(utente), null);
    }

    // ===================== PENALITA =====================

    @Override
    public void inviaNotificaPenalita(String idUtente) {
        final String dest = (idUtente == null || idUtente.trim().isEmpty())
                ? "idUtente=VUOTO"
                : "idUtente=" + idUtente.trim();
        info("Notifica penalita", dest, null);
    }

    // ===================== PRENOTAZIONE =====================

    @Override
    public void inviaConfermaPrenotazione(UtenteBean utente, String dettaglio) {
        if (utente == null) {
            warn("Conferma prenotazione", UTENTE_NULL, dettaglio);
            return;
        }
        info("Conferma prenotazione", destinatario(utente), dettaglio);
    }

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

    // ===================== REGOLE =====================

    @Override
    public void inviaNotificaAggiornamentoRegole() {
        info("Aggiornamento regole", "BROADCAST: utenti interessati", null);
    }

    // ===================== LOGICA =====================

    /** Log INFO standardizzato. */
    private void info(String tipo, String destinatario, String dettaglio) {
        final String msg = composeInfo("[NOTIFICA]", tipo, destinatario, dettaglio);
        log().log(Level.INFO, msg);
    }

    /** Log WARNING standardizzato. */
    private void warn(String contesto, String problema, String dettaglio) {
        final String msg = composeWarn("[NOTIFICA][WARN]", contesto, problema, dettaglio);
        log().log(Level.WARNING, msg);
    }

    private String composeInfo(String prefix, String a, String b, String maybeDettaglio) {
        final StringBuilder sb = new StringBuilder(96)
                .append(prefix).append(' ')
                .append(Objects.toString(a, ""))
                .append(" -> ")
                .append(Objects.toString(b, ""));
        if (maybeDettaglio != null && !maybeDettaglio.isBlank()) {
            sb.append(" | dettaglio=\"").append(maybeDettaglio).append('"');
        }
        sb.append(" ... inviato");
        return sb.toString();
    }

    private String composeWarn(String prefix, String a, String b, String maybeDettaglio) {
        final StringBuilder sb = new StringBuilder(96)
                .append(prefix).append(' ')
                .append(Objects.toString(a, ""))
                .append(" -> ")
                .append(Objects.toString(b, ""));
        if (maybeDettaglio != null && !maybeDettaglio.isBlank()) {
            sb.append(" | dettaglio=\"").append(maybeDettaglio).append('"');
        }
        // niente "riuscito" sui warning
        return sb.toString();
    }

    /** Descrittore leggibile del destinatario dal bean utente. */
    private String destinatario(UtenteBean utente) {
        if (utente == null) return UTENTE_NULL;

        // Se UtenteBean ha getter standard, li usiamo (senza cambiare dipendenze).
        // In caso contrario, fallback su toString().
        try {
            String email = utente.getEmail();
            if (email != null && !email.isBlank()) {
                return "email=" + email.trim();
            }
        } catch (RuntimeException ignore) {
            // fallback sotto
        }

        return utente.toString();
    }

    @SuppressWarnings("java:S1312")
    private Logger log() {
        return Logger.getLogger(getClass().getName());
    }
}
