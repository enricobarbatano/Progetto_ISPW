package com.ispw.view.shared;

import java.util.List;
import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.model.enums.Ruolo;

/**
 * Utility condivisa per le view che mostrano log.
 *
 * Responsabilità:
 * - verificare se la sessione appartiene a un gestore;
 * - leggere la lista dei log dai parametri di navigazione;
 * - normalizzare i log in una lista di stringhe.
 *
 * NON contiene:
 * - logica di business;
 * - logica DAO;
 * - logica specifica CLI o GUI.
 */
public final class LogViewUtils {

    /**
     * Costruttore privato.
     *
     * La classe contiene solo metodi statici,
     * quindi non deve essere istanziata.
     */
    private LogViewUtils() {
        // Utility class: nessuna istanza necessaria.
    }

    /**
     * Verifica se la sessione corrente appartiene a un utente gestore.
     *
     * @param sessione sessione corrente
     * @return true se la sessione è valida e il ruolo è GESTORE
     */
    public static boolean isGestore(SessioneUtenteBean sessione) {
        return sessione != null
                && sessione.getUtente() != null
                && sessione.getUtente().getRuolo() == Ruolo.GESTORE;
    }

    /**
     * Estrae i log dai parametri della view.
     *
     * Se i parametri sono null oppure se la chiave dei log non contiene una lista,
     * viene restituita una lista vuota.
     *
     * Questo risolve lo smell S1168:
     * i metodi che restituiscono collezioni non devono restituire null.
     *
     * @param params parametri di navigazione
     * @return lista di log convertiti in stringa, oppure lista vuota
     */
    public static List<String> readLogs(Map<String, Object> params) {
        if (params == null) {
            return List.of();
        }

        Object raw = params.get(GraphicControllerUtils.KEY_LOGS);

        if (!(raw instanceof List<?> logs)) {
            return List.of();
        }

        return logs.stream()
                .map(String::valueOf)
                .toList();
    }
}