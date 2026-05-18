package com.ispw.controller.graphic.abstracts;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.NavigableController;

/**
 * Controller astratto di navigazione.
 *
 * Questa classe gestisce il routing comune tra GUI e CLI:
 * - registra le route disponibili;
 * - mantiene la route corrente;
 * - mantiene uno storico per il back;
 * - richiama onShow(...) sul controller/view associato alla route.
 *
 * Nota di progetto:
 * il navigator evita che i controller grafici conoscano direttamente
 * le view concrete. I controller chiedono solo di andare a una route.
 */
public abstract class AbstractGraphicControllerNavigation implements GraphicControllerNavigation {

    // Mappa route -> controller/view navigabile
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    protected final Map<String, NavigableController> routes = new HashMap<>();

    // Storico delle route visitate
    protected final Deque<String> history = new ArrayDeque<>();

    // Route attualmente mostrata
    protected String currentRoute;

    /**
     * Registra una route associandola a un controller/view navigabile.
     */
    public void registerRoute(String route, NavigableController controller) {
        if (route == null || controller == null) {
            return;
        }

        routes.put(route, controller);
    }

    /**
     * Naviga verso una route passando eventuali parametri.
     *
     * Il metodo:
     * - controlla che la route sia valida;
     * - recupera il controller associato;
     * - aggiorna lo storico;
     * - imposta la route corrente;
     * - chiama onShow(...) sul controller di destinazione.
     */
    @Override
    public void goTo(String route, Map<String, Object> params) {
        if (route == null) {
            return;
        }

        NavigableController controller = routes.get(route);
        if (controller == null) {
            return;
        }

        if (currentRoute != null && !currentRoute.equals(route)) {
            history.push(currentRoute);
        }

        currentRoute = route;
        controller.onShow(params);
    }

    /**
     * Torna alla route precedente, se presente nello storico.
     */
    @Override
    public void back() {
        if (history.isEmpty()) {
            return;
        }
        //carica la route precendente tramite .pop di history che prima carica e poi rimuove la stringa presentein cima alla coda
        String previous = history.pop();
        //recupera il controller associato alla route precedente
        NavigableController controller = routes.get(previous);

        if (controller == null) {
            return;
        }
        
        //aggiorno correntRoute e chiamo onShow sul controller di destinazione
        currentRoute = previous;
        controller.onShow(Map.of());
    }

    /**
     * Termina l'applicazione.
     */
    @Override
    public void exit() {
        System.exit(0);
    }
}