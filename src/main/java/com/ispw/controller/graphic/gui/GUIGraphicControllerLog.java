package com.ispw.controller.graphic.gui;

import java.util.List;
import java.util.logging.Logger;

import com.ispw.controller.graphic.GraphicControllerLogUtils;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerLog;
import com.ispw.controller.logic.ctrl.LogicControllerGestioneAccount;

public class GUIGraphicControllerLog extends AbstractGraphicControllerLog {

    public GUIGraphicControllerLog(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    @SuppressWarnings("java:S1312")
    @Override
    protected Logger log() { return Logger.getLogger(getClass().getName()); }

    @Override
    protected List<String> listaUltimiLog(int limit) {
        return GraphicControllerLogUtils.formatLogs(
            new LogicControllerGestioneAccount().listaUltimiLog(limit));
    }

    @Override
    protected void goToHome() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_HOME, null);
        }
    }
}
