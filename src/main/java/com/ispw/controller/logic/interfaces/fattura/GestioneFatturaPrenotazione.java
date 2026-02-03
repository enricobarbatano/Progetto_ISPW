package com.ispw.controller.logic.interfaces.fattura;

import com.ispw.bean.DatiFatturaBean;
import com.ispw.model.entity.Fattura;

public interface GestioneFatturaPrenotazione {
    Fattura generaFatturaPrenotazione(DatiFatturaBean datiFatturaBean, int idPrenotazione);
}
