package com.ispw.controller.logic.interfaces.disponibilita;

import java.util.List;

import com.ispw.bean.CampiBean;
import com.ispw.bean.CampoBean;
import com.ispw.bean.DatiDisponibilitaBean;
import com.ispw.bean.ParametriVerificaBean;

public interface GestioneDisponibilitaPrenotazione {

    CampiBean listaCampi();

    CampoBean recuperaCampo(int idCampo);

    List<DatiDisponibilitaBean> verificaDisponibilita(ParametriVerificaBean param);
}