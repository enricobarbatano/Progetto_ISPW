package com.ispw.controller.graphic.cli;

import java.util.logging.Logger;

import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerDisdetta;

public class CLIGraphicControllerDisdetta extends AbstractGraphicControllerDisdetta {
    
    public CLIGraphicControllerDisdetta(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    @SuppressWarnings("java:S1312")
    @Override
    protected Logger log() { return Logger.getLogger(getClass().getName()); }

    @Override
    protected void goToHome() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_HOME);
        }
    }

}
