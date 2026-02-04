package com.ispw.controller.graphic.gui;

import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerAccount;
import com.ispw.controller.graphic.GraphicControllerNavigation;

public class GUIGraphicControllerAccount implements GraphicControllerAccount {
    
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
    public void loadAccount() {
    }

    @Override
    public void aggiornaDatiAccount(Map<String, Object> nuoviDati) {
    }

    @Override
    public void cambiaPassword(String vecchiaPassword, String nuovaPassword) {
    }

    @Override
    public void logout() {
    }
}
