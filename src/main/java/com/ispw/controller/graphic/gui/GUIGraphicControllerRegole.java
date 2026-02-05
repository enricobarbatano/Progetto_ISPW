package com.ispw.controller.graphic.gui;

import java.util.logging.Logger;

import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.PenalitaBean;
import com.ispw.bean.RegolaCampoBean;
import com.ispw.bean.TempisticheBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerRegole;
import com.ispw.controller.logic.ctrl.LogicControllerConfiguraRegole;

public class GUIGraphicControllerRegole extends AbstractGraphicControllerRegole {
    
    public GUIGraphicControllerRegole(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    @SuppressWarnings("java:S1312")
    @Override
    protected Logger log() { return Logger.getLogger(getClass().getName()); }

    @Override
    protected void goToHome() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_HOME, null);
        }
    }

    @Override
    protected java.util.List<String> listaCampi() {
        return new LogicControllerConfiguraRegole().listaCampi();
    }

    @Override
    protected EsitoOperazioneBean aggiornaRegoleCampo(RegolaCampoBean bean) {
        return new LogicControllerConfiguraRegole().aggiornaRegoleCampo(bean);
    }

    @Override
    protected EsitoOperazioneBean aggiornaRegolaTempistiche(TempisticheBean bean) {
        return new LogicControllerConfiguraRegole().aggiornaRegolaTempistiche(bean);
    }

    @Override
    protected EsitoOperazioneBean aggiornaRegolepenalita(PenalitaBean bean) {
        return new LogicControllerConfiguraRegole().aggiornaRegolepenalita(bean);
    }

}
