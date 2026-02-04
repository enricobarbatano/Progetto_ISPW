package com.ispw.controller.graphic.gui;

import java.util.logging.Logger;

import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerAccount;

public class GUIGraphicControllerAccount extends AbstractGraphicControllerAccount {

    public GUIGraphicControllerAccount(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    @Override
    protected Logger log() { return Logger.getLogger(getClass().getName()); }

    @Override
    protected void goToLogin() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_LOGIN, null);
        }
    }

}
