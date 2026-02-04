package com.ispw.controller.graphic.cli;

import java.util.Map;

import com.ispw.bean.DatiLoginBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicLoginController;

public class CLIGraphicLoginController implements GraphicLoginController {
    
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
