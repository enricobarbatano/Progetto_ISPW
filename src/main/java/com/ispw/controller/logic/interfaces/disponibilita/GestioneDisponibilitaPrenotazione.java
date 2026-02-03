package com.ispw.controller.logic.interfaces.disponibilita;

import java.util.List;

import com.ispw.bean.DatiDisponibilitaBean;
import com.ispw.bean.ParametriVerificaBean;

public interface GestioneDisponibilitaPrenotazione {
    List<DatiDisponibilitaBean> verificaDisponibilita(ParametriVerificaBean param);
}
