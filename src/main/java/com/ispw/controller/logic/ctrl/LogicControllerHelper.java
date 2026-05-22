package com.ispw.controller.logic.ctrl;

import java.util.Locale;

import com.ispw.bean.EsitoOperazioneBean;

/**
 * Utility class con piccoli helper comuni ai logic controller.
 *
 * Questa classe NON contiene logica di dominio.
 * Serve solo a evitare duplicazione di codice tecnico semplice, come:
 * - controllo stringhe vuote;
 * - normalizzazione email;
 * - creazione standard di EsitoOperazioneBean;
 * - gestione sicura di stringhe null.
 *
 * La classe è package-private perché deve essere usata solo dai controller
 * logici che si trovano nello stesso package.
 */
final class LogicControllerHelper {

    private LogicControllerHelper() {
        // Costruttore privato: questa è una utility class e non deve essere istanziata.
    }

    /**
     * Ritorna true se la stringa è null, vuota o composta solo da spazi.
     */
    static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Ritorna true se la stringa contiene almeno un carattere non spazio.
     *
     * È il contrario logico di isBlank(...).
     */
    static boolean hasText(String s) {
        return !isBlank(s);
    }

    /**
     * Normalizza una email:
     * - rimuove gli spazi laterali;
     * - converte in minuscolo;
     * - ritorna null se la stringa non contiene testo.
     */
    static String normalizeEmail(String email) {
        if (isBlank(email)) {
            return null;
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Crea un bean di esito standard.
     *
     * Questo evita di ripetere in ogni controller:
     * new EsitoOperazioneBean(), setSuccesso(...), setMessaggio(...).
     */
    static EsitoOperazioneBean esito(boolean ok, String msg) {
        EsitoOperazioneBean e = new EsitoOperazioneBean();
        e.setSuccesso(ok);
        e.setMessaggio(msg);
        return e;
    }

    /**
     * Ritorna una stringa vuota se il valore è null.
     *
     * Utile per log, descrizioni e messaggi dove non vogliamo concatenare null.
     */
    static String safe(String s) {
        return s == null ? "" : s;
    }

    static boolean isValidEmailFormat(String email) {
    return email != null && email.contains("@") && email.contains(".");
    }

}