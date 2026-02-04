package com.ispw.controller.logic.interfaces.disponibilita;

import java.util.List;

import com.ispw.bean.DatiDisponibilitaBean;

public interface GestioneDisponibilitaGestioneRegole {
    Boolean rimuoviDisponibilita(int idCampo);
    List<DatiDisponibilitaBean> attivaDisponibilita(int idCampo);
}
