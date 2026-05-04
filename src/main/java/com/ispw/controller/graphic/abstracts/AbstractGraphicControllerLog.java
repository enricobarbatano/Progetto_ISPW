package com.ispw.controller.graphic.abstracts;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.controller.graphic.interfaces.GraphicControllerLog;
import com.ispw.controller.graphic.interfaces.GraphicControllerLogUtils;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.logic.ctrl.LogicControllerGestioneAccount;

public abstract class AbstractGraphicControllerLog implements GraphicControllerLog {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: implementa GraphicControllerLog (interfaccia) e usa GraphicControllerNavigation.
    // A2) IO verso GUI/CLI: ritorna lista stringhe formattate.
    // A3) Logica delegata: usa LogicControllerGestioneAccount + GraphicControllerLogUtils.
    //
    // Vincolo responsabilità:
    // - nessuna logica di business/persistenza nel layer graphic
    // - il controller grafico richiama SOLO il logic controller e gestisce navigation/presentazione

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicControllerLog(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    protected abstract Logger log();

    protected abstract void goToHome();

    /** Hook per testabilità/override e coerenza con gli altri AbstractGraphicController* */
    protected LogicControllerGestioneAccount logicController() {
        return new LogicControllerGestioneAccount();
    }

    /** Recupera e formatta gli ultimi log (presentation logic). */
    protected List<String> listaUltimiLog(int limit) {
        return GraphicControllerLogUtils.formatLogs(
                logicController().listaUltimiLog(limit)
        );
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_LOGS;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        // Coerenza con altri controller: logga eventuali errori/successi passati come params
        GraphicControllerUtils.handleOnShow(log(), params, "[LOG]");
    }

    @Override
    public void richiediLog(int limit) {
        try {
            List<String> logs = listaUltimiLog(limit);
            if (navigator != null) {
                navigator.goTo(
                        GraphicControllerUtils.ROUTE_LOGS,
                        Map.of(GraphicControllerUtils.KEY_LOGS, logs)
                );
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore caricamento log", e);
            // Best-effort: resta nella stessa route e mostra errore
            if (navigator != null) {
                navigator.goTo(
                        GraphicControllerUtils.ROUTE_LOGS,
                        Map.of(GraphicControllerUtils.KEY_ERROR, "Errore caricamento log")
                );
            }
        }
    }

    @Override
    public void tornaAllaHome() {
        goToHome();
    }
}