package com.ispw.controller.logic.interfaces.disponibilita;

import java.util.List;

import com.ispw.bean.DatiDisponibilitaBean;

public interface GestioneDisponibilitaGestioneRegole {
    Boolean rimuoviDisponibilità(int idCampo);
    List<DatiDisponibilitaBean> attivaDisponibilità(int idCampo);
}
