package com.ispw.controller.logic.ctrl;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.UtenteBean;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaConfiguraRegole;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaDisdetta;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaGestioneAccount;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaPenalita;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaPrenotazione;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaRegistrazione;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.model.entity.GeneralUser;
import com.ispw.service.ExternalServiceFactory;
import com.ispw.service.interfaces.EmailNotification;

/**
 * Controller logico per la gestione delle notifiche.
 * Tutte le operazioni vengono eseguite in modo asincrono
 * per non bloccare il flusso principale dei casi d'uso.
 */
public class LogicControllerGestioneNotifica implements
        GestioneNotificaConfiguraRegole,
        GestioneNotificaPrenotazione,
        GestioneNotificaDisdetta,
        GestioneNotificaRegistrazione,
        GestioneNotificaGestioneAccount,
        GestioneNotificaPenalita {

    private static final String UTENTE_NULL = "utente=null";
    private static final String PREFIX_INFO = "[NOTIFICA]";
    private static final String PREFIX_WARN = "[NOTIFICA][WARN]";
    private static final String MSG_AGGIORNAMENTO_ACCOUNT = "Aggiornamento account";
    private static final String MSG_NOTIFICA_PENALITA = "Notifica penalita";
    private static final String MSG_CONFERMA_PRENOTAZIONE = "Conferma prenotazione";


    // Logger statico (best practice)
    private static final Logger LOGGER =
            Logger.getLogger(LogicControllerGestioneNotifica.class.getName());

    // Executor per esecuzione asincrona
    private final ExecutorService executor = Executors.newCachedThreadPool();

    // Service per invio email (attore esterno)
    private final EmailNotification emailService =
            ExternalServiceFactory.createEmailService();

    //DAO per per richiamare l'utente dal layer di persistenza
    private GeneralUserDAO userDAO(){ 
        return DAOFactory.getInstance().getGeneralUserDAO();
    }
    // ===================== DISDETTA =====================

    @Override
    public void inviaNotificaRichiestaDisdetta(UtenteBean gestore, String dettaglio) {
        async(() -> {
            if (gestore == null || gestore.getEmail() == null || gestore.getEmail().isBlank()) {
                warn("Notifica richiesta disdetta", "EMAIL GESTORE NON VALIDA", dettaglio);
                return;
            }

            info("Notifica richiesta disdetta", destinatario(gestore), dettaglio);

            emailService.sendNotification(
                    gestore.getEmail(),
                    "Nuova richiesta disdetta",
                    dettaglio
            );
        });
    }

    @Override
    public void inviaConfermaCancellazione(UtenteBean utente, String dettaglio) {
        async(() -> {
            if (utente == null) {
                warn("Conferma cancellazione", UTENTE_NULL, dettaglio);
                return;
            }

            info("Conferma cancellazione", destinatario(utente), dettaglio);

            // invio email reale
            emailService.sendNotification(
                    utente.getEmail(),
                    "Cancellazione prenotazione",
                    "La tua prenotazione è stata cancellata. Dettagli: " + dettaglio
            );
        });
    }

    // ===================== REGISTRAZIONE =====================

    @Override
    public void inviaConfermaRegistrazione(UtenteBean utente) {
        async(() -> {
            if (utente == null) {
                warn("Conferma registrazione", UTENTE_NULL, null);
                return;
            }

            info("Conferma registrazione", destinatario(utente), null);

            emailService.sendNotification(
                    utente.getEmail(),
                    "Registrazione completata",
                    "La tua registrazione all'applicazione SportBooking è avvenuta con successo."
            );
        });
    }

    // ===================== ACCOUNT =====================

    
    @Override
    public void inviaConfermaAggiornamentoAccount(UtenteBean utente) {
        async(() -> {
            if (utente == null) {
                warn(MSG_AGGIORNAMENTO_ACCOUNT, UTENTE_NULL, null);
                return;
            }

            if (utente.getEmail() == null || utente.getEmail().isBlank()) {
                warn(MSG_AGGIORNAMENTO_ACCOUNT, "EMAIL NON VALIDA", null);
                return;
            }

            info("MSG_AGGIORNAMENTO_ACCOUNT", destinatario(utente), null);

            emailService.sendNotification(
                    utente.getEmail(),
                    "MSG_AGGIORNAMENTO_ACCOUNT",
                    "I tuoi dati sono stati aggiornati con successo."
            );
        });
    }



    // ===================== PENALITA =====================

    @Override
    public void inviaNotificaPenalita(String idUtente) {
        async(() -> {

            // controllo input stringa
            if (idUtente == null || idUtente.trim().isEmpty()) {
                warn(MSG_NOTIFICA_PENALITA, "idUtente=VUOTO", null);
                return;
            }

            try {
                // conversione da String a int (dato che DAO usa int)
                int id = Integer.parseInt(idUtente.trim());

                // recupero utente dal DAO
                GeneralUser user = userDAO().findById(id);

                if (user == null) {
                    warn(MSG_NOTIFICA_PENALITA, "UTENTE NON TROVATO: id=" + id, null);
                    return;
                }

                // controllo email
                if (user.getEmail() == null || user.getEmail().isBlank()) {
                    warn(MSG_NOTIFICA_PENALITA, "EMAIL NON VALIDA per id=" + id, null);
                    return;
                }

                // log informativo
                info(MSG_NOTIFICA_PENALITA, "email=" + user.getEmail(), null);

                // invio email reale
                emailService.sendNotification(
                        user.getEmail(),
                        "MSG_NOTIFICA_PENALITA",
                        "Hai ricevuto una penalità nel sistema."
                );

            } catch (NumberFormatException e) {
                //idUtente non convertibile in intero
                warn("MSG_NOTIFICA_PENALITA", "ID NON NUMERICO: " + idUtente, null);

            } catch (Exception e) {
                // errore generico
                LOGGER.log(Level.SEVERE, "Errore invio MSG_NOTIFICA_PENALITA", e);
            }
        });
    }

    // ===================== PRENOTAZIONE =====================

    @Override
    public void inviaConfermaPrenotazione(UtenteBean utente, String dettaglio) {
        async(() -> {
            if (utente == null) {
                warn(MSG_CONFERMA_PRENOTAZIONE, UTENTE_NULL, dettaglio);
                return;
            }

            info("MSG_CONFERMA_PRENOTAZIONE", destinatario(utente), dettaglio);

            emailService.sendNotification(
                    utente.getEmail(),
                    "MSG_CONFERMA_PRENOTAZIONE",
                    "Prenotazione confermata. Dettagli: " + dettaglio
            );
        });
    }

    @Override
    public void impostaPromemoria(int idPrenotazione, int minutiAnticipo) {
        async(() -> {
            if (idPrenotazione <= 0) {
                warn("Imposta promemoria",
                        "idPrenotazione non valido: " + idPrenotazione, null);
                return;
            }

            if (minutiAnticipo <= 0) {
                warn("Imposta promemoria",
                        "minutiAnticipo non valido: " + minutiAnticipo, null);
                return;
            }

            LOGGER.log(Level.FINE,
                    "[PROMEMORIA] Prenotazione#{0} -> scheduling promemoria {1} minuti prima ... riuscito",
                    new Object[]{idPrenotazione, minutiAnticipo});
        });
    }

    // ===================== REGOLE =====================

    @Override
    public void inviaNotificaAggiornamentoRegole() {
        async(() -> {
            info("Aggiornamento regole", "BROADCAST: utenti interessati", null);

            try {
                for (GeneralUser user : userDAO().findAll()) {

                    if (user.getEmail() != null && !user.getEmail().isBlank()) {
                        emailService.sendNotification(
                                user.getEmail(),
                                "Aggiornamento regole",
                                "Le regole del sistema sono state aggiornate."
                        );
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Errore invio broadcast", e);
            }
        });
    }


    // ===================== LOGICA =====================

    /**
     * Log informativo standardizzato.
     */
    private void info(String tipo, String destinatario, String dettaglio) {
        final String msg = composeInfo(PREFIX_INFO, tipo, destinatario, dettaglio);
        LOGGER.log(Level.INFO, msg);
    }

    /**
     * Log di warning standardizzato.
     */
    private void warn(String contesto, String problema, String dettaglio) {
        final String msg = composeWarn(PREFIX_WARN, contesto, problema, dettaglio);
        LOGGER.log(Level.WARNING, msg);
    }

    /**
     * Costruzione messaggio informativo.
     */
    private String composeInfo(String prefix, String a, String b, String dettaglio) {
        final StringBuilder sb = new StringBuilder(96)
                .append(prefix).append(' ')
                .append(Objects.toString(a, ""))
                .append(" -> ")
                .append(Objects.toString(b, ""));

        if (dettaglio != null && !dettaglio.isBlank()) {
            sb.append(" | dettaglio=\"").append(dettaglio).append('"');
        }

        sb.append(" ... inviato");
        return sb.toString();
    }

    /**
     * Costruzione messaggio warning.
     */
    private String composeWarn(String prefix, String a, String b, String dettaglio) {
        final StringBuilder sb = new StringBuilder(96)
                .append(prefix).append(' ')
                .append(Objects.toString(a, ""))
                .append(" -> ")
                .append(Objects.toString(b, ""));

        if (dettaglio != null && !dettaglio.isBlank()) {
            sb.append(" | dettaglio=\"").append(dettaglio).append('"');
        }

        return sb.toString();
    }

    /**
     * Restituisce il destinatario leggibile.
     */
    private String destinatario(UtenteBean utente) {
        if (utente == null) return UTENTE_NULL;

        try {
            String email = utente.getEmail();
            if (email != null && !email.isBlank()) {
                return "email=" + email.trim();
            }
        } catch (RuntimeException ignore) {
            // fallback
        }

        return utente.toString();
    }

    /**
     * Wrapper per esecuzione asincrona tramite thread pool.
     */
    private void async(Runnable task) {
        executor.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Errore task asincrono", e);
            }
        });
    }
}