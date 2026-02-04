package com.ispw.controller.graphic;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Utility per i controller grafici: gestione messaggi standard e navigazione errori.
 */
public final class GraphicControllerUtils {

    public static final String KEY_ERROR = "error";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_SUCCESSO = "successo";
    public static final String KEY_RIEPILOGO = "riepilogo";
    public static final String KEY_PAGAMENTO = "pagamento";
    public static final String KEY_ID_PRENOTAZIONE = "idPrenotazione";
    public static final String KEY_IMPORTO_TOTALE = "importoTotale";
    public static final String KEY_PRENOTAZIONI = "prenotazioni";
    public static final String KEY_ANTEPRIMA = "anteprima";
    public static final String KEY_POSSIBILE = "possibile";
    public static final String KEY_PENALE = "penale";
    public static final String KEY_CAMPI = "campi";
    public static final String KEY_ID_CAMPO = "idCampo";
    public static final String KEY_DATI_ACCOUNT = "datiAccount";
    public static final String KEY_ID_UTENTE = "idUtente";
    public static final String KEY_NOME = "nome";
    public static final String KEY_COGNOME = "cognome";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_ATTIVO = "attivo";
    public static final String KEY_FLAG_MANUTENZIONE = "flagManutenzione";
    public static final String KEY_PREAVVISO_MINIMO_MINUTI = "preavvisoMinimoMinuti";
    public static final String KEY_DURATA_SLOT_MINUTI = "durataSlotMinuti";
    public static final String KEY_STATO = "stato";
    public static final String KEY_MESSAGGIO = "messaggio";
    public static final String KEY_ID_TRANSAZIONE = "idTransazione";
    public static final String KEY_DATA_PAGAMENTO = "dataPagamento";

    public static final String ROUTE_LOGIN = "login";
    public static final String ROUTE_HOME = "home";
    public static final String ROUTE_REGISTRAZIONE = "registrazione";
    public static final String ROUTE_ACCOUNT = "account";
    public static final String ROUTE_PRENOTAZIONE = "prenotazione";
    public static final String ROUTE_DISDETTA = "disdetta";
    public static final String ROUTE_REGOLE = "regole";
    public static final String ROUTE_PENALITA = "penalita";

    public static final String PREFIX_LOGIN = "[LOGIN]";
    public static final String PREFIX_REGISTRAZIONE = "[REGISTRAZIONE]";
    public static final String PREFIX_ACCOUNT = "[ACCOUNT]";
    public static final String PREFIX_PRENOT = "[PRENOT]";
    public static final String PREFIX_DISDETTA = "[DISDETTA]";
    public static final String PREFIX_PENALITA = "[PENALITA]";
    public static final String PREFIX_REGOLE = "[REGOLE]";

    private GraphicControllerUtils() {
        // utility class
    }

    public static void notifyError(Logger log,
                                   GraphicControllerNavigation navigator,
                                   String route,
                                   String prefix,
                                   String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        if (log != null) {
            log.warning(() -> prefix + " " + message);
        }
        if (navigator != null) {
            navigator.goTo(route, Map.of(KEY_ERROR, message));
        }
    }

    public static void handleOnShow(Logger log, Map<String, Object> params, String prefix) {
        if (params == null || params.isEmpty()) {
            return;
        }
        Object error = params.get(KEY_ERROR);
        if (error != null) {
            if (log != null) {
                log.warning(() -> prefix + " " + error);
            }
            return;
        }
        Object rawMessage = params.get(KEY_MESSAGE);
        if (rawMessage == null) {
            rawMessage = params.get(KEY_SUCCESSO);
        }
        if (rawMessage != null && log != null) {
            final String msg = String.valueOf(rawMessage);
            log.info(() -> prefix + " " + msg);
        }
    }
}
