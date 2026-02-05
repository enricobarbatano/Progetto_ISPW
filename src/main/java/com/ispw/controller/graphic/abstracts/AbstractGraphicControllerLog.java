package com.ispw.controller.graphic.abstracts;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.controller.graphic.GraphicControllerLog;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;

public abstract class AbstractGraphicControllerLog implements GraphicControllerLog {

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicControllerLog(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    protected abstract Logger log();

    protected abstract List<String> listaUltimiLog(int limit);

    protected abstract void goToHome();

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_LOGS;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        // no-op
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
}
