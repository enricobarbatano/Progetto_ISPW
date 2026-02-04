package com.ispw.controller.graphic.gui;

import java.util.Map;

import com.ispw.bean.DatiLoginBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicLoginController;

public class GUIGraphicLoginController implements GraphicLoginController {
    
    @Override
    public String getRouteName() {
        return null;
    }

    @Override
    public void setNavigator(GraphicControllerNavigation navigator) {
    }

    @Override
    public void onShow(Map<String, Object> params) {
    }

    @Override
    public void effettuaLogin(DatiLoginBean credenziali) {
    }

    @Override
    public void logout() {
    }

    @Override
    public void vaiARegistrazione() {
    }

    @Override
    public void vaiAHome() {
    }
}
