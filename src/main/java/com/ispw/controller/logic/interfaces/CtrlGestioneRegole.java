package com.ispw.controller.logic.interfaces;


import com.ispw.bean.CampiBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.PenalitaBean;
import com.ispw.bean.RegolaCampoBean;
import com.ispw.bean.TempisticheBean;


public interface CtrlGestioneRegole {
    

 CampiBean listaCampi();

    EsitoOperazioneBean aggiornaRegoleCampo(RegolaCampoBean bean);

    EsitoOperazioneBean eseguiManutenzione(RegolaCampoBean bean);

    EsitoOperazioneBean aggiornaRegolaTempistiche(TempisticheBean bean);

    EsitoOperazioneBean aggiornaRegolepenalita(PenalitaBean bean);

}
