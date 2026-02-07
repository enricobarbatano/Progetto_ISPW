package com.ispw.controller.graphic.abstracts;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.controller.graphic.GraphicControllerLog;
import com.ispw.controller.graphic.GraphicControllerLogUtils;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.logic.ctrl.LogicControllerGestioneAccount;

public abstract class AbstractGraphicControllerLog implements GraphicControllerLog {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: implementa GraphicControllerLog (interfaccia) e usa GraphicControllerNavigation.
    // A2) IO verso GUI/CLI: ritorna lista stringhe formattate.
    // A3) Logica delegata: usa LogicControllerGestioneAccount + GraphicControllerLogUtils.

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicControllerLog(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    protected abstract Logger log();

    protected List<String> listaUltimiLog(int limit) {
        return GraphicControllerLogUtils.formatLogs(
            new LogicControllerGestioneAccount().listaUltimiLog(limit));
    }

    protected abstract void goToHome();

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_LOGS;
    }

    @Override
    public void onShow(Map<String, Object> params) {
    }

    @Override
    public void richiediLog(int limit) {
        try {
            List<String> logs = listaUltimiLog(limit);
            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_LOGS,
                    Map.of(GraphicControllerUtils.KEY_LOGS, logs));
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore caricamento log", e);
        }
    }

    @Override
    public void tornaAllaHome() {
        goToHome();
    }

    // SEZIONE LOGICA
    // Legenda metodi: nessun helper privato.
}
