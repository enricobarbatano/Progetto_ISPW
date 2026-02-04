package com.ispw.controller.graphic.cli;

import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerRegistrazione;

public class CLIGraphicControllerRegistrazione implements GraphicControllerRegistrazione {
    
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
    public void inviaDatiRegistrazione(Map<String, Object> datiRegistrazione) {
    }

    @Override
    public void vaiAlLogin() {
    }
}
