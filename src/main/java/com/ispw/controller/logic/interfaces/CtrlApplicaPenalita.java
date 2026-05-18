package com.ispw.controller.logic.interfaces;

import com.ispw.bean.DatiFatturaBean;
import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.DatiPenalitaBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.UtentiBean;

public interface CtrlApplicaPenalita {
    
    UtentiBean listaUtentiPerPenalita();

    EsitoOperazioneBean applicaSanzione(
            DatiPenalitaBean dati,
            DatiPagamentoBean datiPagamento,
            DatiFatturaBean datiFattura
    );

}
