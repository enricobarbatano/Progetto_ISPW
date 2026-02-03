package com.ispw.controller.logic.interfaces.fattura;


import com.ispw.bean.DatiFatturaBean;
import com.ispw.model.entity.Fattura;// ho dei dubbi che si possa fare questa cosa

public interface GestioneFatturaPenalita  {
    Fattura generaFatturaPenalita(DatiFatturaBean datiFatturaBean, int idPrenotazione);
}
